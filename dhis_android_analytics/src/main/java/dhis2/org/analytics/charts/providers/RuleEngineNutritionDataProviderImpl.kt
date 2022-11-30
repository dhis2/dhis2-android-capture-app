package dhis2.org.analytics.charts.providers

import dhis2.org.analytics.charts.data.GraphPoint
import dhis2.org.analytics.charts.data.NutritionChartType
import dhis2.org.analytics.charts.data.SerieData
import java.util.GregorianCalendar
import org.hisp.dhis.rules.functions.ZScoreTable

class RuleEngineNutritionDataProviderImpl : NutritionDataProvider {
    override fun getNutritionData(nutritionChartType: NutritionChartType): List<SerieData> {
        val zscoreTable = when (nutritionChartType) {
            NutritionChartType.WHO_WFA_BOY -> ZScoreTable.getZscoreWFATableBoy()
            NutritionChartType.WHO_WFA_GIRL -> ZScoreTable.getZscoreWFATableGirl()
            NutritionChartType.WHO_HFA_BOY -> ZScoreTable.getZscoreHFATableBoy()
            NutritionChartType.WHO_HFA_GIRL -> ZScoreTable.getZscoreHFATableGirl()
            NutritionChartType.WHO_WFH_BOY -> ZScoreTable.getZscoreWFHTableBoy()
            NutritionChartType.WHO_WHO_WFH_GIRL -> ZScoreTable.getZscoreWFHTableGirl()
        }

        val numberOfData = zscoreTable.values.first().size
        val nutritionData = mutableListOf<MutableList<GraphPoint>>().apply {
            for (i in 0 until numberOfData) {
                add(mutableListOf())
            }
        }

        zscoreTable.toSortedMap(compareBy { it.parameter }).forEach {
            val parameter = it.key.parameter
            val values = it.value.keys.sorted()
            for (dataIndex in 0 until numberOfData) {
                nutritionData[dataIndex].add(
                    GraphPoint(
                        eventDate = GregorianCalendar(2021, 0, 1).time,
                        position = parameter,
                        fieldValue = values[dataIndex]
                    )
                )
            }
        }

        return nutritionData.map {
            SerieData("", it)
        }
    }
}
