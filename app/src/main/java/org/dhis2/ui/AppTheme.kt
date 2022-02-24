package org.dhis2.ui

import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.res.colorResource
import org.dhis2.R

@Composable
fun Dhis2Theme(
    programColor: String?,
    mainThemeColor: String?,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider() {
        MaterialTheme(
            colors = colorForProgram(programColor, mainThemeColor),
            content = content
        )
    }
}

object Dhis2Theme {
    val colors: Colors
        @Composable
        get() = MaterialTheme.colors
}

@Composable
fun colorForProgram(programColor: String?, mainThemeColor: String?): Colors {
    return Colors(
        primary = colorResource(id = R.color.colorPrimary),
        primaryVariant = colorResource(id = R.color.colorPrimaryDark),
        secondary = colorResource(id = R.color.colorPrimaryLight),
        secondaryVariant = colorResource(id = R.color.colorPrimaryLight),
        background = colorResource(id = R.color.white),
        surface = colorResource(id = R.color.white),
        error = colorResource(id = R.color.error_color),
        onPrimary = colorResource(id = R.color.primaryBgTextColor),
        onSecondary = colorResource(id = R.color.primaryLightBgTextColor),
        onBackground = colorResource(id = R.color.whiteBgTextColor),
        onSurface = colorResource(id = R.color.whiteBgTextColor),
        onError = colorResource(id = R.color.black_de0),
        isLight = true
    )
}
