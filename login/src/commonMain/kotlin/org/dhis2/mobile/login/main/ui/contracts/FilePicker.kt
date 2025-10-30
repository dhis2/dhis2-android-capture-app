package org.dhis2.mobile.login.main.ui.contracts

import androidx.compose.runtime.Composable

@Composable
expect fun filePicker(onResult: (String?) -> Unit): FilePicker

fun interface FilePicker {
    fun launch()
}
