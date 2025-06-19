package com.example.shinobimusic.ui.allplaylist

import androidx.lifecycle.LiveData
import com.example.shinobimusic.data.model.Playlist
import com.example.shinobimusic.domain.local.LocalDb
import com.example.shinobimusic.domain.repo.AllPlaylistsRepo
import javax.inject.Inject

class AllPlaylistsRepoImp @Inject constructor(
    private val localDb: LocalDb
) : AllPlaylistsRepo {

    override fun getAllPlaylists(): LiveData<List<Playlist>> {
        return localDb.getAllPlaylists()
    }

    override suspend fun createPlaylist(playlist: Playlist) {
        localDb.createPlaylist(playlist)
    }
}
