package dhis2.org.analytics.charts.providers

import dhis2.org.analytics.charts.data.GraphFieldValue
import dhis2.org.analytics.charts.data.GraphPoint
import dhis2.org.analytics.charts.data.NutritionChartType
import dhis2.org.analytics.charts.data.SerieData
import org.hisp.dhis.lib.expression.math.ZScoreTable
import java.util.GregorianCalendar

class RuleEngineNutritionDataProviderImpl : NutritionDataProvider {
    override fun getNutritionData(nutritionChartType: NutritionChartType): List<SerieData> {
        val zscoreTable = when (nutritionChartType) {
            NutritionChartType.WHO_WFA_BOY -> ZScoreTable.Z_SCORE_WFA_TABLE_BOY
            NutritionChartType.WHO_WFA_GIRL -> ZScoreTable.Z_SCORE_WFA_TABLE_GIRL
            NutritionChartType.WHO_HFA_BOY -> ZScoreTable.Z_SCORE_HFA_TABLE_BOY
            NutritionChartType.WHO_HFA_GIRL -> ZScoreTable.Z_SCORE_HFA_TABLE_GIRL
            NutritionChartType.WHO_WFH_BOY -> ZScoreTable.Z_SCORE_WFH_TABLE_BOY
            NutritionChartType.WHO_WHO_WFH_GIRL -> ZScoreTable.Z_SCORE_WFH_TABLE_GIRL
        }

        val numberOfData = zscoreTable.values.first().sdMap.size
        val nutritionData = mutableListOf<MutableList<GraphPoint>>().apply {
            for (i in 0 until numberOfData) {
                add(mutableListOf())
            }
        }

        zscoreTable.toSortedMap(compareBy { it.parameter }).forEach {
            val parameter = it.key.parameter
            val values = it.value.sdMap.keys.sorted()
            for (dataIndex in 0 until numberOfData) {
                nutritionData[dataIndex].add(
                    GraphPoint(
                        eventDate = GregorianCalendar(2021, 0, 1).time,
                        position = parameter,
                        fieldValue = GraphFieldValue.Numeric(values[dataIndex]),
                    ),
                )
            }
        }

        return nutritionData.map {
            SerieData("", it)
        }
    }
}
