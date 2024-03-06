package org.dhis2.ui.buttons

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dhis2.ui.model.ButtonUiModel

@Composable
fun Dhis2TextButton(
    modifier: Modifier = Modifier,
    model: ButtonUiModel,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    TextButton(
        modifier = modifier,
        onClick = model.onClick,
        enabled = model.enabled
    ) {
        leadingIcon?.let {
            it.invoke()
            Spacer(modifier = Modifier.size(8.dp))
        }
        Text(
            text = model.text,
            fontSize = 12.sp
        )
    }
}

@Composable
fun Dhis2Button(
    modifier: Modifier = Modifier,
    model: ButtonUiModel,
    leadingIcon: @Composable (() -> Unit)? = null
) {
    Button(
        modifier = modifier,
        onClick = model.onClick
    ) {
        leadingIcon?.let {
            it.invoke()
            Spacer(modifier = Modifier.size(8.dp))
        }
        Text(
            text = model.text,
            fontSize = 12.sp
        )
    }
}

@Preview
@Composable
fun Dhis2TextButtonPreview() {
    Dhis2TextButton(
        model = ButtonUiModel(
            text = "Action"
        ) {},
        leadingIcon = {
            Icon(imageVector = Icons.Default.Add, contentDescription = "")
        }
    )
}

@Preview
@Composable
fun Dhis2ButtonPreview() {
    Dhis2Button(
        model = ButtonUiModel(
            text = "Action"
        ) {},
        leadingIcon = {
            Icon(imageVector = Icons.Default.Add, contentDescription = "")
        }
    )
}
