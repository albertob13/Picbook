package com.example.picbook.viewmodels

import android.content.Context
import androidx.lifecycle.*
import com.example.picbook.data.DataRepository
import com.example.picbook.data.MSImage
import kotlinx.coroutines.launch

class ImageDetailViewModel(val dataRepository: DataRepository): ViewModel() {

    /**
     * LiveData used for update the image displayed. When 'displayImage' is updated, Ui will be
     * automatically updated. Useful both for smartphone and tablet
     */
    private val mutableDisplayImage = MutableLiveData<MSImage>()
    val displayImage: LiveData<MSImage> get() = mutableDisplayImage

    /**
     * Remove displayed image from Room database
     */
    fun removeImage(image: MSImage){
        viewModelScope.launch {
            dataRepository.removeImage(image)
        }
    }

    /**
     * Update image to display
     */
    fun imageToDisplay(id: Int){
        viewModelScope.launch {
            mutableDisplayImage.value = dataRepository.getImageForId(id)
        }
    }
}

class ImageDetailViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ImageDetailViewModel::class.java)) {
            return ImageDetailViewModel(
                dataRepository = DataRepository.getDataSource(context)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}