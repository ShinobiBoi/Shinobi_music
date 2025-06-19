package com.example.shinobimusic.domain.repo

import androidx.lifecycle.LiveData
import com.example.shinobimusic.data.model.Playlist
import com.example.shinobimusic.data.model.Song

interface SongRepository {
    suspend fun getSongs(): List<Song>
    suspend fun getSongsByPaths(songPaths: List<String>): List<Song>
    suspend fun scanSongs():List<Song>

    suspend fun createPlaylist(playlist: Playlist)

    fun getAllPlaylists(): LiveData<List<Playlist>>
    suspend fun getPlaylistByName(name: String): Playlist?

    suspend fun addSongToPlaylist(playlistId: Int, songPath: String)

    suspend fun addSongToRecently(songPath: String)

    suspend fun updatePlaylist(playlist: Playlist)

    suspend fun removeSongFromPlaylist(playlistId: Int, songPath: String)

    suspend fun isSongInPlaylist(playlistId: Int, songPath: String): Boolean

    suspend fun deletePlaylist(playlistId: Int)
}
