package org.dhis2.uicomponents.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import org.dhis2.R

object TeiMarkers {

    private fun initMarkerCanvas(context: Context, color: Int): Pair<Canvas, Bitmap> {
        val drawable: Drawable =
            ContextCompat.getDrawable(context, R.drawable.ic_img_marker_frame)!!
        tintDrawable(drawable, color)
        drawable.mutate()

        val canvasMarker = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val markerBitmap =
            Bitmap.createBitmap(canvasMarker.width, canvasMarker.height, canvasMarker.config)
        val canvas = Canvas(markerBitmap)

        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        return Pair(canvas, markerBitmap)
    }

    private fun tintDrawable(drawable: Drawable, color: Int) {
        if (color != -1) {
            val wrapped = DrawableCompat.wrap(drawable)
            DrawableCompat.setTint(wrapped, color)
        }
    }

    fun getMarker(context: Context, bitmap: Bitmap): Bitmap {
        val drawable: Drawable =
            ContextCompat.getDrawable(context, R.drawable.ic_image_poi)!!
        drawable.mutate()

        val canvasMarker = Bitmap.createBitmap(
            drawable.intrinsicWidth,
            drawable.intrinsicHeight,
            Bitmap.Config.ARGB_8888
        )
        val markerBitmap =
            Bitmap.createBitmap(canvasMarker.width, canvasMarker.height, canvasMarker.config)
        val canvas = Canvas(markerBitmap)

        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)

        canvas.drawBitmap(
            bitmap,
            canvas.width / 2f - bitmap.width / 2f,
            canvas.height / 2f - bitmap.height / 2f - 5,
            null
        )
        return markerBitmap
    }

    fun getMarker(context: Context, teiImage: Drawable, color: Int): Bitmap {
        return getMarker(context, drawableToBitmap(teiImage))
    }

    fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable) {
            val bitmapDrawable: BitmapDrawable = drawable as BitmapDrawable
            if (bitmapDrawable.bitmap != null) {
                return bitmapDrawable.bitmap
            }
        }
        var bitmap: Bitmap = if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
            Bitmap.createBitmap(
                1,
                1,
                Bitmap.Config.ARGB_8888
            ) // Single color bitmap will be created of 1x1 pixel
        } else {
            Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
        }
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }
}
