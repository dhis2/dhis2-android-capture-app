package org.dhis2.utils.maps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import org.dhis2.R

object MarkerUtils {

    fun getMarker(context: Context, bitmap: Bitmap): Bitmap {
        val drawable: Drawable = ContextCompat.getDrawable(context, R.drawable.ic_img_marker_frame)!!
//        val canvasMarker: Bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_img_marker_frame)
        val canvasMarker = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        val markerBitmap = Bitmap.createBitmap(canvasMarker.width, canvasMarker.height, canvasMarker.config)
        val canvas = Canvas(markerBitmap)
//        canvas.drawBitmap(canvasMarker, 0f, 0f, null)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        canvas.drawBitmap(bitmap, canvasMarker.width / 2f - bitmap.width / 2f, 10f, null)
        return markerBitmap
    }
}