package com.viditnakhawa.myimageapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a single image record in our database.
 * The imageUri is the primary key, ensuring no duplicate URIs are stored.
 */
@Entity(tableName = "images")
data class ImageEntity(
    @PrimaryKey val imageUri: String,
    // Add a new nullable field for the summary
    val aiSummary: String? = null
)
