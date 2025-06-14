package com.viditnakhawa.myimageapp.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.viditnakhawa.myimageapp.workers.ImageAnalysisWorker
import kotlinx.coroutines.flow.firstOrNull
import java.io.File

class ImageRepository(private val imageDao: ImageDao) {

    val allImages: Flow<List<Uri>> = imageDao.getAllImages().map { entityList ->
        entityList.map { Uri.parse(it.imageUri) }
    }

    suspend fun addImage(uri: Uri) {
        imageDao.insertImage(ImageEntity(imageUri = uri.toString()))
    }

    suspend fun addImages(uris: List<Uri>) {
        val entities = uris.map { ImageEntity(imageUri = it.toString()) }
        imageDao.insertImages(entities)
    }

    suspend fun deleteImage(uri: Uri) {
        imageDao.deleteImage(uri.toString())
    }

    suspend fun updateImageSummary(uriString: String, summary: String) {
        withContext(Dispatchers.IO) {
            val image = imageDao.getImageByUri(uriString).firstOrNull()
            if (image != null) {
                val updatedImage = image.copy(content = summary)
                imageDao.updateImage(updatedImage)
            }
        }
    }


    //PHASE 1 CODE
    suspend fun getImageDetails(uri: Uri): ImageEntity? {
        return imageDao.getImageByUri(uri.toString()).firstOrNull()
    }

    suspend fun getImageDetailsFlow(uri: Uri): Flow<ImageEntity?> {
        return imageDao.getImageByUri(uri.toString())
    }

    //PHASE 1 CODE
    suspend fun updateImageDetails(imageDetails: ImageEntity) {
        imageDao.updateImage(imageDetails)
    }

    //PHASE 1 CODE
    fun isGemmaModelDownloaded(context: Context): Boolean {
        val model = GEMMA_E2B_MODEL
        val file = File(model.getPath(context))
        return file.exists() && file.length() >= model.sizeInBytes
    }

    suspend fun getUnanalyzedImages(): List<ImageEntity> {
        return imageDao.getUnanalyzedImages()
    }

    /**
     * Scans the device's MediaStore for screenshots and only adds new ones to the database.
     * This is much more efficient on subsequent app launches.
     */
    suspend fun refreshImagesFromDevice(context: Context) {
        withContext(Dispatchers.IO) {
            val deviceImageUris = mutableSetOf<String>()
            val collection =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

            val projection = arrayOf(MediaStore.Images.Media._ID)
            val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
            } else {
                "${MediaStore.Images.Media.DATA} LIKE ?"
            }
            val selectionArgs = arrayOf("%${Environment.DIRECTORY_SCREENSHOTS}%")

            // 1. Get all screenshot URIs from the device
            context.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val contentUri: Uri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    deviceImageUris.add(contentUri.toString())
                }
            }

            // 2. Get all URIs currently in our database
            val databaseImageUris = imageDao.getAllImageUris().toSet()

            // 3. Find only the new images that are on the device but not in our database
            val newImageUris = deviceImageUris - databaseImageUris

            // 4. If there are new images, add them to the database
            if (newImageUris.isNotEmpty()) {
                val newImageEntities = newImageUris.map { ImageEntity(imageUri = it) }
                imageDao.insertImages(newImageEntities)


                val workManager = WorkManager.getInstance(context)
                newImageUris.forEach { uri ->
                    val workRequest = OneTimeWorkRequestBuilder<ImageAnalysisWorker>()
                        .setInputData(workDataOf("IMAGE_URI" to uri))
                        .build()
                    workManager.enqueue(workRequest)
                }
            }
        }
    }
}

