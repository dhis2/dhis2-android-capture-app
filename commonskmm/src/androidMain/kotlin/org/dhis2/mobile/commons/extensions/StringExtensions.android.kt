package org.dhis2.mobile.commons.extensions

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File

actual fun String.toImageBitmap(): ImageBitmap? =
    File(this)
        .takeIf { it.exists() }
        ?.let { BitmapFactory.decodeFile(it.absolutePath).asImageBitmap() }
