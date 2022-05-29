package com.example.picbook

import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.BitmapCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.picbook.data.MSImage
import com.example.picbook.viewmodels.ImageDetailViewModel
import com.example.picbook.viewmodels.ImageDetailViewModelFactory


class DetailFragment : Fragment() {

    private val imageDetailViewModel by activityViewModels<ImageDetailViewModel>{
        ImageDetailViewModelFactory(requireContext())
    }

    private lateinit var imageDisplayed: MSImage
    private lateinit var imageView: ZoomImageView
    private lateinit var imageName: TextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                      savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_detail, container, false)

        imageView = view.findViewById(R.id.imageDetail)
        imageName = view.findViewById(R.id.imageName)

        val imageId = requireActivity().intent.getIntExtra("id", -1)
        //if target device is smartphone, imageId=-1 always
        if(imageId != -1){
            imageDetailViewModel.imageToDisplay(imageId)
        }

        imageDetailViewModel.displayImage.observe(viewLifecycleOwner){
            if(it != null){
                imageDisplayed = it
                imageName.visibility = View.VISIBLE
                imageView.visibility = View.VISIBLE
                imageName.text = it.displayName
                imageView.resetPresentScale()   //set presentScale default value
                imageView.setImageBitmap(BitmapFactory.decodeFile(it.path))
            }else{
                imageName.visibility = View.INVISIBLE
                imageView.visibility = View.INVISIBLE

            }
        }
        return view
    }

    fun deleteImage(){
        val alertDialog: AlertDialog? = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle("Delete image")
                setMessage("Remove ${imageDisplayed.displayName} from gallery?")
                setPositiveButton(R.string.delAlert) { _, _ ->
                    imageDetailViewModel.removeImage(imageDisplayed)
                    imageView.visibility = View.INVISIBLE
                    imageName.visibility = View.INVISIBLE
                    if (requireActivity() is DisplayActivity) {
                        //if target device is Smartphone, finish Display Activity
                        requireActivity().finish()
                    }
                }
                setNegativeButton("Cancel", null)
            }
            builder.create()
        }

        alertDialog?.show()
    }

    fun updateDisplayImage(image: MSImage){
            imageDetailViewModel.imageToDisplay(image.id)
    }
    fun removeDisplayImage(){
        imageDetailViewModel.imageToDisplay(-1)
    }
}

