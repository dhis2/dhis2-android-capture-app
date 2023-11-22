package dhis2.org.analytics.charts.mappers

import android.util.Log
import dhis2.org.analytics.charts.data.DefaultSettingsAnalyticModel
import dhis2.org.analytics.charts.data.NutritionGenderData
import dhis2.org.analytics.charts.data.NutritionSettingsAnalyticsModel
import dhis2.org.analytics.charts.data.SettingsAnalyticModel
import dhis2.org.analytics.charts.data.toAnalyticsChartType
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.settings.AnalyticsTeiSetting
import org.hisp.dhis.android.core.settings.AnalyticsTeiWHONutritionItem

class AnalyticTeiSettingsToSettingsAnalyticsModel(
    private val analyticDataElementMapper: AnalyticDataElementToDataElementData,
    private val analyticIndicatorMapper: AnalyticIndicatorToIndicatorData,
) {
    fun map(analyticsTeiSetting: AnalyticsTeiSetting): SettingsAnalyticModel {
        return if (analyticsTeiSetting.whoNutritionData() != null) {
            mapNutrition(analyticsTeiSetting)
        } else {
            mapDefault(analyticsTeiSetting)
        }
    }

    private fun mapNutrition(
        analyticsTeiSetting: AnalyticsTeiSetting,
    ): NutritionSettingsAnalyticsModel {
        val (zScoreContainer, zScoreStageUid, yIsDataElement) = getZscoreContainer(
            analyticsTeiSetting.whoNutritionData()!!.y(),
        )
        val (ageOrHeightUid, ageOrHeightStageUid, xIsDataElement) = getAgeOrHeightContainer(
            analyticsTeiSetting.whoNutritionData()!!.x(),
        )

        if (zScoreStageUid != ageOrHeightStageUid) {
            Log.d("NUTRITION_CHART", "Stage should be the same")
        }

        return NutritionSettingsAnalyticsModel(
            analyticsTeiSetting.name(),
            analyticsTeiSetting.program(),
            analyticsTeiSetting.type().toAnalyticsChartType(),
            NutritionGenderData(
                analyticsTeiSetting.whoNutritionData()!!.gender().attribute(),
                analyticsTeiSetting.whoNutritionData()!!.gender().values().female(),
                analyticsTeiSetting.whoNutritionData()!!.gender().values().male(),
            ),
            zScoreStageUid,
            zScoreContainer,
            yIsDataElement,
            ageOrHeightUid,
            xIsDataElement,
        )
    }

    private fun mapDefault(analyticsTeiSetting: AnalyticsTeiSetting): DefaultSettingsAnalyticModel {
        return DefaultSettingsAnalyticModel(
            analyticsTeiSetting.name(),
            analyticsTeiSetting.program(),
            analyticsTeiSetting.type().toAnalyticsChartType(),
            analyticsTeiSetting.data()?.dataElements()?.filter { it.programStage() != null }?.map {
                analyticDataElementMapper.map(it)
            } ?: emptyList(),
            analyticsTeiSetting.data()?.indicators()?.filter { it.programStage() != null }?.map {
                analyticIndicatorMapper.map(it)
            } ?: emptyList(),
            analyticsTeiSetting.period()?.name ?: PeriodType.Daily.name,
        )
    }

    private fun getZscoreContainer(
        y: AnalyticsTeiWHONutritionItem,
    ): Triple<String, String, Boolean> {
        return if (y.dataElements().isNullOrEmpty()) {
            Triple(
                y.indicators().firstOrNull()?.indicator() ?: "",
                y.indicators().firstOrNull()?.programStage() ?: "",
                false,
            )
        } else {
            Triple(
                y.dataElements().firstOrNull()?.dataElement() ?: "",
                y.dataElements().firstOrNull()?.programStage() ?: "",
                true,
            )
        }
    }

    private fun getAgeOrHeightContainer(
        x: AnalyticsTeiWHONutritionItem,
    ): Triple<String, String, Boolean> {
        return if (x.dataElements().isNullOrEmpty()) {
            Triple(
                x.indicators().firstOrNull()?.indicator() ?: "",
                x.indicators().firstOrNull()?.programStage() ?: "",
                false,
            )
        } else {
            Triple(
                x.dataElements().firstOrNull()?.dataElement() ?: "",
                x.dataElements().firstOrNull()?.programStage() ?: "",
                true,
            )
        }
    }
}
