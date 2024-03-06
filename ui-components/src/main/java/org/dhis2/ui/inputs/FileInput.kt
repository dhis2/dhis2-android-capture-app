package org.dhis2.ui.inputs

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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.graphics.toColorInt
import org.dhis2.ui.IconTextButton
import org.dhis2.ui.R
import org.dhis2.ui.model.InputData
import org.dhis2.ui.theme.defaultFontFamily

@Composable
fun BoxedInput(
    leadingIcon: @Composable
    (modifier: Modifier) -> Unit,
    trailingIcons: @Composable
    RowScope.() -> Unit,
    content: @Composable
    (modifier: Modifier) -> Unit
) {
    Surface(
        modifier = Modifier
            .wrapContentSize(),
        shape = RoundedCornerShape(6.dp),
        color = Color.White,
        shadowElevation = 4.dp
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
fun FileDescription(modifier: Modifier, fileInputData: InputData.FileInputData) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = fileInputData.fileName,
            style = TextStyle(
                color = Color.Black.copy(alpha = 0.87f),
                fontSize = 12.sp,
                fontFamily = defaultFontFamily,
                fontWeight = FontWeight(400),
                lineHeight = 20.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = fileInputData.fileSizeLabel,
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

@Composable
fun FileInput(
    fileInputData: InputData.FileInputData?,
    addFileLabel: String,
    enabled: Boolean = true,
    onAddFile: () -> Unit = {},
    onDownloadClick: () -> Unit = {},
    onDeleteFile: () -> Unit = {}
) {
    if (fileInputData != null) {
        FileInputWithValue(
            fileInputData = fileInputData,
            enabled = enabled,
            onDownloadClick = onDownloadClick,
            onDeleteFile = onDeleteFile

        )
    } else {
        FileInputWithoutValue(
            modifier = Modifier.fillMaxWidth(),
            label = addFileLabel,
            enabled = enabled,
            onAddFile = onAddFile
        )
    }
}

@Composable
fun FileInputWithoutValue(
    modifier: Modifier,
    label: String,
    enabled: Boolean,
    onAddFile: () -> Unit
) {
    IconTextButton(
        modifier = modifier,
        enabled = enabled,
        onClick = onAddFile,
        painter = painterResource(id = R.drawable.ic_file),
        text = label
    )
}

@Composable
fun FileInputWithValue(
    fileInputData: InputData.FileInputData,
    enabled: Boolean,
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
            IconButton(enabled = enabled, onClick = onDownloadClick) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_file_download),
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(enabled = enabled, onClick = { onDeleteFile() }) {
                Icon(
                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_delete),
                    contentDescription = "",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    ) { modifier ->
        FileDescription(modifier = modifier, fileInputData = fileInputData)
    }
}

@Composable
@Preview
fun FileWithoutValueInputTest() {
    FileInput(fileInputData = null, addFileLabel = "addFile")
}

@Composable
@Preview
fun FileWithValueInputTest() {
    FileInput(fileInputData = null, addFileLabel = "addFile")
}

@Composable
@Preview
fun FileInputWithMessageTest() {
    FormInputBox(
        labelText = "This is the label",
        helperText = "This is a messsage",
        descriptionText = "This is a description",
        selected = true,
        labelTextColor = Color.Black.copy(alpha = 0.54f),
        helperTextColor = Color("#E91E63".toColorInt())
    ) {
        FileInput(
            fileInputData = InputData.FileInputData(
                fileName = "file.txt",
                fileSize = 1234,
                filePath = "/file.txt"
            ),
            addFileLabel = "addFile"
        )
    }
}

@Composable
@Preview
fun FileInputNoValueWithMessageTest() {
    FormInputBox(
        labelText = "This is the label",
        helperText = "This is a messsage",
        descriptionText = "This is a description",
        selected = true,
        labelTextColor = Color.Black.copy(alpha = 0.54f),
        helperTextColor = Color("#E91E63".toColorInt())
    ) {
        FileInput(
            fileInputData = null,
            addFileLabel = "addFile"
        )
    }
}
