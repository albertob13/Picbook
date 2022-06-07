package com.example.picbook.fragments

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.picbook.DisplayActivity
import com.example.picbook.GalleryAdapter
import com.example.picbook.GridItemDecorator
import com.example.picbook.R
import com.example.picbook.data.MSImage
import com.example.picbook.viewmodels.ImageListViewModel
import com.example.picbook.viewmodels.ImageListViewModelFactory

const val READ_EXTERNAL_STORAGE_REQUEST = 0x1045
const val REQUEST_CODE = 101


class GalleryFragment : Fragment() {

    private val imageListViewModel by activityViewModels<ImageListViewModel>{
        ImageListViewModelFactory(requireActivity().application)
    }

    private lateinit var galleryAdapter: GalleryAdapter
    private lateinit var gallery: RecyclerView
    private lateinit var emptyView: View
    private lateinit var headerText: TextView
    private lateinit var imageCount: TextView

    private lateinit var addFab: FloatingActionButton

    /**
     * Fragment for Tablet. Will be null if target device is a smartphone
     */
    private var detailsFrag: Fragment? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_gallery, container, false)

        /**
         * detailsFrag will be null if target device is a smartphone
         */
        detailsFrag = requireActivity().supportFragmentManager.findFragmentById(R.id.details_frag_large)

        /**
         * First of all check permission
         */
        if(!haveStoragePermission()){
            requestPermission()
        }

        gallery= view.findViewById(R.id.gallery)
        emptyView = view.findViewById(R.id.empty_view)
        headerText = view.findViewById(R.id.allImages)
        imageCount = view.findViewById(R.id.imageCount)

        addFab = view.findViewById(R.id.addFab)

        galleryAdapter = GalleryAdapter(
            onClick = { image: MSImage -> adapterOnClick(image) },
            onLongClick = {image: MSImage -> adapterOnLongClick(image)}
        )
        gallery.let{ rv ->
            rv.adapter = galleryAdapter

            //Define span count based on screen Width
            if(detailsFrag != null && resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT){
                //Tablet Portrait
                rv.layoutManager = GridLayoutManager(requireContext(), 6)
            }else if(detailsFrag == null && resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                //Smartphone Landscape
                rv.layoutManager = GridLayoutManager(requireContext(), 9)
            } else {
                //Smartphone Portrait or Tablet Landscape
                rv.layoutManager = GridLayoutManager(requireContext(), 4)
            }

            rv.addItemDecoration(GridItemDecorator())
        }

        /**
         * LiveData observer. If image list changes then update UI
         */
        imageListViewModel.allImages.observe(viewLifecycleOwner){
            imageCount.text = it.size.toString()
            if(it.isNotEmpty()){
                emptyView.visibility = View.GONE
            } else {
                emptyView.visibility = View.VISIBLE
            }
            galleryAdapter.submitList(it)
        }

        addFab.setOnClickListener{
            if(haveStoragePermission()){
                openGallery()
            }else{
                Toast
                    .makeText(requireContext(), "Check Settings for change permissions.", Toast.LENGTH_LONG)
                    .show()
            }
        }

        return view
    }


    /**
     *  User click RecyclerView Item -> Image detail will be displayed
     */
    private fun adapterOnClick(image: MSImage){
        if(detailsFrag == null){
            //if target device is smartphone...
            val intent = Intent(context, DisplayActivity::class.java)
            intent.putExtra("id", image.id)
            startActivity(intent)
        } else {
            //else update UI...
            (detailsFrag as DetailFragment).updateDisplayImage(image)
        }

    }

    /**
     * User long click an image for delete it from gallery
     */
    private fun adapterOnLongClick(image: MSImage){
        val alertDialog: AlertDialog? = activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setTitle(R.string.titleAlert)
                setMessage("Remove ${image.displayName} from gallery?")
                setPositiveButton(R.string.delAlert){ _, _ ->
                    imageListViewModel.removeImage(image)
                    if(detailsFrag != null)
                        (detailsFrag as DetailFragment).removeDisplayImage(image.id)
                }
                setNegativeButton(R.string.cancelAlert, null)
            }
            builder.create()
        }

        alertDialog?.show()
    }

    /**
     * Open Gallery for image selection, if permissions are verified
     */
    @Suppress("DEPRECATION")
    private fun openGallery(){
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }
        startActivityForResult(intent, REQUEST_CODE)
    }
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK){
            val imageUri: Uri? = data?.data
            if(imageUri != null){
                imageListViewModel.insertImage(imageUri)
            }else{
                Toast
                    .makeText(requireContext(), "Error importing image!", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }


    /**
     * Next lines provide user permissions for device gallery access
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == READ_EXTERNAL_STORAGE_REQUEST){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.i("GalleryFragment","External Storage permission granted.")
            }else{
                Log.i("GalleryFragment","External Storage permission NOT granted.")
            }
        }
    }

    private fun haveStoragePermission() = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun requestPermission(){

        if(!haveStoragePermission()){
            val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(requireActivity(), permissions, READ_EXTERNAL_STORAGE_REQUEST)
        }
    }
}
