package com.example.picbook


import android.content.Context
import android.graphics.Rect
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.setPadding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.picbook.data.MSImage
import kotlin.math.round


class GalleryAdapter(private val gallery: RecyclerView, private val onClick: (MSImage) -> Unit):
    ListAdapter<MSImage, GalleryAdapter.ImageViewHolder>(ImageDiffCallback) {

    /**
     * Array containing positions of selected items, used for retrieve basic style of all selected items
     * after user press Cancel button
     */
    val selPositions = ArrayList<Int>()

    inner class ImageViewHolder(itemView: View, onClick: (MSImage) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        private val imageView: ImageView = itemView.findViewById(R.id.image)
        private lateinit var currentImage: MSImage

        init {
            itemView.setOnClickListener { view ->
                val pos = gallery.getChildAdapterPosition(view)
                currentImage.let(onClick)
                view.isSelected = currentImage.selected
                if(view.isSelected){
                    selPositions.add(pos)
                }else {
                    selPositions.remove(pos)
                }
            }
        }

        fun bind(image: MSImage){
            currentImage = image
            imageView.setImageBitmap(image.bitmap)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.gallery_item, parent, false)
        return ImageViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val mediaStoreImage = getItem(position)
        holder.bind(mediaStoreImage)
    }
}

object ImageDiffCallback: DiffUtil.ItemCallback<MSImage>(){
    override fun areItemsTheSame(oldItem: MSImage, newItem: MSImage): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: MSImage, newItem: MSImage): Boolean {
        return oldItem.id == newItem.id
    }

}

class GridItemDecorator : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {

        super.getItemOffsets(outRect, view, parent, state)
        outRect.set(4,4,4,4)
    }
}







