package org.dhis2.mobile.login.main.ui.contracts

import androidx.compose.runtime.Composable

@Composable
expect fun serverQrReader(onResult: (String?) -> Unit): ServerQRReader

interface ServerQRReader {
    fun launch()
}
