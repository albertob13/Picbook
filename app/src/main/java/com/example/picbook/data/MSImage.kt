package com.example.picbook.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/**
 * Data class to hold information about each Image picked from MediaStore
 */

@Entity(tableName = "image")
data class MSImage(
    @ColumnInfo(name = "displayName") val displayName: String,
    @ColumnInfo(name = "path") val path: String
    ){
    @PrimaryKey(autoGenerate = true) @ColumnInfo(name = "id") var id: Int = 0
    @Ignore var bitmap: Bitmap? = BitmapFactory.decodeFile(path)
    @Ignore var selected: Boolean = false
}

