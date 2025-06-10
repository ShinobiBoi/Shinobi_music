package com.example.shinobimusic.ui.songs

import android.content.Context
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import android.util.Base64
import com.example.shinobimusic.data.local.SongDao
import com.example.shinobimusic.data.model.Song
import com.example.shinobimusic.domain.local.LocalDb
import com.example.shinobimusic.domain.repo.SongRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SongRepositoryImp @Inject constructor(private val localDb: LocalDb) : SongRepository {
    override suspend fun getSongs(): List<Song> {
        return localDb.getSongs()
    }


}