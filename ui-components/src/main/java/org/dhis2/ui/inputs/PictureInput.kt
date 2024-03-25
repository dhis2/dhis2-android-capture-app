package org.dhis2.ui.inputs

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.dhis2.ui.theme.errorColor
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.IconButton
import org.hisp.dhis.mobile.ui.designsystem.resource.provideDHIS2Icon

@Composable
fun PictureInput(
    imageValue: Bitmap?,
    enabled: Boolean = true,
    addButtonData: AddButtonData,
    onClick: () -> Unit,
    onClear: () -> Unit,
) {
    if (imageValue != null) {
        Picture(imageValue.asImageBitmap(), enabled, onClick, onClear)
    } else {
        Button(
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            text = addButtonData.label,
            icon = {
                Icon(
                    painter = addButtonData.icon,
                    contentDescription = "",
                )
            },
            onClick = addButtonData.onClick,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Picture(image: ImageBitmap, enabled: Boolean, onClick: () -> Unit, onClear: () -> Unit) {
    Box {
        Surface(
            onClick = onClick,
            shadowElevation = 4.dp,
            shape = RoundedCornerShape(6.dp),
        ) {
            Image(
                modifier = Modifier.defaultMinSize(
                    minWidth = if (image.width >= image.height) 200.dp else 0.dp,
                    minHeight = if (image.width < image.height) 200.dp else 0.dp,
                ),
                bitmap = image,
                contentScale = ContentScale.Crop,
                contentDescription = "picture",
            )
        }
        if (enabled) {
            IconButton(
                modifier = Modifier
                    .padding(8.dp)
                    .background(Color.White, CircleShape)
                    .size(40.dp)
                    .align(Alignment.TopEnd),
                onClick = onClear,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "clear",
                        tint = errorColor,
                    )
                },
            )
        }
    }
}

@Preview
@Composable
fun IconClear() {
    Icon(
        imageVector = Icons.Default.Clear,
        contentDescription = "clear",
        tint = errorColor,
    )
}

@Preview
@Composable
fun IconClearUI() {
    Icon(
        painter = provideDHIS2Icon(resourceName = "dhis2_microscope_outline"),
        contentDescription = "clear",
        tint = errorColor,
    )
}

data class AddButtonData(
    val icon: Painter,
    val label: String,
    val onClick: () -> Unit,
)
