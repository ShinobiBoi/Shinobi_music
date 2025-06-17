package com.example.shinobimusic.ui.songs


import androidx.lifecycle.LiveData
import com.example.shinobimusic.data.model.Playlist
import com.example.shinobimusic.data.model.Song
import com.example.shinobimusic.domain.local.LocalDb
import com.example.shinobimusic.domain.repo.SongRepository
import javax.inject.Inject

class SongRepositoryImp @Inject constructor(
    private val localDb: LocalDb
) : SongRepository {

    override suspend fun getSongs(): List<Song> {
        return localDb.getSongs()
    }

    override suspend fun getSongsByPaths(songPaths: List<String>): List<Song> {
        return localDb.getSongsByPaths(songPaths)
    }

    override suspend fun scanSongs():List<Song> {
        return localDb.scanSongs()
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

    override suspend fun addSongToRecently(songPath: String) {
        localDb.addSongToRecently(songPath)
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
