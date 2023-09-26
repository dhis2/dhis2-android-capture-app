package org.dhis2.ui.dialogs.signature

import android.view.MotionEvent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInteropFilter

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SignatureCanvas(modifier: Modifier = Modifier, drawing: MutableState<Offset?>) {
    val path by remember { mutableStateOf(Path()) }
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInteropFilter {
                when (it.action) {
                    MotionEvent.ACTION_DOWN -> {
                        drawing.value = Offset(it.x, it.y)
                        path.moveTo(it.x, it.y)
                    }
                    MotionEvent.ACTION_MOVE -> {
                        drawing.value = Offset(it.x, it.y)
                        path.lineTo(it.x, it.y)
                    }
                }
                true
            }.graphicsLayer { clip = true },
    ) {
        drawing.value?.let {
            drawPath(
                path = path,
                color = Color.Black,
                alpha = 1f,
                style = Stroke(7f),
            )
        } ?: path.reset()
    }
}
