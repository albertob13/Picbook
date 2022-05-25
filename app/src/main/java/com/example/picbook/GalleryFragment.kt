package com.example.picbook

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.children
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
    private lateinit var selectAll: CheckBox

    private lateinit var addFab: FloatingActionButton
    private lateinit var editFab: FloatingActionButton
    private lateinit var cancelFab: FloatingActionButton
    private lateinit var deleteFab: FloatingActionButton

    private var editMode: Boolean = false

    /**
     * Save the selected images id, making them ready for deletion
     */
    private val pendingImages = mutableListOf<MSImage>()

    /**
     * Fragment for Tablet. Will be null if target device is a smartphone
     */
    private var detailsFrag: Fragment? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_gallery, container, false)

        /**
         * First of all check permission
         */
        if(!haveStoragePermission()){
            requestPermission()
        }

        gallery= view.findViewById(R.id.gallery)
        emptyView = view.findViewById(R.id.empty_view)
        headerText = view.findViewById(R.id.textView)
        selectAll = view.findViewById(R.id.selectAll)

        addFab = view.findViewById(R.id.addFab)
        editFab = view.findViewById(R.id.editFab)

        cancelFab = view.findViewById(R.id.cancelFab)
        deleteFab = view.findViewById(R.id.deleteFab)

        galleryAdapter = GalleryAdapter(gallery){image -> adapterOnClick(image)}
        gallery.let{ rv ->
            rv.adapter = galleryAdapter
            rv.layoutManager = GridLayoutManager(requireContext(), 3)
            rv.addItemDecoration(GridItemDecorator())
        }

        /**
         * LiveData observer. If image list changes then update UI
         */
        imageListViewModel.allImages.observe(viewLifecycleOwner){
            if(it.isNotEmpty()){
                emptyView.visibility = View.GONE
                editFab.isEnabled = true
            } else {
                emptyView.visibility = View.VISIBLE
                editFab.isEnabled = false
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

        editFab.setOnClickListener{
            editFab.visibility = View.GONE
            addFab.visibility = View.GONE
            cancelFab.visibility = View.VISIBLE
            deleteFab.visibility = View.VISIBLE
            headerText.visibility = View.INVISIBLE
            selectAll.visibility = View.VISIBLE
            editMode = true
        }
        cancelFab.setOnClickListener{
            cancelBehaviour()
        }

        deleteFab.setOnClickListener{

            if(pendingImages.isNotEmpty()){
                val alertDialog: AlertDialog? = activity?.let {
                    val builder = AlertDialog.Builder(it)
                    builder.apply {
                        setTitle("Delete images")
                        setMessage("Remove selected images from gallery?")
                        setPositiveButton(R.string.delAlert){ _, _ ->

                            editFab.visibility = View.VISIBLE
                            addFab.visibility = View.VISIBLE
                            cancelFab.visibility = View.GONE
                            deleteFab.visibility = View.GONE
                            headerText.visibility = View.VISIBLE
                            selectAll.visibility = View.INVISIBLE
                            editMode = false
                            Log.v("selectAll", "${pendingImages.size}")
                            imageListViewModel.removeImageList(pendingImages)
                            retrieveSelections()
                            pendingImages.clear()
                            selectAll.isChecked = false
                        }
                        setNegativeButton(R.string.cancAlert){ _, _ ->
                            // User cancelled the dialog
                            cancelBehaviour()
                        }
                    }
                    builder.create()
                }

                alertDialog?.show()
            }else{
                Toast
                    .makeText(requireContext(), "Select al least one image!", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        selectAll.setOnCheckedChangeListener{ _, checked ->
            val allImages = imageListViewModel.allImages.value
            if(checked){
                imageListViewModel.selectStateAll(true)
                gallery.children.forEach {
                    it.isSelected = true
                }
                if(allImages != null){
                    pendingImages.addAll(allImages)
                    Log.v("selectAll", "${pendingImages.size}")
                }
            }else{
                imageListViewModel.selectStateAll(false)
                gallery.children.forEach {
                    it.isSelected = false
                }
                pendingImages.clear()
            }

        }

        /**
         * detailsFrag will be null if target device is a smartphone
         */
        detailsFrag = requireActivity().supportFragmentManager.findFragmentById(R.id.details_frag_large)

        return view
    }

    /**
     * Called if user press Cancel button (Gallery or Alert Dialog)
     */
    private fun cancelBehaviour(){
        editFab.visibility = View.VISIBLE
        addFab.visibility = View.VISIBLE
        cancelFab.visibility = View.GONE
        deleteFab.visibility = View.GONE
        headerText.visibility = View.VISIBLE
        selectAll.visibility = View.INVISIBLE
        editMode = false
        pendingImages.forEach { it.selected = false }
        retrieveSelections()
        pendingImages.clear()
        imageListViewModel.selectStateAll(false)
        selectAll.isChecked = false
    }

    /**
     *  Called every time user click RecyclerView Item
     */
    private fun adapterOnClick(image: MSImage){
        if(!editMode){
            if(detailsFrag == null){
                //if target device is smartphone...
                val intent = Intent(context, DisplayActivity::class.java)
                intent.putExtra("id", image.id)
                startActivity(intent)
            } else {
                    //else update UI...
                (detailsFrag as DetailFragment).updateDisplayImage(image)
            }
        } else {
            if(!image.selected){
                image.selected = true
                pendingImages.add(image)
            } else {
                image.selected = false
                pendingImages.remove(image)
            }
        }
    }

    /**
     * Retrieve background color and selected state of selected items after Cancel/Delete click
     */
    private fun retrieveSelections(){
        val positions = galleryAdapter.selPositions
        for(i in 0 until positions.size){
            val index = positions[i]
            gallery.getChildAt(index).isSelected = false
        }
        positions.clear()
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
