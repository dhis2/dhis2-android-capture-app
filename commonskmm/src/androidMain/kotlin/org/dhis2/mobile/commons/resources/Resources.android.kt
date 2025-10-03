package org.dhis2.mobile.commons.resources

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

@Composable
actual fun getDrawableResource(name: String): Painter? {
    val context = LocalContext.current
    val appPackage = context.packageName

    fun getResource(
        pkg: String,
        resName: String,
    ): Int =
        try {
            context.resources.getIdentifier(resName, "drawable", pkg)
        } catch (_: Exception) {
            0
        }

    val resource = getResource(appPackage, name)
    return if (resource != 0) painterResource(resource) else null
}
