package com.example.shinobimusic.data.local

import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.shinobimusic.data.model.Playlist
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

class PlaylistCallback @Inject constructor(
    private val playlistDaoProvider: Provider<PlayListDao>
):RoomDatabase.Callback() {

    override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        CoroutineScope(Dispatchers.IO).launch {
            val playListDao = playlistDaoProvider.get()
            playListDao.insertPlaylist(Playlist(name = "Favorites", songPaths = emptyList()))
            playListDao.insertPlaylist(Playlist(name = "Recently Played", songPaths = emptyList()))
        }


    }
}