package com.viditnakhawa.myimageapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * The main Room database class for the application.
 */
@Database(
    entities = [ImageEntity::class, CollectionEntity::class, ImageCollectionCrossRef::class],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun imageDao(): ImageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "image_database"
                )
                    // 2. Since this is for development, we can remove migrations
                    // and let Room create the new schema on a fresh install.
                    .fallbackToDestructiveMigration(false) // Helpful for development
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
