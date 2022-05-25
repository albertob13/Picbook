package com.example.picbook.data

import android.content.Context
import androidx.lifecycle.LiveData

/**
 * Class shared between MainActivity and DetailActivity,
 * centralize changes to the data
 */
class DataRepository(private val imageDao: MSImageDao) {

    /**
     * Methods used for Database data manipulation
     */

    fun getImageList(): LiveData<List<MSImage>> {
        return imageDao.getImages()
    }

    suspend fun addImage(image: MSImage){
        imageDao.insertImage(image)
    }

    suspend fun removeImage(image: MSImage){
        imageDao.deleteImage(image)
    }

    suspend fun getImageForId(id: Int): MSImage {
        return imageDao.getImageForId(id)
    }

    companion object {
        private var INSTANCE: DataRepository? = null

        fun getDataSource(context: Context): DataRepository {
            return synchronized(DataRepository::class) {
                val newInstance = INSTANCE ?: DataRepository(AppDatabase.getInstance(context).imageDao())
                INSTANCE = newInstance
                newInstance
            }
        }
    }

}