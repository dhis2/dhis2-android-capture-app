package org.dhis2.mobile.login.main.ui.contracts

import androidx.compose.runtime.Composable

@Composable
actual fun filePicker(onResult: (String?) -> Unit) =
    FilePicker {
        onResult(null)
    }
