package dhis2.org.analytics.charts.providers

import dhis2.org.analytics.charts.data.GraphPoint
import dhis2.org.analytics.charts.data.NutritionChartType
import dhis2.org.analytics.charts.data.SerieData
import java.util.Date
import org.hisp.dhis.rules.functions.ZScoreTable

class RuleEngineNutritionDataProviderImpl : NutritionDataProvider {
    override fun getNutritionData(nutritionChartType: NutritionChartType): List<SerieData> {
        val (zscoreTable, genderByte) = when (nutritionChartType) {
            NutritionChartType.WHO_WFA_BOY -> Pair(ZScoreTable.getZscoreWFATableBoy(), 0)
            NutritionChartType.WHO_WFA_GIRL -> Pair(ZScoreTable.getZscoreWFATableGirl(), 1)
            NutritionChartType.WHO_HFA_BOY -> Pair(ZScoreTable.getZscoreHFATableBoy(), 0)
            NutritionChartType.WHO_HFA_GIRL -> Pair(ZScoreTable.getZscoreHFATableGirl(), 1)
            NutritionChartType.WHO_WFH_BOY -> Pair(ZScoreTable.getZscoreWFHTableBoy(), 0)
            NutritionChartType.WHO_WHO_WFH_GIRL -> Pair(ZScoreTable.getZscoreWFHTableGirl(), 1)
        }

        val numberOfData = zscoreTable.values.first().size
        val nutritionData = mutableListOf<MutableList<GraphPoint>>().apply {
            for (i in 0 until numberOfData) {
                add(mutableListOf())
            }
        }

        zscoreTable.toSortedMap(compareBy { it.parameter })
            .values.forEachIndexed { i, map ->
                val values = map.keys.sorted()
                for (dataIndex in 0 until numberOfData) {
                    nutritionData[dataIndex].add(
                        GraphPoint(
                            eventDate = Date(),
                            position = i,
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
