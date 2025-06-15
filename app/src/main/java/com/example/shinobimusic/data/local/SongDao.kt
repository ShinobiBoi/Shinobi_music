package com.example.shinobimusic.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.shinobimusic.data.model.Song

@Dao
interface SongDao {

    @Query("SELECT * FROM Song")
    suspend fun getAllSongs(): List<Song>



    @Query("SELECT * FROM Song WHERE data IN (:paths)")
    suspend fun getSongsByPaths(paths: List<String>): List<Song>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(songs: List<Song>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertScannedSongs(songs: List<Song>)

    @Query("DELETE FROM Song")
    suspend fun clearSongs()
}