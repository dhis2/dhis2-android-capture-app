package dhis2.org.analytics.charts

import dhis2.org.analytics.charts.data.Graph
import dhis2.org.analytics.charts.mappers.AnalyticsTeiSettingsToGraph
import dhis2.org.analytics.charts.mappers.DataElementToGraph
import dhis2.org.analytics.charts.mappers.ProgramIndicatorToGraph
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.period.PeriodType
import org.hisp.dhis.android.core.program.ProgramIndicator

class ChartsRepositoryImpl(
    private val d2: D2,
    private val analyticsTeiSettingsToGraph: AnalyticsTeiSettingsToGraph,
    private val dataElementToGraph: DataElementToGraph,
    private val programIndicatorToGraph: ProgramIndicatorToGraph
) : ChartsRepository {

    override fun getAnalyticsForEnrollment(enrollmentUid: String): List<Graph> {
        val enrollment = getEnrollment(enrollmentUid)
        if (enrollment.trackedEntityInstance() == null) return emptyList()

        val settingsAnalytics = getSettingsAnalytics(enrollment)
        return if (settingsAnalytics.isNotEmpty()) {
            settingsAnalytics
        } else {
            getDefaultAnalytics(enrollment)
        }
    }

    private fun getSettingsAnalytics(enrollment: Enrollment): List<Graph> {
        return d2.settingModule().analyticsSetting().teis()
            .byProgram().eq(enrollment.program())
            .blockingGet()?.let { analyticsSettings ->
            analyticsTeiSettingsToGraph.map(
                enrollment.trackedEntityInstance()!!,
                analyticsSettings,
                { dataElementUid ->
                    d2.dataElementModule().dataElements().uid(dataElementUid).blockingGet()
                        .displayFormName() ?: dataElementUid
                },
                { indicatorUid ->
                    d2.programModule().programIndicators().uid(indicatorUid).blockingGet()
                        .displayName() ?: indicatorUid
                },
                { nutritionGenderData ->
                    val genderValue =
                        d2.trackedEntityModule().trackedEntityAttributeValues().value(
                            nutritionGenderData.attributeUid,
                            enrollment.trackedEntityInstance()
                        ).blockingGet()
                    nutritionGenderData.isFemale(genderValue?.value())
                }
            )
        } ?: emptyList()
    }

    private fun getDefaultAnalytics(enrollment: Enrollment): List<Graph> {
        return getRepeatableProgramStages(enrollment.program()).map { programStage ->

            val period = programStage.periodType() ?: PeriodType.Daily

            getNumericDataElements(programStage.uid()).map { dataElement ->
                dataElementToGraph.map(
                    dataElement,
                    programStage.uid(),
                    enrollment.trackedEntityInstance()!!,
                    period
                )
            }.union(
                getStageIndicators(enrollment.program()).map { programIndicator ->
                    programIndicatorToGraph.map(
                        programIndicator,
                        programStage.uid(),
                        enrollment.trackedEntityInstance()!!,
                        period
                    )
                }
            )
        }.flatten()
            .filter { it.series.isNotEmpty() }
//            .nutritionTestingData(d2) TODO: REMOVE BEFORE MERGING
    }

    private fun getRepeatableProgramStages(program: String?) =
        d2.programModule().programStages()
            .byProgramUid().eq(program)
            .byRepeatable().eq(true)
            .blockingGet()

    private fun getEnrollment(enrollmentUid: String) =
        d2.enrollmentModule().enrollments()
            .uid(enrollmentUid)
            .blockingGet()

    private fun getNumericDataElements(stageUid: String): List<DataElement> {
        return d2.programModule().programStageDataElements()
            .byProgramStage().eq(stageUid)
            .blockingGet().filter {
                d2.dataElementModule().dataElements().uid(it.dataElement()?.uid())
                    .blockingGet().valueType()?.isNumeric ?: false
            }.map {
                d2.dataElementModule().dataElements().uid(
                    it.dataElement()?.uid()
                ).blockingGet()
            }
    }

    private fun getStageIndicators(programUid: String?): List<ProgramIndicator> {
        return d2.programModule().programIndicators()
            .byDisplayInForm().isTrue
            .byProgramUid().eq(programUid)
            .blockingGet()
    }
}
