package com.example.shinobimusic.data.local

import android.content.Context
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import com.example.shinobimusic.data.model.Playlist
import com.example.shinobimusic.data.model.Song
import com.example.shinobimusic.domain.local.LocalDb
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class LocalDbImp@Inject constructor
    (@ApplicationContext private val context: Context,
     private val songDao: SongDao,
     private val playListDao: PlayListDao
) :LocalDb {

    override suspend fun getSongs(): List<Song> {
        val cachedSongs = songDao.getAllSongs()
        if (cachedSongs.isNotEmpty()) {
            return cachedSongs
        }

        // Scan if cache is empty
        val songs = scanSongsFromStorage()
        if (songs.isNotEmpty()) {
            songDao.insertAll(songs)
        }

        return songs
    }

    override suspend fun scanSongs():List<Song> {

        // Scan if cache is empty
        val songs = scanSongsFromStorage()
        if (songs.isNotEmpty()) {
            songDao.insertScannedSongs(songs)
        }
        return songs

    }

    override suspend fun getSongsByPaths(songPaths: List<String>): List<Song> {
        val cachedSongs = songDao.getSongsByPaths(songPaths)
        if (cachedSongs.isNotEmpty()) {
            val songMap = cachedSongs.associateBy { it.data }
            return songPaths.mapNotNull { songMap[it] }
        }

        // Scan if cache is empty
        val songs = scanSongsFromStorage()
        if (songs.isNotEmpty()) {
            songDao.insertAll(songs)
        }
        val songMap = songs.associateBy { it.data }
        return songPaths.mapNotNull { songMap[it] }
    }

    private fun scanSongsFromStorage(): List<Song> {
        val songs = mutableListOf<Song>()
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DATE_ADDED
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.DATE_ADDED} DESC"

        val cursor = context.contentResolver.query(uri, projection, selection, null, sortOrder)
        val retriever = MediaMetadataRetriever()

        cursor?.use {
            val titleIndex = it.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val artistIndex = it.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val dataIndex = it.getColumnIndex(MediaStore.Audio.Media.DATA)

            while (it.moveToNext()) {

                val excludedPaths = listOf(
                    "/WhatsApp/", "/.thumbnails/", "/Notifications/",
                    "/Ringtones/", "/Alarms/", "/Recordings/", "/Download/voice notes"
                )
                val data = it.getString(dataIndex)
                if (excludedPaths.any { data.contains(it, ignoreCase = true) }) continue

                val title = it.getString(titleIndex)
                val artist = it.getString(artistIndex)

                songs.add(Song(title, artist, data))
            }
        }

        retriever.release()
        return songs
    }



    override suspend fun createPlaylist(playlist: Playlist) {
        playListDao.insertPlaylist(playlist)
    }


    override suspend fun getPlaylistByName(name: String): Playlist? {
        return playListDao.getPlaylistByName(name)
    }

    override  fun getAllPlaylists(): LiveData<List<Playlist>> {
        return playListDao.getAllPlaylists()
    }

    override suspend fun addSongToPlaylist(playlistId: Int, songPath: String) {
        val playlist = playListDao.getPlaylistById(playlistId)
        if (!playlist.songPaths.contains(songPath)) {
            val updatedSongs = playlist.songPaths + songPath
            val updatedPlaylist = playlist.copy(songPaths = updatedSongs)
            playListDao.updatePlaylist(updatedPlaylist)
        }
    }

override suspend fun addSongToRecently(songPath: String) {
    val playlist = playListDao.getPlaylistById(2) // Assuming ID 1 is for "Recently Played"

    // Remove song if it already exists to avoid duplicates
    val cleanedList = playlist.songPaths.filter { it != songPath }

    // Add the song at the top
    val updatedSongs = listOf(songPath) + cleanedList

    // Optional: Limit to last 50 songs
    val limitedSongs = updatedSongs.take(50)

    val updatedPlaylist = playlist.copy(songPaths = limitedSongs)
    playListDao.updatePlaylist(updatedPlaylist)
    }

    override suspend fun updatePlaylist(playlist: Playlist) {
        playListDao.updatePlaylist(playlist)
    }


    override suspend fun removeSongFromPlaylist(playlistId: Int, songPath: String)  {
        val playlist = playListDao.getPlaylistById(playlistId)
        val updatedSongs = playlist.songPaths - songPath
        val updatedPlaylist = playlist.copy(songPaths = updatedSongs)
        playListDao.updatePlaylist(updatedPlaylist)
    }

    override suspend fun isSongInPlaylist(playlistId: Int, songPath: String): Boolean {
        val playlist = playListDao.getPlaylistById(playlistId)
        return playlist.songPaths.contains(songPath)
    }

    override suspend fun deletePlaylist(playlistId: Int)  {
        val playlist = playListDao.getPlaylistById(playlistId)
        playListDao.deletePlaylist(playlist)
    }







}