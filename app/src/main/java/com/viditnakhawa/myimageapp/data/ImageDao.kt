package com.viditnakhawa.myimageapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ImageDao {
    /**
     * Gets all saved images from the database, ordered by newest first.
     */
    @Query("SELECT * FROM images ORDER BY rowid DESC")
    fun getAllImages(): Flow<List<ImageEntity>>

    /**
     * Inserts a single image. If the URI already exists, it's ignored.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertImage(image: ImageEntity): Long

    /**
     * Deletes an image from the database using its URI string.
     */
    @Query("DELETE FROM images WHERE imageUri = :uri")
    suspend fun deleteImage(uri: String): Int

    @Query("SELECT * FROM images WHERE imageUri = :uri")
    suspend fun getImageByUri(uri: String): ImageEntity?

    // The old insertImages for bulk loading can be removed if you no longer need it,
    // but we'll keep it for the initial scan from the repository.
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertImages(images: List<ImageEntity>): List<Long>

    @Update
    suspend fun updateImage(image: ImageEntity)


}
