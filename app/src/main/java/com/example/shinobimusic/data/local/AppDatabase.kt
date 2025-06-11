package com.example.shinobimusic.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.shinobimusic.data.model.Converters
import com.example.shinobimusic.data.model.Playlist
import com.example.shinobimusic.data.model.Song

@Database(entities = [Song::class, Playlist::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlayListDao
}