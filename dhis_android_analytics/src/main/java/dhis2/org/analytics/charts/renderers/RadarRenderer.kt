package dhis2.org.analytics.charts.renderers

import android.graphics.Canvas
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.renderer.XAxisRendererRadarChart
import com.github.mikephil.charting.utils.FSize
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler
import kotlin.math.roundToInt

class RadarRenderer(
    viewPortHandler: ViewPortHandler?,
    xAxis: XAxis?,
    chart: RadarChart?,
) : XAxisRendererRadarChart(viewPortHandler, xAxis, chart) {
    override fun drawLabels(c: Canvas, pos: Float, anchor: MPPointF) {
        super.drawLabels(c, pos, anchor)
    }

    override fun computeSize() {
        var longest = mXAxis.longestLabel
        val line = lines(longest)
        if (line.size > 1) {
            longest = line[0]
        }
        mAxisLabelPaint.typeface = mXAxis.typeface
        mAxisLabelPaint.textSize = mXAxis.textSize
        val labelSize = Utils.calcTextSize(mAxisLabelPaint, longest)
        val labelWidth = labelSize.width
        val labelHeight = Utils.calcTextHeight(mAxisLabelPaint, "Q").toFloat()
        val labelRotatedSize = Utils.getSizeOfRotatedRectangleByDegrees(
            labelWidth,
            labelHeight,
            mXAxis.labelRotationAngle,
        )
        mXAxis.mLabelWidth = labelWidth.roundToInt()
        mXAxis.mLabelHeight = labelHeight.roundToInt()
        mXAxis.mLabelRotatedWidth = labelRotatedSize.width.roundToInt()
        mXAxis.mLabelRotatedHeight = labelRotatedSize.height.roundToInt()
        FSize.recycleInstance(labelRotatedSize)
        FSize.recycleInstance(labelSize)
    }

    override fun drawLabel(
        c: Canvas,
        formattedLabel: String,
        x: Float,
        y: Float,
        anchor: MPPointF,
        angleDegrees: Float,
    ) {
        val line = lines(formattedLabel)
        if (line.size > 1) {
            Utils.drawXAxisValue(
                c,
                line[0],
                x,
                y,
                mAxisLabelPaint,
                anchor,
                angleDegrees,
            )
            Utils.drawXAxisValue(
                c,
                line[1],
                x,
                y + mAxisLabelPaint.textSize,
                mAxisLabelPaint,
                anchor,
                angleDegrees,
            )
        } else {
            super.drawLabel(c, formattedLabel, x, y, anchor, angleDegrees)
        }
    }

    private fun lines(label: String): Array<String> {
        val blankIndex =
            label.toList().mapIndexedNotNull { index, c -> if (c == ' ') index else null }
        val totalLenght = label.length

        return if (blankIndex.isEmpty()) {
            arrayOf(label)
        } else {
            val splitIndex = blankIndex.lastOrNull {
                it * 2 <= totalLenght
            } ?: blankIndex.first()
            arrayOf(label.substring(0, splitIndex), label.substring(splitIndex + 1, label.length))
        }
    }
}
