package com.example.shinobimusic.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.shinobimusic.data.model.Song

@Database(entities = [Song::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
}