package org.dhis2.usescases.settings.bindings

import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.databinding.BindingAdapter
import org.dhis2.R
import org.dhis2.ui.theme.colorPrimary

@BindingAdapter("addSyncButton")
fun ComposeView.addSyncButton(onClick: () -> Unit) {
    setContent {
        TextButton(onClick = onClick) {
            Text(
                text = "Sync data now".uppercase(),
                color = colorPrimary,
                fontSize = 12.sp,
                fontFamily = FontFamily(Font(R.font.rubik_medium))
            )
        }
    }
}