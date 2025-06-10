package com.example.shinobimusic.ui.songs

import android.content.Context
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import android.util.Base64
import com.example.shinobimusic.data.model.Song
import com.example.shinobimusic.domain.repo.SongRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SongRepositoryImp @Inject constructor
    (@ApplicationContext private val context: Context) : SongRepository {

    override suspend fun getSongs(): List<Song> = withContext(Dispatchers.IO) {
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

        cursor?.use {
            val titleIndex = it.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val artistIndex = it.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val dataIndex = it.getColumnIndex(MediaStore.Audio.Media.DATA)
            val retriever = MediaMetadataRetriever()

            while (it.moveToNext()) {
                val title = it.getString(titleIndex)
                val artist = it.getString(artistIndex)
                val data = it.getString(dataIndex)

                val excludedPaths = listOf(
                    "/WhatsApp/", "/.thumbnails/", "/Notifications/",
                    "/Ringtones/", "/Alarms/", "/Recordings/", "/Download/voice notes"
                )
                if (excludedPaths.any { data.contains(it, ignoreCase = true) }) continue

                var albumArt: String? = null
                try {
                    retriever.setDataSource(data)
                    val art = retriever.embeddedPicture
                    if (art != null) {
                        albumArt = "data:image/jpeg;base64," + Base64.encodeToString(art, Base64.DEFAULT)
                    }
                } catch (_: Exception) {}

                songs.add(Song(title, artist, data, albumArt ?: ""))
            }

            retriever.release()
        }

        return@withContext songs
    }
}