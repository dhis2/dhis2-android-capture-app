package org.dhis2.ui.inputs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dhis2.ui.R
import org.dhis2.ui.dialogs.alert.DescriptionDialog

@Composable
fun FormInputBox(
    labelText: String?,
    helperText: String? = null,
    descriptionText: String? = null,
    selected: Boolean = false,
    enabled: Boolean = true,
    labelTextColor: Color,
    helperTextColor: Color = Color.Black.copy(alpha = 0.38f),
    content: @Composable
    () -> Unit,
) {
    val openDescriptionDialog = remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .wrapContentHeight()
            .alpha(1.0f.takeIf { enabled } ?: 0.5f),
    ) {
        Column(
            modifier = Modifier
                .wrapContentHeight()
                .padding(
                    top = 9.dp,
                    bottom = 16.dp,
                )
                .drawInputSelector(
                    selected = selected,
                    color = MaterialTheme.colorScheme.primary,
                )
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                )
                .graphicsLayer { clip = false },
            verticalArrangement = spacedBy(9.dp),
        ) {
            labelText?.let {
                Row(
                    modifier = Modifier,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = spacedBy(8.dp),
                ) {
                    HelperText(
                        helperText = labelText,
                        textStyle = TextStyle(
                            color = labelTextColor,
                            fontSize = 10.sp,
                            lineHeight = 10.sp,
                        ),
                    )
                    descriptionText?.let {
                        Icon(
                            modifier = Modifier
                                .size(10.dp)
                                .clickable { openDescriptionDialog.value = true },
                            imageVector = ImageVector.vectorResource(id = R.drawable.ic_input_info),
                            contentDescription = "",
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
            content()
            helperText?.let {
                HelperText(
                    helperText = helperText,
                    textStyle = TextStyle(
                        color = helperTextColor,
                        fontSize = 10.sp,
                        lineHeight = 12.sp,
                    ),
                )
            }
        }

        if (openDescriptionDialog.value) {
            DescriptionDialog(labelText!!, descriptionText!!) {
                openDescriptionDialog.value = false
            }
        }
    }
}

@Composable
fun HelperText(helperText: String, textStyle: TextStyle) {
    Text(
        text = helperText,
        style = textStyle,
    )
}

fun Modifier.drawInputSelector(selected: Boolean, color: Color) = when (selected) {
    true -> this.then(
        drawBehind {
            drawPath(
                Path().apply {
                    addRoundRect(
                        RoundRect(
                            rect = Rect(
                                offset = Offset(8.dp.toPx(), 0f),
                                size = Size(2.dp.toPx(), size.height),
                            ),
                            topLeft = CornerRadius(10f, 10f),
                            topRight = CornerRadius(10f, 10f),
                            bottomLeft = CornerRadius(10f, 10f),
                            bottomRight = CornerRadius(10f, 10f),
                        ),
                    )
                },
                color = color,
            )
        },
    )
    else -> this
}
