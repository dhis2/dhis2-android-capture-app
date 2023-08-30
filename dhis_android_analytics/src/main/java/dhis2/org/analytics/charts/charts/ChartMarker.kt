package dhis2.org.analytics.charts.charts

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.ViewPortHandler
import dhis2.org.R
import org.dhis2.commons.bindings.dp
import kotlin.math.round

@SuppressLint("ViewConstructor")
class ChartMarker(
    context: Context,
    private val viewPort: ViewPortHandler,
    private val xAxis: XAxis,
    private val yAxis: YAxis,
    @LayoutRes layoutRes: Int = R.layout.chart_marker,
    private val forceTopMarkerPlacement: Boolean = false,
) :
    MarkerView(context, layoutRes) {
    private enum class MarkerPlacement {
        TOP_RIGHT, TOP_LEFT, BOTTOM_RIGHT, BOTTOM_LEFT
    }

    private val contentX = findViewById<TextView>(R.id.chart_marker_content_x)
    private val contentY = findViewById<TextView>(R.id.chart_marker_content_y)
    private var markerPlacement: MarkerPlacement? = null
    private val markerVerticalPadding = if (forceTopMarkerPlacement) {
        0.dp
    } else {
        8.dp
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        highlight?.let { markerPlacement(highlight.xPx, highlight.yPx) }
        val formattedXValue = xAxis.valueFormatter.getAxisLabel(round(e?.x ?: 0f), xAxis)
        val formattedYValue = yAxis.valueFormatter.getAxisLabel(e?.y ?: 0f, yAxis)
        contentX.text = formattedXValue
        contentY.text = formattedYValue
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return when (markerPlacement) {
            MarkerPlacement.TOP_RIGHT ->
                MPPointF(-width.toFloat() / 2f, -height.toFloat() - markerVerticalPadding)
            MarkerPlacement.TOP_LEFT ->
                MPPointF(-width.toFloat() / 2f, -height.toFloat() - markerVerticalPadding)
            MarkerPlacement.BOTTOM_RIGHT ->
                MPPointF(-width.toFloat() / 2f, markerVerticalPadding.toFloat())
            MarkerPlacement.BOTTOM_LEFT ->
                MPPointF(-width.toFloat() / 2f, markerVerticalPadding.toFloat())
            null -> MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
        }
    }

    private fun markerPlacement(xEntryPx: Float, yEntryPx: Float) {
        val contentCenterX = viewPort.contentCenter.x
        val contentCenterY = viewPort.contentCenter.y
        markerPlacement = when {
            forceTopMarkerPlacement -> MarkerPlacement.TOP_RIGHT
            xEntryPx <= contentCenterX && yEntryPx >= contentCenterY -> MarkerPlacement.TOP_RIGHT
            xEntryPx < contentCenterX && yEntryPx < contentCenterY -> MarkerPlacement.BOTTOM_RIGHT
            xEntryPx > contentCenterX && yEntryPx > contentCenterY -> MarkerPlacement.TOP_LEFT
            xEntryPx >= contentCenterX && yEntryPx <= contentCenterY -> MarkerPlacement.BOTTOM_LEFT
            else -> MarkerPlacement.TOP_RIGHT
        }
    }
}
