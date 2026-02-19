package com.maazm7d.termuxhub.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.maazm7d.termuxhub.data.local.entities.HallOfFameEntity
import com.maazm7d.termuxhub.data.local.entities.ToolEntity

@Database(
    entities = [
        ToolEntity::class,
        HallOfFameEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun toolDao(): ToolDao
    abstract fun hallOfFameDao(): HallOfFameDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "termuxhub_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
