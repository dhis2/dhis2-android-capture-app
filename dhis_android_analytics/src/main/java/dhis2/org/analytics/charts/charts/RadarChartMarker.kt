package dhis2.org.analytics.charts.charts

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import androidx.annotation.LayoutRes
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import dhis2.org.R
import org.dhis2.commons.bindings.dp

@SuppressLint("ViewConstructor")
class RadarChartMarker(
    context: Context,
    private val yAxis: YAxis,
    @LayoutRes layoutRes: Int = R.layout.chart_marker,
    forceTopMarkerPlacement: Boolean = false,
) : MarkerView(context, layoutRes) {
    private enum class MarkerPlacement {
        TOP_RIGHT, TOP_LEFT, BOTTOM_RIGHT, BOTTOM_LEFT
    }

    private val contentY = findViewById<TextView>(R.id.chart_marker_content_y)
    private var markerPlacement: MarkerPlacement? = null
    private val markerVerticalPadding = if (forceTopMarkerPlacement) {
        0.dp
    } else {
        8.dp
    }

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        val formattedYValue = yAxis.valueFormatter.getAxisLabel(e?.y ?: 0f, yAxis)
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
}
