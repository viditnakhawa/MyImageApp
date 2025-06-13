package com.viditnakhawa.myimageapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * The main Room database class for the application.
 */
@Database(entities = [ImageEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun imageDao(): ImageDao

    companion object {
        // Volatile ensures that the value of INSTANCE is always up-to-date
        @Volatile
        private var INSTANCE: AppDatabase? = null


        // 3. Define the migration plan
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add the new 'title', 'tags', and 'content' columns to the existing table
                db.execSQL("ALTER TABLE images ADD COLUMN title TEXT")
                db.execSQL("ALTER TABLE images ADD COLUMN tags TEXT")
                db.execSQL("ALTER TABLE images ADD COLUMN content TEXT")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "image_database"
                )
                    .addMigrations(MIGRATION_1_2) // 4. Add the migration to the builder
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

