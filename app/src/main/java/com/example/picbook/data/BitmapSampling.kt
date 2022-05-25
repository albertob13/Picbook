package com.example.picbook.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream

class BitmapSampling {

    companion object{

        /**
         *  Create a 250x250 sampled bitmap. It will used for image preview in gallery.
         */
        fun sampledBitmap(bitmap: Bitmap): Bitmap?{
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, stream)
            return decodeSampledBitmap(stream.toByteArray())
        }

        private fun decodeSampledBitmap(byteArray: ByteArray): Bitmap?{

            return BitmapFactory.Options().run {
                inJustDecodeBounds = true
                BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, this)

                inSampleSize = calculateInSampleSize(this)

                inJustDecodeBounds = false
                BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, this)
            }
        }

        private fun calculateInSampleSize(options: BitmapFactory.Options): Int {
            // Raw height and width of image
            val (height: Int, width: Int) = options.run { outHeight to outWidth }
            var inSampleSize = 1

            if (height > 250 || width > 250) {

                val halfHeight: Int = height / 2
                val halfWidth: Int = width / 2

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while (halfHeight / inSampleSize >= 250 && halfWidth / inSampleSize >= 250) {
                    inSampleSize *= 2
                }
            }

            return inSampleSize
        }
    }
}