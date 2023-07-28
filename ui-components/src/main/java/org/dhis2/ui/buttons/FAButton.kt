package org.dhis2.ui.buttons

import androidx.annotation.StringRes
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import org.dhis2.ui.R

@Composable
fun FAButton(
    modifier: Modifier = Modifier,
    @StringRes text: Int,
    contentColor: Color,
    containerColor: Color,
    expanded: Boolean = true,
    icon: @Composable
    () -> Unit,
    onClick: () -> Unit
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        expanded = expanded,
        icon = icon,
        text = { Text(text = stringResource(text)) },
        contentColor = contentColor,
        containerColor = containerColor
    )
}

@Preview
@Composable
fun ExtendedFAButtonPreview() {
    FAButton(
        modifier = Modifier,
        text = R.string.button_extended,
        contentColor = Color.DarkGray,
        containerColor = Color.LightGray,
        expanded = true,
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_home_positive),
                contentDescription = null
            )
        }
    ) {
    }
}
