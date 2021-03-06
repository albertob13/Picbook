package com.example.picbook.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Data class to hold information about each Image picked from MediaStore
 */

@Entity(tableName = "image")
data class MSImage(
    @ColumnInfo(name = "displayName") val displayName: String,
    @ColumnInfo(name = "path") val path: String,
    @ColumnInfo(name = "thumb-path") val thumbPath: String
    ){
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Int = 0
}

