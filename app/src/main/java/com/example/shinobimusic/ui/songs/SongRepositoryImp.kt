package com.example.shinobimusic.ui.songs

import android.content.Context
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import android.util.Base64
import androidx.lifecycle.LiveData
import com.example.shinobimusic.data.local.SongDao
import com.example.shinobimusic.data.model.Playlist
import com.example.shinobimusic.data.model.Song
import com.example.shinobimusic.domain.local.LocalDb
import com.example.shinobimusic.domain.repo.SongRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SongRepositoryImp @Inject constructor(
    private val localDb: LocalDb
) : SongRepository {

    override suspend fun getSongs(): List<Song> {
        return localDb.getSongs()
    }

    override suspend fun createPlaylist(name: String) {
        localDb.createPlaylist(name)
    }

    override fun getAllPlaylists(): LiveData<List<Playlist>> {
        return localDb.getAllPlaylists()
    }

    override suspend fun addSongToPlaylist(playlistId: Int, songPath: String) {
        localDb.addSongToPlaylist(playlistId, songPath)
    }

    override suspend fun removeSongFromPlaylist(playlistId: Int, songPath: String) {
        localDb.removeSongFromPlaylist(playlistId, songPath)
    }

    override suspend fun isSongInPlaylist(playlistId: Int, songPath: String): Boolean {
        return localDb.isSongInPlaylist(playlistId, songPath)
    }

    override suspend fun deletePlaylist(playlistId: Int) {
        localDb.deletePlaylist(playlistId)
    }
}
