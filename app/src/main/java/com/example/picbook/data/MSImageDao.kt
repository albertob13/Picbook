package com.example.picbook.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface MSImageDao {

    @Query("SELECT * FROM image")
    fun getImages(): LiveData<List<MSImage>>

    @Query("SELECT * FROM image WHERE id=:id")
    suspend fun getImageForId(id: Int): MSImage

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: MSImage)

    @Delete
    suspend fun deleteImage(image: MSImage)
}