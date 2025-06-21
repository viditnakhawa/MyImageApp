package com.viditnakhawa.myimageapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import androidx.room.Index

@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)

@Entity(
    tableName = "image_collection_cross_ref",
    primaryKeys = ["imageUri", "collectionId"],
    indices = [Index(value = ["collectionId"])]
)
data class ImageCollectionCrossRef(
    val imageUri: String,
    val collectionId: Long
)

data class CollectionWithImages(
    @Embedded val collection: CollectionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "imageUri",
        associateBy = Junction(
            value = ImageCollectionCrossRef::class,
            parentColumn = "collectionId",
            entityColumn = "imageUri"
        )
    )
    val images: List<ImageEntity>
)