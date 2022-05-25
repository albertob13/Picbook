package com.example.picbook.viewmodels

import android.app.Application
import android.content.ContentUris
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.ImageDecoder.decodeBitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.*
import com.example.picbook.data.*
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File


class ImageListViewModel(application: Application) : AndroidViewModel(application) {

    private val imageDao = AppDatabase.getInstance(application).imageDao()
    private val dataRepository = DataRepository(imageDao)

    val allImages: LiveData<List<MSImage>> = dataRepository.getImageList()

    /**
     * Add image to Room database
     */
    fun insertImage(uri: Uri){
        viewModelScope.launch {
            val queriedImage = queryImage(uri)
            dataRepository.addImage(queriedImage)
        }
    }

    /**
     * Return MSImage queried from device filesystem.
     * MSImage will contain an identifier, the display name, the path of the internal storage file
     * and a default selected flag set to 'false'.
     */
    private fun queryImage(inputUri: Uri): MSImage {

        lateinit var image: MSImage
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
        )

        val contentResolver = getApplication<Application>().contentResolver
        contentResolver.query(
            inputUri,
            projection,
            null,
            null,
            null
        )?.use { cursor ->

            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            cursor.moveToNext()
            val id = cursor.getLong(idColumn)
            val displayName = cursor.getString(displayNameColumn)
            val contentUri = ContentUris.withAppendedId(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                id
            )
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.createSource(contentResolver, contentUri).run {
                    decodeBitmap(this)
                }
            } else {
                MediaStore.Images.Media.getBitmap(contentResolver, contentUri)
            }

            val sampled = BitmapSampling.sampledBitmap(bitmap)
            //Original file
            val bitmapFile = createBitmapFile(getApplication<Application>().applicationContext, bitmap, displayName)
            //Sampled file (image preview)
            val sampledFile = createBitmapFile(getApplication<Application>().applicationContext, sampled, "sampled_$displayName")
            if(bitmapFile != null && sampledFile != null){
                image = MSImage(displayName,bitmapFile.absolutePath, sampledFile.absolutePath)
            }
            Log.i(TAG, "Added image: ($displayName, $contentUri)")
        }
        return image
    }

    /**
     * Remove all images selected in the gallery
     */
    fun removeImageList(imageList: List<MSImage>){
        imageList.forEach{
            viewModelScope.launch {
                dataRepository.removeImage(it)
            }
        }
    }

    /**
     * Set all images in selected state
     */
    fun selectStateAll(newSelected: Boolean){
        allImages.value?.forEach {
            it.selected = newSelected
        }
    }

    /**
     * Save the target bitmap as a File and put it in the device internal storage, which is accessible
     * only by the application.
     */
    private fun createBitmapFile(context: Context,
                                 bitmap: Bitmap?,
                                 fileName: String): File?{

        val wrapper = ContextWrapper(context)
        var file: File? = null

        return try{
            val dir = wrapper.getDir("Images", MODE_PRIVATE)
            file = File(dir, fileName)

            val stream = file.outputStream()    //create an output stream for the file
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 50, stream)
            stream.flush()  //complete all stream processing
            stream.close()  //close the stream
            file
        }catch (e: Exception){
            e.printStackTrace()
            file    //return null file
        }
    }
}

class ImageListViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImageListViewModel::class.java)) {
            return ImageListViewModel(
                application
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

private const val TAG = "MainActivityVM"