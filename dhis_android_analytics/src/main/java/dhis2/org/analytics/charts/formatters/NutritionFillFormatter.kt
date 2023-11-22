package dhis2.org.analytics.charts.formatters

import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet

class NutritionFillFormatter(
    private val boundaryDataSet: ILineDataSet?,
) : IFillFormatter {

    override fun getFillLinePosition(
        dataSet: ILineDataSet?,
        dataProvider: LineDataProvider?,
    ): Float {
        return 0f
    }

    fun getFillLineBoundary(): List<Entry> {
        return if (boundaryDataSet != null) {
            (boundaryDataSet as LineDataSet).values
        } else {
            emptyList()
        }
    }
}
