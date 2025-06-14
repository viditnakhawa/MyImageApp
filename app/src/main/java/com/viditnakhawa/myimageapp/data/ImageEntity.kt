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
    // Add a field for the AI-generated title
    var title: String? = null,
    // Add a field for the AI-generated tags
    var tags: List<String>? = null,
    // We can rename aiSummary to content to be more generic
    var content: String? = null,
    //Source App used in SmartAnalysisWorker
    var sourceApp: String? = null
)
