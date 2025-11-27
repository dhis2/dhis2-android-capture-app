package org.dhis2.mobile.login.main.ui.contracts

import androidx.compose.runtime.Composable

@Composable
actual fun serverQrReader(onResult: (String?) -> Unit): ServerQRReader =
    object : ServerQRReader {
        override fun launch() {
            onResult(null)
        }
    }
