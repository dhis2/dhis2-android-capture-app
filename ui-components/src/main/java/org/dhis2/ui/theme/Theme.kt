package org.dhis2.ui.theme

import androidx.compose.runtime.Composable
import com.google.accompanist.themeadapter.material3.Mdc3Theme

@Composable
fun Dhis2Theme(content: @Composable () -> Unit) {
    Mdc3Theme(content = content)
}
