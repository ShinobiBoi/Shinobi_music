package com.example.shinobimusic.ui.artist

import androidx.lifecycle.LiveData
import com.example.shinobimusic.data.model.Playlist
import com.example.shinobimusic.data.model.Song
import com.example.shinobimusic.domain.local.LocalDb
import com.example.shinobimusic.domain.repo.ArtistRepo
import javax.inject.Inject

class ArtistRepoImp @Inject constructor(
    private val localDb: LocalDb
) : ArtistRepo {

    override fun getAllPlaylists(): LiveData<List<Playlist>> {
        return localDb.getAllPlaylists()
    }

    override suspend fun getSongs(): List<Song> {
        return localDb.getSongs()
    }

    override suspend fun getPlaylistByName(name: String): Playlist? {
        return localDb.getPlaylistByName(name)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        localDb.updatePlaylist(playlist)
    }

    override suspend fun createPlaylist(playlist: Playlist) {
        localDb.createPlaylist(playlist)
    }

}
