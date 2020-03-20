package org.dhis2.Bindings

import android.graphics.BitmapFactory
import java.io.File

fun File.widthAndHeight(minimun: Int? = null): Pair<Int, Int> {
    BitmapFactory.decodeFile(this.absolutePath).apply {
        val ratio = width.toFloat() / height.toFloat()

        return if (minimun != null && width > height && width < minimun) {
            Pair(minimun, (minimun.toFloat() / ratio).toInt())
        } else if (minimun != null && height >= width && height < minimun) {
            Pair((minimun.toFloat() * ratio).toInt(), minimun)
        } else {
            Pair(width, height)
        }
    }
}