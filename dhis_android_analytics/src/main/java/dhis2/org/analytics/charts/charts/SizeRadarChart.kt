package dhis2.org.analytics.charts.charts

import android.content.Context
import com.github.mikephil.charting.charts.RadarChart
import dhis2.org.analytics.charts.renderers.RadarRenderer

class SizeRadarChart(context: Context) : RadarChart(context) {
    override fun init() {
        super.init()
        this.mXAxisRenderer = RadarRenderer(mViewPortHandler, mXAxis, this)
    }
}
