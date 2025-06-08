package com.example.shinobimusic

import android.content.Context
import android.util.Log
import com.example.shinobimusic.data.model.Song
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileNotFoundException

object SongCacheManager {
    private const val FILE_NAME = "songs_cache.json"
    private val gson = Gson()
    private val type = object : TypeToken<List<Song>>() {}.type

    fun saveSongs(context: Context, songs: List<Song>) {
        try {
            val json = gson.toJson(songs)
            context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE).use {
                it.write(json.toByteArray())
            }
        } catch (e: Exception) {
            Log.e("SongCache", "Failed to save songs", e)
            throw e
        }
    }

    fun loadSongs(context: Context): List<Song>? {
        return try {
            context.openFileInput(FILE_NAME).use { stream ->
                val json = stream.bufferedReader().use { it.readText() }
                gson.fromJson(json, type)
            }
        } catch (e: FileNotFoundException) {
            null // File doesn't exist yet
        } catch (e: Exception) {
            Log.e("SongCache", "Failed to load songs", e)
            null
        }
    }

    fun clearCache(context: Context) {
        try {
            context.deleteFile(FILE_NAME)
        } catch (e: Exception) {
            Log.e("SongCache", "Failed to clear cache", e)
        }
    }
}