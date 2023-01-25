package org.dhis2.ui.inputs

import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

enum class HelperTextStyle {
    NORMAL,
    ERROR
}

@Composable
fun HelperTextBox(
    helperText: String,
    helperTextStyle: HelperTextStyle,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier,
        verticalArrangement = spacedBy(2.dp)
    ) {
        content()
        HelperText(
            helperText = helperText,
            textStyle = when(helperTextStyle){
                HelperTextStyle.NORMAL -> TextStyle()
                HelperTextStyle.ERROR -> TextStyle()
            }
        )
    }
}

@Composable
fun HelperText(helperText: String, textStyle: TextStyle) {
    Text(
        text = helperText,
        style = textStyle
    )
}