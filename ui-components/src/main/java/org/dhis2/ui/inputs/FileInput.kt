package org.dhis2.ui.inputs

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import java.io.File
import org.dhis2.ui.IconTextButton
import org.dhis2.ui.R
import org.dhis2.ui.theme.defaultFontFamily

@Composable
fun BoxedInput(
    leadingIcon: @Composable (modifier: Modifier) -> Unit,
    trailingIcons: @Composable RowScope.() -> Unit,
    content: @Composable (modifier: Modifier) -> Unit
) {
    Surface(
        modifier = Modifier
            .wrapContentSize(),
        shape = RoundedCornerShape(6.dp),
        color = Color.White,
        elevation = 4.dp
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(8.dp)
        ) {
            val (leadingIconRef, contentRef, trailingIconsRef) = createRefs()
            leadingIcon(
                modifier = Modifier.constrainAs(leadingIconRef) {
                    start.linkTo(parent.start)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    height = Dimension.wrapContent
                }
            )
            content(
                modifier = Modifier.constrainAs(contentRef) {
                    start.linkTo(leadingIconRef.end, 8.dp)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(trailingIconsRef.start, 8.dp)
                    height = Dimension.fillToConstraints
                    width = Dimension.fillToConstraints
                }
            )
            Row(
                modifier = Modifier.constrainAs(trailingIconsRef) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                    height = Dimension.wrapContent
                }
            ) {
                trailingIcons()
            }
        }
    }
}

@Composable
fun FileDescription(modifier: Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "file_name.extension",
            style = TextStyle(
                color = Color.Black.copy(alpha = 0.87f),
                fontSize = 12.sp,
                fontFamily = defaultFontFamily,
                fontWeight = FontWeight(400),
                lineHeight = 20.sp
            )
        )
        Text(
            text = "1.2MB",
            style = TextStyle(
                color = Color.Black.copy(alpha = 0.38f),
                fontSize = 10.sp,
                fontWeight = FontWeight(400),
                fontFamily = defaultFontFamily,
                lineHeight = 12.sp
            )
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FileInput(
    file: File?,
    addFileLabel: String,
    onAddFile: () -> Unit = {},
    onDownloadClick: () -> Unit = {},
    onDeleteFile: () -> Unit = {}
) {
    var hasValue by remember { mutableStateOf(file != null) }

    AnimatedContent(
        targetState = hasValue,
        transitionSpec = {
            fadeIn(animationSpec = tween(150, 150)) with
                    fadeOut(animationSpec = tween(150)) using
                    SizeTransform { initialSize, targetSize ->
                        keyframes {
                            // Expand horizontally first.
                            IntSize(initialSize.width, targetSize.height) at 150
                            durationMillis = 300
                        }
                    }
        }
    ) { targetHasValue ->
        if (targetHasValue) {
            FileInputWithValue(
                onDownloadClick = onDownloadClick,
                onDeleteFile = {
                    onDeleteFile()
                    hasValue = false
                }
            )
        } else {
            FileInputWithoutValue(
                modifier = Modifier.fillMaxWidth(),
                label = addFileLabel,
                onAddFile = {
                    onAddFile()
                    hasValue = true
                }
            )
        }
    }
}

@Composable
fun FileInputWithoutValue(modifier: Modifier, label: String, onAddFile: () -> Unit) {
    IconTextButton(
        modifier = modifier,
        onClick = onAddFile,
        painter = painterResource(id = R.drawable.ic_file),
        text = label
    )
}

@Composable
fun FileInputWithValue(
    onDownloadClick: () -> Unit,
    onDeleteFile: () -> Unit
) {
    BoxedInput(
        leadingIcon = { modifier ->
            Box(
                modifier = modifier
                    .size(LocalViewConfiguration.current.minimumTouchTargetSize),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_file),
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        trailingIcons = {
            IconButton(onClick = onDownloadClick) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_file_download),
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = { onDeleteFile() }) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_delete),
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) { modifier ->
        FileDescription(modifier = modifier)
    }
}

@Composable
@Preview
fun FileInputTest() {
    FileInput("addFile")
}