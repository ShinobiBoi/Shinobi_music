package com.example.shinobimusic.domain.repo

import androidx.lifecycle.LiveData
import com.example.shinobimusic.data.model.Playlist

interface AllPlaylistsRepo {
    fun getAllPlaylists(): LiveData<List<Playlist>>
    suspend fun createPlaylist(playlist: Playlist)
}
