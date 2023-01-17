package org.dhis2.ui.buttons

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.dhis2.ui.R

@Composable
fun FAButton(
    modifier: Modifier,
    uiModel: FAButtonUiModel,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier
            .defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
            .apply {
                if (uiModel.extended) {
                    wrapContentWidth()
                } else {
                    widthIn(56.dp)
                }
            }
            .height(56.dp),
        contentPadding = PaddingValues(16.dp),
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.elevation(),
        enabled = uiModel.enabled
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = uiModel.icon),
            contentDescription = stringResource(uiModel.text),
            tint = if (uiModel.enabled) {
                uiModel.iconTint
            } else {
                colorResource(id = R.color.black).copy(alpha = ContentAlpha.disabled)
            }
        )
        AnimatedVisibility(visible = uiModel.extended) {
            Row {
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = stringResource(uiModel.text),
                    color = if (uiModel.enabled) {
                        uiModel.textColor
                    } else {
                        colorResource(id = R.color.black).copy(alpha = ContentAlpha.disabled)
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun ExtendedFAButtonPreview() {
    FAButton(
        modifier = Modifier,
        uiModel = FAButtonUiModel(
            text = R.string.button_extended,
            textColor = Color.Blue,
            icon = R.drawable.ic_home_positive,
            iconTint = Color.Green,
            extended = true
        )
    ) {
    }
}
