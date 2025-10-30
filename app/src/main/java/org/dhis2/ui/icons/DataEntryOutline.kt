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

val DHIS2Icons.DataEntryOutline: ImageVector
    get() {
        if (dataEntryOutline != null) {
            return dataEntryOutline!!
        }
        dataEntryOutline =
            Builder(
                name = "DataEntryOutline",
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
                    moveTo(18.4257f, 5.5504f)
                    lineTo(17.4524f, 4.577f)
                    lineTo(12.4037f, 9.6257f)
                    verticalLineTo(10.6082f)
                    horizontalLineTo(13.3861f)
                    lineTo(18.4257f, 5.5504f)
                    close()
                    moveTo(17.6252f, 2.4029f)
                    curveTo(17.7513f, 2.2752f, 17.9015f, 2.1739f, 18.067f, 2.1048f)
                    curveTo(18.2325f, 2.0356f, 18.4101f, 2.0f, 18.5895f, 2.0f)
                    curveTo(18.7689f, 2.0f, 18.9465f, 2.0356f, 19.112f, 2.1048f)
                    curveTo(19.2775f, 2.1739f, 19.4277f, 2.2752f, 19.5538f, 2.4029f)
                    lineTo(20.5999f, 3.449f)
                    curveTo(21.1366f, 3.9857f, 21.1366f, 4.8499f, 20.5999f, 5.3775f)
                    lineTo(13.9774f, 12.0f)
                    horizontalLineTo(10.9937f)
                    verticalLineTo(9.0344f)
                    lineTo(17.6252f, 2.4029f)
                    close()
                }
            }.build()
        return dataEntryOutline!!
    }

private var dataEntryOutline: ImageVector? = null
