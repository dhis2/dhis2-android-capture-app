package org.dhis2.usescases.settings.bindings

import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.ui.platform.ComposeView
import androidx.databinding.BindingAdapter

@BindingAdapter("addSyncButton")
fun ComposeView.addSyncButton(onClick: () -> Unit) {
    setContent {
        TextButton(onClick = onClick) {
            Text(text = "Sync data now")
        }
    }
}