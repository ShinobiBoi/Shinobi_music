package com.example.shinobimusic.ui.playlist

import androidx.lifecycle.LiveData
import com.example.shinobimusic.data.model.Playlist
import com.example.shinobimusic.data.model.Song
import com.example.shinobimusic.domain.local.LocalDb
import com.example.shinobimusic.domain.repo.PlayListRepo
import javax.inject.Inject

class PlayListRepoImp @Inject constructor(
    private val localDb: LocalDb
) : PlayListRepo {

    override fun getAllPlaylists(): LiveData<List<Playlist>> {
        return localDb.getAllPlaylists()
    }

    override suspend fun getSongsByPaths(songPaths: List<String>): List<Song> {
        return localDb.getSongsByPaths(songPaths)
    }

    override suspend fun createPlaylist(playlist: Playlist) {
        localDb.createPlaylist(playlist)
    }

    override suspend fun addSongToPlaylist(playlistId: Int, songPath: String) {
        localDb.addSongToPlaylist(playlistId, songPath)
    }

    override suspend fun removeSongFromPlaylist(playlistId: Int, songPath: String) {
        localDb.removeSongFromPlaylist(playlistId, songPath)
    }
}
