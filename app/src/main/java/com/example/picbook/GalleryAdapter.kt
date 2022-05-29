package com.example.picbook


import android.graphics.BitmapFactory
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.picbook.data.MSImage


class GalleryAdapter(private val onClick: (MSImage) -> Unit,
                     private val onLongClick: (MSImage) -> Unit):
    ListAdapter<MSImage, GalleryAdapter.ImageViewHolder>(ImageDiffCallback) {

    inner class ImageViewHolder(itemView: View,
                                onClick: (MSImage) -> Unit,
                                onLongClick: (MSImage) -> Unit) :
        RecyclerView.ViewHolder(itemView) {

        private val imageView: ImageView = itemView.findViewById(R.id.image)
        private lateinit var currentImage: MSImage

        init {
            itemView.setOnClickListener {
                currentImage.let(onClick)
            }

            itemView.setOnLongClickListener{
                currentImage.let(onLongClick)
                true
            }
        }

        fun bind(image: MSImage){
            currentImage = image
            imageView.setImageBitmap(BitmapFactory.decodeFile(currentImage.thumbPath))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.gallery_item, parent, false)
        return ImageViewHolder(view, onClick, onLongClick)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = getItem(position)
        holder.bind(image)
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







