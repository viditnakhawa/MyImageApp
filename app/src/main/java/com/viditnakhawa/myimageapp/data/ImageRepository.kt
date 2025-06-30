package com.viditnakhawa.myimageapp.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.net.toUri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ImageRepository(
    private val imageDao: ImageDao,
    private val context: Context,
    private val scope: CoroutineScope
) {

    val allImageEntities: Flow<List<ImageEntity>> = imageDao.getAllImageEntities()

    val allImages: Flow<List<Uri>> = imageDao.getAllImageUris().map { stringList ->
        stringList.map { uriString ->
            uriString.toUri()
        }
    }

    init {
        // Start listening for screenshot changes as soon as the repository is created
        observeScreenshotChanges()
    }

    private fun observeScreenshotChanges() {
        scope.launch {
            createScreenshotFlow(context.contentResolver).collect {
                // When a change is detected, trigger the robust sync logic
                syncScreenshotsFromDevice()
            }
        }
    }

    suspend fun addImage(uri: Uri) {
        val imageEntity = ImageEntity(
            imageUri = uri.toString(),
            lastModified = System.currentTimeMillis()
        )
        imageDao.insertImage(imageEntity)
    }

    suspend fun addImages(uris: List<Uri>) {
        // Add the lastModified timestamp for each image
        val entities = uris.map {
            ImageEntity(imageUri = it.toString(), lastModified = System.currentTimeMillis())
        }
        imageDao.insertImages(entities)
    }

    suspend fun syncScreenshotsFromDevice() = withContext(Dispatchers.IO) {
        val mediaStoreUris = mutableSetOf<String>()
        val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_ADDED)

        val collection =
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)

        val selection = "${MediaStore.Images.Media.RELATIVE_PATH} LIKE ?"
        val selectionArgs = arrayOf("%${Environment.DIRECTORY_SCREENSHOTS}%")

        context.contentResolver.query(collection, projection, selection, selectionArgs, null)
            ?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val uri = ContentUris.withAppendedId(collection, id)
                    mediaStoreUris.add(uri.toString())
                }
            }

        val databaseUris = imageDao.getAllUris().toSet()

        // 1. Add new images that are on disk but not in our database
        val newUris = (mediaStoreUris - databaseUris).map { it.toUri() }
        val newImages = newUris.map { uri ->
            ImageEntity(
                imageUri = uri.toString(),
                // Guarantee a valid timestamp for synced images, falling back to current time
                lastModified = getTimestampForUri(uri) ?: System.currentTimeMillis()
            )
        }
        if (newImages.isNotEmpty()) {
            imageDao.insertImages(newImages)
        }

        // 2. Remove images from our database that are no longer on disk
        /*
        val deletedUris = (databaseUris - mediaStoreUris).toList()
        if (deletedUris.isNotEmpty()) {
            imageDao.deleteImagesByUri(deletedUris)
        }
        */
    }

    private fun getTimestampForUri(uri: Uri): Long? {
        return try {
            context.contentResolver.query(uri, arrayOf(MediaStore.Images.Media.DATE_ADDED), null, null, null)
                ?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val timestampSeconds = cursor.getLong(0)
                        if (timestampSeconds > 0) timestampSeconds * 1000L else null
                    } else {
                        null
                    }
                }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun ignoreImage(uri: Uri) {
        imageDao.ignoreImage(uri.toString())
    }

    suspend fun deleteImage(uri: Uri) {
        imageDao.deleteImageByUri(uri.toString())
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

    suspend fun getImageDetails(uri: Uri): ImageEntity? {
        return imageDao.getImageByUri(uri.toString()).firstOrNull()
    }

    fun getImageDetailsFlow(uri: Uri): Flow<ImageEntity?> {
        return imageDao.getImageByUri(uri.toString())
    }

    suspend fun updateImageDetails(imageDetails: ImageEntity) {
        imageDao.updateImage(imageDetails)
    }

    suspend fun getUnanalyzedImages(): List<ImageEntity> {
        return imageDao.getUnanalyzedImages()
    }

    suspend fun updateImageNote(uri: Uri, note: String) {
        val image = imageDao.getImageByUri(uri.toString()).firstOrNull()
        if (image != null) {
            val updatedImage = image.copy(note = note)
            imageDao.updateImage(updatedImage)
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

