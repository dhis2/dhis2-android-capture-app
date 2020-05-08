package org.dhis2.uicomponents.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import org.dhis2.R

object MarkerUtils {

    fun getMarker(context: Context, bitmap: Bitmap, color: Int): Bitmap {
        val (canvas, markerBitmap) = initMarkerCanvas(context, color)

        canvas.drawBitmap(bitmap, canvas.width / 2f - bitmap.width / 2f, 10f, null)
        return markerBitmap
    }

    fun getMarker(context: Context, teiImage: Drawable, color: Int): Bitmap {
        val (canvas, markerBitmap) = initMarkerCanvas(context, color)

        val paint = Paint()
        paint.color = Color.WHITE
        canvas.drawRect(15f, 10f, canvas.width - 15f, canvas.width - 25f, paint)

        teiImage.setBounds(15, 10, canvas.width - 15, canvas.width - 25)
        teiImage.draw(canvas)

        return markerBitmap
    }

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
}
