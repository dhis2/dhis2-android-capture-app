package org.dhis2.ui.buttons

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import org.dhis2.ui.model.ButtonUiModel

@Composable
fun Dhis2TextButton(model: ButtonUiModel) {
    TextButton(
        onClick = model.onClick,
        enabled = model.enabled
    ) {
        Text(
            text = model.text,
            fontSize = 12.sp
        )
    }
}
