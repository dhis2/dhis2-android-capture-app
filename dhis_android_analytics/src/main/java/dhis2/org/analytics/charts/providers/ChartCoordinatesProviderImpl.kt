package dhis2.org.analytics.charts.providers

import dhis2.org.analytics.charts.data.GraphPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.analytics.aggregated.GridResponseValue
import org.hisp.dhis.android.core.analytics.aggregated.MetadataItem
import org.hisp.dhis.android.core.common.RelativePeriod
import org.hisp.dhis.android.core.period.Period

class ChartCoordinatesProviderImpl(
    val d2: D2,
    val periodStepProvider: PeriodStepProvider
) : ChartCoordinatesProvider {

    override fun dataElementCoordinates(
        stageUid: String,
        teiUid: String,
        dataElementUid: String,
        selectedRelativePeriod: List<RelativePeriod>?,
        selectedOrgUnits: List<String>?
    ): List<GraphPoint> {
        var initialPeriod: Period? = null
        return d2.analyticsModule().eventLineList()
            .byProgramStage().eq(stageUid)
            .byTrackedEntityInstance().eq(teiUid)
            .withDataElement(dataElementUid)
            .run {
                selectedRelativePeriod?.let { relativePeriods ->
                    this.byEventDate().inPeriods(*relativePeriods.toTypedArray())
                } ?: this
            }
            .run {
                selectedOrgUnits?.let {
                    this.byOrganisationUnit().`in`(*selectedOrgUnits.toTypedArray())
                } ?: this
            }
            .blockingEvaluate()
            .sortedBy { it.date }
            .mapNotNull { lineListResponse ->
                if (initialPeriod == null) initialPeriod = lineListResponse.period
                lineListResponse.values.first().value?.let { value ->
                    GraphPoint(
                        eventDate = formattedDate(lineListResponse.date),
                        position = periodStepProvider.getPeriodDiff(
                            initialPeriod!!,
                            lineListResponse.period
                        ).toFloat(),
                        fieldValue = value.toFloat()
                    )
                }
            }
    }

    override fun indicatorCoordinates(
        stageUid: String,
        teiUid: String,
        indicatorUid: String,
        selectedRelativePeriod: List<RelativePeriod>?,
        selectedOrgUnits: List<String>?
    ): List<GraphPoint> {
        var initialPeriod: Period? = null
        return d2.analyticsModule()
            .eventLineList()
            .byProgramStage().eq(stageUid)
            .byTrackedEntityInstance().eq(teiUid)
            .withProgramIndicator(indicatorUid)
            .run {
                selectedRelativePeriod?.let { relativePeriods ->
                    this.byEventDate().inPeriods(*relativePeriods.toTypedArray())
                } ?: this
            }
            .run {
                selectedOrgUnits?.let {
                    this.byOrganisationUnit().`in`(*selectedOrgUnits.toTypedArray())
                } ?: this
            }
            .blockingEvaluate()
            .sortedBy { it.date }
            .filter {
                try {
                    !(it.values.first().value?.toFloat() ?: Float.NaN).isNaN()
                } catch (e: java.lang.Exception) {
                    false
                }
            }
            .mapNotNull { lineListResponse ->
                lineListResponse.values.first().value?.let { value ->
                    if (initialPeriod == null) initialPeriod = lineListResponse.period
                    GraphPoint(
                        eventDate = formattedDate(lineListResponse.date),
                        position = periodStepProvider.getPeriodDiff(
                            initialPeriod!!,
                            lineListResponse.period
                        ).toFloat(),
                        fieldValue = value.toFloat()
                    )
                }
            }
    }

    override fun nutritionCoordinates(
        stageUid: String,
        teiUid: String,
        zScoreValueContainerUid: String,
        zScoreSavedIsDataElement: Boolean,
        ageOrHeightCountainerUid: String,
        ageOrHeightIsDataElement: Boolean,
        selectedRelativePeriod: List<RelativePeriod>?,
        selectedOrgUnits: List<String>?
    ): List<GraphPoint> {
        var eventLineListRepository = d2.analyticsModule().eventLineList()
            .byProgramStage().eq(stageUid)
            .byTrackedEntityInstance().eq(teiUid)
        eventLineListRepository = if (zScoreSavedIsDataElement) {
            eventLineListRepository.withDataElement(zScoreValueContainerUid)
        } else {
            eventLineListRepository.withProgramIndicator(zScoreValueContainerUid)
        }
        eventLineListRepository = if (ageOrHeightIsDataElement) {
            eventLineListRepository.withDataElement(ageOrHeightCountainerUid)
        } else {
            eventLineListRepository.withProgramIndicator(ageOrHeightCountainerUid)
        }
        return eventLineListRepository
            .run {
                selectedRelativePeriod?.let { relativePeriods ->
                    this.byEventDate().inPeriods(*relativePeriods.toTypedArray())
                } ?: this
            }
            .run {
                selectedOrgUnits?.let {
                    this.byOrganisationUnit().`in`(*selectedOrgUnits.toTypedArray())
                } ?: this
            }
            .blockingEvaluate().mapNotNull { lineListResponse ->
                val zScoreValue =
                    lineListResponse.values.firstOrNull {
                        it.uid == zScoreValueContainerUid
                    }?.value
                val xAxisValue =
                    lineListResponse.values.firstOrNull {
                        it.uid == ageOrHeightCountainerUid
                    }?.value
                if (zScoreValue == null || xAxisValue == null) {
                    null
                } else {
                    GraphPoint(
                        eventDate = formattedDate(lineListResponse.date),
                        position = xAxisValue.toFloat(),
                        fieldValue = zScoreValue.toFloat()
                    )
                }
            }
    }

    override fun pieChartCoordinates(
        stageUid: String,
        teiUid: String,
        dataElementUid: String,
        selectedRelativePeriod: List<RelativePeriod>?,
        selectedOrgUnits: List<String>?
    ): List<GraphPoint> {
        val eventList = d2.analyticsModule().eventLineList()
            .byProgramStage().eq(stageUid)
            .byTrackedEntityInstance().eq(teiUid)
            .withDataElement(dataElementUid)
            .run {
                selectedRelativePeriod?.let { relativePeriods ->
                    this.byEventDate().inPeriods(*relativePeriods.toTypedArray())
                } ?: this
            }
            .run {
                selectedOrgUnits?.let {
                    this.byOrganisationUnit().`in`(*selectedOrgUnits.toTypedArray())
                } ?: this
            }
            .blockingEvaluate()
            .sortedBy { it.date }
            .filter { it.values.first().value != null }
        return eventList.groupBy { it.values.first().value }.mapNotNull {
            GraphPoint(
                eventDate = formattedDate(it.value.first().date),
                fieldValue = it.value.size.toFloat(),
                legend = it.key
            )
        }
    }

    override fun visualizationCoordinates(
        gridResponseValueList: List<GridResponseValue>,
        metadata: Map<String, MetadataItem>,
        categories: List<String>
    ): List<GraphPoint> {
        return gridResponseValueList.filter { it.value != null }
            .mapIndexed { index, gridResponseValue ->

                val periodId = gridResponseValue.rows.joinToString(separator = " - ") {
                    metadata[it]?.displayName.toString()
                }

                val position = periodId.let {
                    when (metadata[periodId]) {
                        is MetadataItem.RelativePeriodItem -> categories.indexOf(periodId)
                        else -> categories.indexOf(periodId)
                    }
                }

                val columnLegend = gridResponseValue.columns.firstOrNull()?.let {
                    when (val metadataItem = metadata[it]) {
                        is MetadataItem.PeriodItem -> periodStepProvider.periodUIString(
                            Locale.getDefault(),
                            metadataItem.item
                        )
                        else -> metadata[it]?.displayName
                    }
                }

                GraphPoint(
                    eventDate = GregorianCalendar(2021, 0, 1).time,
                    position = position.toFloat(),
                    fieldValue = gridResponseValue.value!!.toFloat(),
                    legend = columnLegend
                )
            }
    }

    private fun formattedDate(date: Date): Date {
        return try {
            val formattedDateString = SimpleDateFormat("yyyy-MM-dd").format(date)
            val formattedDate = SimpleDateFormat("yyyy-MM-dd").parse(formattedDateString)
            formattedDate ?: date
        } catch (e: Exception) {
            date
        }
    }
}
