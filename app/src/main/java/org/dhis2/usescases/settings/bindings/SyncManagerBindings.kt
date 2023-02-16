package org.dhis2.usescases.settings.bindings

import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.databinding.BindingAdapter
import org.dhis2.R
import org.dhis2.usescases.settings.models.SyncButtonUIModel

@BindingAdapter("addSyncButton")
fun ComposeView.addSyncButton(syncButton: SyncButtonUIModel?) {
    syncButton?.let {
        setContent {
            TextButton(
                onClick = it.onClick,
                enabled = it.enabled
            ) {
                Text(
                    text = it.text.uppercase(),
                    color = colorResource(id = R.color.colorPrimary),
                    fontSize = 12.sp,
                    fontFamily = FontFamily(Font(R.font.rubik_medium))
                )
            }
        }
    }
}
