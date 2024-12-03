package com.vm.backgroundremove.objectremove.di

import android.content.Context
import androidx.room.Room
import com.vm.backgroundremove.objectremove.database.HistoryRepository
import com.vm.backgroundremove.objectremove.database.RoomMyDatabase
import org.koin.dsl.module

val dBModule = module {
    single { provideDatabase(get()) }
    single { provideStylishDao(get()) }
    single { HistoryRepository(get()) }
}

private fun provideDatabase(context: Context): RoomMyDatabase {
    return Room.databaseBuilder(
        context.applicationContext,
        RoomMyDatabase::class.java,
        RoomMyDatabase.DATABASE_NAME
    )
        .fallbackToDestructiveMigration()
        .build()
}

private fun provideStylishDao(database: RoomMyDatabase) = database.historyDao()

