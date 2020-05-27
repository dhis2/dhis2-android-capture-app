package org.dhis2.Bindings

import android.graphics.BitmapFactory
import java.io.File

fun File.widthAndHeight(minimum: Int? = null): Pair<Int, Int> {
    BitmapFactory.decodeFile(this.absolutePath).apply {
        return resizeToMinimum(minimum, width, height)
    }
}

fun resizeToMinimum(minimum: Int? = null, width: Int, height: Int): Pair<Int, Int> {
    val ratio = width.toFloat() / height.toFloat()

    return if (minimum != null && width > height && width < minimum) {
        Pair(minimum, (minimum.toFloat() / ratio).toInt())
    } else if (minimum != null && height >= width && height < minimum) {
        Pair((minimum.toFloat() * ratio).toInt(), minimum)
    } else {
        Pair(width, height)
    }
}
