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

class ImageRepository(private val imageDao: ImageDao) {

    /**
     * Provides a Flow of image URIs directly from the database.
     */
    val allImages: Flow<List<Uri>> = imageDao.getAllImages().map { entityList ->
        entityList.map { Uri.parse(it.imageUri) }
    }

    /**
     * Adds a single image URI to the database.
     */
    suspend fun addImage(uri: Uri) {
        imageDao.insertImage(ImageEntity(imageUri = uri.toString()))
    }

    /**
     * Deletes a single image URI from the database.
     */
    suspend fun deleteImage(uri: Uri) {
        imageDao.deleteImage(uri.toString())
    }

    suspend fun updateImageSummary(uri: String, summary: String) {
        val image = imageDao.getImageByUri(uri) // We'll need to add this to the DAO
        if (image != null) {
            imageDao.updateImage(image.copy(aiSummary = summary))
        }
    }
    /**
     * Scans the device's MediaStore for screenshots and saves new ones to the database.
     */
    suspend fun refreshImagesFromDevice(context: Context) {
        withContext(Dispatchers.IO) {
            val imageList = mutableListOf<ImageEntity>()
            val collection =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
                } else {
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                }

            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.RELATIVE_PATH // We need this for the selection
            )

            // --- THE FIX IS HERE ---
            // On modern Android (API 29+), use the official Environment constant.
            // On older versions, fall back to the old method.
            val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
            } else {
                "${MediaStore.Images.Media.DATA} LIKE ?"
            }
            val selectionArgs = arrayOf("%${Environment.DIRECTORY_SCREENSHOTS}%")


            context.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                "${MediaStore.Images.Media.DATE_ADDED} DESC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val contentUri: Uri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    imageList.add(ImageEntity(imageUri = contentUri.toString()))
                }
            }

            if (imageList.isNotEmpty()) {
                imageDao.insertImages(imageList)
            }
        }
    }
}