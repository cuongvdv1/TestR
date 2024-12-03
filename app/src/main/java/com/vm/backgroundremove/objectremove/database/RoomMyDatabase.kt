package com.vm.backgroundremove.objectremove.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase


@androidx.room.Database(
    entities = [
        HistoryModel::class
    ],
    version = 2
)
abstract class RoomMyDatabase : RoomDatabase() {

    abstract fun historyDao(): HistoryDao?

    companion object {
        private var roomMyDatabase: RoomMyDatabase? = null
        const val DATABASE_NAME = "DATABASE_NAME"

        @Synchronized
        fun getInstance(context: Context): RoomMyDatabase? {
            if (roomMyDatabase == null) {
                roomMyDatabase =
                    Room.databaseBuilder(context, RoomMyDatabase::class.java, DATABASE_NAME)
                        .fallbackToDestructiveMigration()
                        .build()
            }
            return roomMyDatabase
        }
    }
}