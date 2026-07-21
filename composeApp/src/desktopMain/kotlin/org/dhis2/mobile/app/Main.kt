package org.dhis2.mobile.app

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() =
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "DHIS2",
        ) {
            App()
        }
    }
