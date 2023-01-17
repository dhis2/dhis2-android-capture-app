package org.dhis2.ui.buttons

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
    modifier: Modifier,
    uiModel: FAButtonUiModel,
    onClick: () -> Unit
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        expanded = uiModel.expanded,
        icon = {
            Icon(
                painter = painterResource(id = uiModel.icon),
                contentDescription = stringResource(uiModel.text)
            )
        },
        text = { Text(text = stringResource(uiModel.text)) },
        contentColor = uiModel.contentColor,
        containerColor = uiModel.containerColor
    )
}

@Preview
@Composable
fun ExtendedFAButtonPreview() {
    FAButton(
        modifier = Modifier,
        uiModel = FAButtonUiModel(
            text = R.string.button_extended,
            icon = R.drawable.ic_home_positive,
            contentColor = Color.DarkGray,
            containerColor = Color.LightGray,
            expanded = true
        )
    ) {
    }
}
