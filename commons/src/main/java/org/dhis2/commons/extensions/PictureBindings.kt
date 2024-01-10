package org.dhis2.commons.extensions

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File

fun String.getBitmap(): Bitmap? = File(this)
    .takeIf { it.exists() }
    ?.let { BitmapFactory.decodeFile(it.absolutePath) }
