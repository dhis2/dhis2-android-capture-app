package org.dhis2.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType.Companion.NonZero
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap.Companion.Butt
import androidx.compose.ui.graphics.StrokeJoin.Companion.Miter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.ImageVector.Builder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val DHIS2Icons.DataEntryFilled: ImageVector
    get() {
        if (dataEntryFilled != null) {
            return dataEntryFilled!!
        }
        dataEntryFilled =
            Builder(
                name = "DataEntryFilled",
                defaultWidth = 24.0.dp,
                defaultHeight =
                    24.0.dp,
                viewportWidth = 24.0f,
                viewportHeight = 24.0f,
            ).apply {
                path(
                    fill = SolidColor(Color(0xFF000000)),
                    stroke = null,
                    strokeLineWidth = 0.0f,
                    strokeLineCap = Butt,
                    strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f,
                    pathFillType = NonZero,
                ) {
                    moveTo(4.6f, 2.04f)
                    curveTo(4.73f, 2.01f, 4.86f, 2.0f, 5.0f, 2.0f)
                    horizontalLineTo(12.0f)
                    verticalLineTo(4.0f)
                    horizontalLineTo(5.0f)
                    verticalLineTo(20.0f)
                    horizontalLineTo(19.0f)
                    verticalLineTo(12.0f)
                    horizontalLineTo(21.0f)
                    verticalLineTo(20.0f)
                    curveTo(21.0f, 21.1f, 20.1f, 22.0f, 19.0f, 22.0f)
                    horizontalLineTo(5.0f)
                    curveTo(4.86f, 22.0f, 4.73f, 21.99f, 4.6f, 21.97f)
                    curveTo(4.21f, 21.89f, 3.86f, 21.69f, 3.59f, 21.42f)
                    curveTo(3.41f, 21.23f, 3.26f, 21.02f, 3.16f, 20.78f)
                    curveTo(3.06f, 20.54f, 3.0f, 20.27f, 3.0f, 20.0f)
                    verticalLineTo(4.0f)
                    curveTo(3.0f, 3.72f, 3.06f, 3.46f, 3.16f, 3.23f)
                    curveTo(3.26f, 2.99f, 3.41f, 2.77f, 3.59f, 2.59f)
                    curveTo(3.86f, 2.32f, 4.21f, 2.12f, 4.6f, 2.04f)
                    close()
                }
                path(
                    fill = SolidColor(Color(0xFF000000)),
                    stroke = null,
                    strokeLineWidth = 0.0f,
                    strokeLineCap = Butt,
                    strokeLineJoin = Miter,
                    strokeLineMiter = 4.0f,
                    pathFillType = NonZero,
                ) {
                    moveTo(17.5809f, 3.0977f)
                    lineTo(18.3583f, 2.3203f)
                    curveTo(18.7854f, 1.8932f, 19.4752f, 1.8932f, 19.9023f, 2.3203f)
                    lineTo(20.6797f, 3.0977f)
                    curveTo(21.1068f, 3.5248f, 21.1068f, 4.2146f, 20.6797f, 4.6417f)
                    lineTo(19.9023f, 5.4191f)
                    lineTo(17.5809f, 3.0977f)
                    close()
                    moveTo(16.8034f, 3.8752f)
                    lineTo(11.0f, 9.6786f)
                    verticalLineTo(12.0f)
                    horizontalLineTo(13.3214f)
                    lineTo(19.1248f, 6.1966f)
                    lineTo(16.8034f, 3.8752f)
                    close()
                }
            }.build()
        return dataEntryFilled!!
    }

private var dataEntryFilled: ImageVector? = null
