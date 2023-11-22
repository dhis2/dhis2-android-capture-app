package org.dhis2.ui.dialogs.signature

import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dhis2.ui.R
import org.dhis2.ui.theme.textSecondary
import org.dhis2.ui.utils.dashedBorder
import kotlin.math.roundToInt

@ExperimentalComposeUiApi
@Composable
fun SignatureDialogUi(title: String, onSave: (Bitmap) -> Unit, onCancel: () -> Unit) {
    var capturingViewBounds: Rect? = null
    val view = LocalView.current

    var capturing by remember { mutableStateOf(false) }
    val drawing = remember { mutableStateOf<Offset?>(null) }
    val isSigned by remember { derivedStateOf { drawing.value != null } }

    Column(
        modifier = Modifier
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = title,
            style = TextStyle(
                fontSize = 10.sp,
                color = textSecondary,
            ),
        )
        Box {
            SignatureCanvas(
                modifier = Modifier
                    .height(200.dp)
                    .dashedBorder(
                        strokeWidth = 1.dp,
                        color = textSecondary.copy(alpha = 0.3f),
                        cornerRadiusDp = 8.dp,
                    )
                    .onGloballyPositioned {
                        capturingViewBounds = it.boundsInRoot()
                    },
                drawing = drawing,
            )
            if (!capturing) {
                Text(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopEnd)
                        .background(
                            MaterialTheme.colorScheme.primary,
                            RoundedCornerShape(6.dp, 6.dp, 6.dp, 0.dp),
                        )
                        .padding(8.dp, 4.dp),
                    text = stringResource(R.string.draw_here),
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
                if (isSigned) {
                    IconButton(
                        modifier = Modifier.align(Alignment.BottomEnd),
                        onClick = {
                            drawing.value = null
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary,
                        ),
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.clear),
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        ) {
            TextButton(
                onClick = onCancel,
            ) {
                Text(text = stringResource(R.string.cancel))
            }
            Button(
                onClick = {
                    capturing = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
                enabled = isSigned,
            ) {
                Text(text = stringResource(R.string.save))
            }
        }
    }

    LaunchedEffect(capturing) {
        capturingViewBounds
            ?.takeIf { capturing }
            ?.captureBitmap(view)
            ?.let { onSave(it) }
    }
}

fun Rect.captureBitmap(view: View): Bitmap? {
    val rect = deflate(2f)
    val imageBitmap = Bitmap.createBitmap(
        rect.width.roundToInt(),
        rect.height.roundToInt(),
        Bitmap.Config.ARGB_8888,
    )
    val canvas = Canvas(imageBitmap)
        .apply {
            translate(-rect.left, -rect.top)
        }
    view.draw(canvas)
    return imageBitmap
}

@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
fun PreviewSignatureUI() {
    SignatureDialogUi(title = "Form name", onSave = {}, onCancel = {})
}
