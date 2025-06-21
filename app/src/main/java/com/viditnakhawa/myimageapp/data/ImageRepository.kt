package com.viditnakhawa.myimageapp.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.firstOrNull
import java.io.File
import androidx.core.net.toUri

class ImageRepository(private val imageDao: ImageDao) {

    val allImages: Flow<List<Uri>> = imageDao.getAllImageUris().map { stringList ->
        stringList.map { uriString ->
            uriString.toUri()
        }
    }

    val allImageEntities: Flow<List<ImageEntity>> = imageDao.getAllImageEntities()


    suspend fun addImage(uri: Uri) {
        imageDao.insertImage(ImageEntity(imageUri = uri.toString()))
    }

    suspend fun addImages(uris: List<Uri>) {
        val entities = uris.map { ImageEntity(imageUri = it.toString()) }
        imageDao.insertImages(entities)
    }

    suspend fun ignoreImage(uri: Uri) {
        imageDao.ignoreImage(uri.toString())
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
            val collection =
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)

            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_MODIFIED
            )
            val selection =
                "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"

            val selectionArgs = arrayOf("%${Environment.DIRECTORY_SCREENSHOTS}%")

            context.contentResolver.query(
                collection,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED)

                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val lastModified = cursor.getLong(dateModifiedColumn)

                    val uri: Uri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )

                    // Check if image already exists in DB to avoid un-ignoring
                    val existingImage = imageDao.getImageByUri(uri.toString()).firstOrNull()
                    if (existingImage == null) {
                        val imageEntity = ImageEntity(
                            imageUri = uri.toString(),
                            lastModified = lastModified,
                            isIgnored = false
                        )
                        imageDao.insertImage(imageEntity)
                    }
                }
            }
        }
    }

    // --- Collection Functions ---
    val collections: Flow<List<CollectionEntity>> = imageDao.getAllCollections()
    val collectionsWithImages: Flow<List<CollectionWithImages>> = imageDao.getCollectionsWithImages()

    suspend fun createCollection(name: String) {
        imageDao.insertCollection(CollectionEntity(name = name))
    }

    suspend fun addImageToCollection(imageUri: String, collectionId: Long) {
        imageDao.addImageToCollection(ImageCollectionCrossRef(imageUri = imageUri, collectionId = collectionId))
    }

    suspend fun createCollectionAndGetId(name: String): Long {
        return imageDao.insertCollection(CollectionEntity(name = name))
    }

    suspend fun addImagesToCollection(imageUris: List<String>, collectionId: Long) {
        val crossRefs = imageUris.map { ImageCollectionCrossRef(imageUri = it, collectionId = collectionId) }
        imageDao.addImagesToCollection(crossRefs)
    }

    fun getCollectionWithImagesById(collectionId: Long): Flow<CollectionWithImages?> {
        return imageDao.getCollectionWithImagesById(collectionId)
    }

    suspend fun removeImagesFromCollection(uris: List<String>, collectionId: Long) {
        imageDao.removeImagesFromCollection(uris, collectionId)
    }

    suspend fun deleteCollectionById(collectionId: Long) {
        imageDao.deleteCollectionById(collectionId)
    }

    suspend fun updateCollectionName(collectionId: Long, newName: String) {
        imageDao.updateCollectionName(collectionId, newName)
    }

}

