package org.dhis2.ui.icons.imagevectors

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview

@Preview
@Composable
private fun VectorPreview() {
    Image(Icons.Filled.Form, null)
}

public val Icons.Filled.Form: ImageVector
    get() {
        if (_form != null) {
            return _form!!
        }
        _form = materialIcon(
            name = "NavigationForms",
        ) {
            materialPath {
                moveTo(3.0098f, 20f)
                curveTo(3.0098f, 21.1f, 3.89f, 22f, 5f, 22f)
                horizontalLineTo(19f)
                curveTo(20.1f, 22f, 21f, 21.1f, 21f, 20f)
                verticalLineTo(10f)
                horizontalLineTo(19f)
                verticalLineTo(20f)
                horizontalLineTo(5f)
                verticalLineTo(4f)
                horizontalLineTo(12f)
                verticalLineTo(2f)
                horizontalLineTo(5f)
                curveTo(3.89f, 2f, 3f, 2.9f, 3f, 4f)
                lineTo(3.0098f, 20f)
                close()
            }
            materialPath {
                moveTo(10f, 10.7087f)
                verticalLineTo(13f)
                horizontalLineTo(12.2913f)
                lineTo(19.0493f, 6.2421f)
                lineTo(16.758f, 3.9507f)
                lineTo(10f, 10.7087f)
                close()
                moveTo(20.8213f, 4.4701f)
                curveTo(21.0596f, 4.2318f, 21.0596f, 3.8468f, 20.8213f, 3.6085f)
                lineTo(19.3915f, 2.1787f)
                curveTo(19.1532f, 1.9404f, 18.7682f, 1.9404f, 18.5299f, 2.1787f)
                lineTo(17.4117f, 3.2969f)
                lineTo(19.7031f, 5.5883f)
                lineTo(20.8213f, 4.4701f)
                close()
            }
        }
        return _form!!
    }

private var _form: ImageVector? = null
