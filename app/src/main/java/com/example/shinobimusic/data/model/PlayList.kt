package com.example.shinobimusic.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "playlists")
data class Playlist(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val songPaths: List<String>
)

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromSongPathList(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun toSongPathList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }
}