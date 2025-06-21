package com.viditnakhawa.myimageapp.data

import android.net.Uri
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {

    @Query("SELECT * FROM images ORDER BY lastModified DESC")
    fun getAllImages(): Flow<List<ImageEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertImage(image: ImageEntity): Long

    @Update
    suspend fun updateImage(image: ImageEntity)

    @Query("UPDATE images SET isIgnored = 1 WHERE imageUri = :uriString")
    suspend fun ignoreImage(uriString: String)

    @Query("SELECT * FROM images WHERE imageUri = :uri")
    fun getImageByUri(uri: String): Flow<ImageEntity?>

    @Query("SELECT imageUri FROM images WHERE isIgnored = 0 ORDER BY lastModified DESC")
    fun getAllImageUris(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertImages(images: List<ImageEntity>): List<Long>

    @Query("SELECT * FROM images WHERE sourceApp IS NULL")
    suspend fun getUnanalyzedImages(): List<ImageEntity>

    // --- Collection Functions ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCollection(collection: CollectionEntity): Long

    @Query("SELECT * FROM collections ORDER BY name ASC")
    fun getAllCollections(): Flow<List<CollectionEntity>>

    @Transaction
    @Query("SELECT * FROM collections")
    fun getCollectionsWithImages(): Flow<List<CollectionWithImages>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addImageToCollection(crossRef: ImageCollectionCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addImagesToCollection(crossRefs: List<ImageCollectionCrossRef>)

    @Transaction
    @Query("SELECT * FROM collections WHERE id = :collectionId")
    suspend fun getCollectionWithImages(collectionId: Long): CollectionWithImages
}
