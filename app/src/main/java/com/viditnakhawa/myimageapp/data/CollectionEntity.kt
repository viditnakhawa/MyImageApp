package com.viditnakhawa.myimageapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

@Entity(tableName = "collections")
data class CollectionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String
)

@Entity(primaryKeys = ["imageUri", "collectionId"])
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
            // This tells Room that the 'collectionId' column in the junction table
            // links to the 'id' column of the parent (CollectionEntity).
            parentColumn = "collectionId",

            // This tells Room that the 'imageUri' column in the junction table
            // links to the 'imageUri' column of the entity (ImageEntity).
            entityColumn = "imageUri"
        )
    )
    val images: List<ImageEntity>
)