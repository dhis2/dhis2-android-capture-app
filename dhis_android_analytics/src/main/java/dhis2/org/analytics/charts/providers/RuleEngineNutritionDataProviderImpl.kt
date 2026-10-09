package dhis2.org.analytics.charts.providers

import dhis2.org.analytics.charts.data.GraphFieldValue
import dhis2.org.analytics.charts.data.GraphPoint
import dhis2.org.analytics.charts.data.NutritionChartType
import dhis2.org.analytics.charts.data.SerieData
import org.hisp.dhis.lib.expression.math.ZScoreTable
import java.util.GregorianCalendar
import kotlin.math.exp
import kotlin.math.pow

class RuleEngineNutritionDataProviderImpl : NutritionDataProvider {
    // Standard SD levels plotted as reference lines on WHO growth charts
    private val sdLevels = listOf(-3.0, -2.0, -1.0, 0.0, 1.0, 2.0, 3.0)

    override fun getNutritionData(nutritionChartType: NutritionChartType): List<SerieData> {
        val zscoreTable =
            when (nutritionChartType) {
                NutritionChartType.WHO_WFA_BOY -> ZScoreTable.Z_SCORE_WFA_TABLE_BOY
                NutritionChartType.WHO_WFA_GIRL -> ZScoreTable.Z_SCORE_WFA_TABLE_GIRL
                NutritionChartType.WHO_HFA_BOY -> ZScoreTable.Z_SCORE_HFA_TABLE_BOY
                NutritionChartType.WHO_HFA_GIRL -> ZScoreTable.Z_SCORE_HFA_TABLE_GIRL
                NutritionChartType.WHO_WFH_BOY -> ZScoreTable.Z_SCORE_WFH_TABLE_BOY
                NutritionChartType.WHO_WHO_WFH_GIRL -> ZScoreTable.Z_SCORE_WFH_TABLE_GIRL
            }

        val nutritionData = sdLevels.map { mutableListOf<GraphPoint>() }

        zscoreTable.toSortedMap(compareBy { it.parameter }).forEach { (key, lms) ->
            val parameter = key.parameter
            sdLevels.forEachIndexed { dataIndex, z ->
                val value = lmsToMeasurement(lms.l, lms.m, lms.s, z)
                nutritionData[dataIndex].add(
                    GraphPoint(
                        eventDate = GregorianCalendar(2021, 0, 1).time,
                        position = parameter,
                        fieldValue = GraphFieldValue.Decimal(value.toFloat()),
                    ),
                )
            }
        }

        return nutritionData.map {
            SerieData("", it)
        }
    }

    // WHO LMS formula: M*(1+L*S*Z)^(1/L) for L≠0, M*exp(S*Z) for L=0
    private fun lmsToMeasurement(
        l: Double,
        m: Double,
        s: Double,
        z: Double,
    ): Double =
        if (l != 0.0) {
            m * (1 + l * s * z).pow(1.0 / l)
        } else {
            m * exp(s * z)
        }
}
