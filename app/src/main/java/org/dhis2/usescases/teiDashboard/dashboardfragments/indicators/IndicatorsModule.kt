package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import dagger.Module
import dagger.Provides
import dhis2.org.analytics.charts.Charts
import org.dhis2.data.dagger.PerFragment
import org.dhis2.data.forms.dataentry.RuleEngineRepository
import org.dhis2.data.schedulers.SchedulerProvider
import org.hisp.dhis.android.core.D2

@PerFragment
@Module
class IndicatorsModule(
    val programUid: String,
    val recordUid: String,
    val view: IndicatorsView,
    private val visualizationType: VisualizationType
) {

    @Provides
    @PerFragment
    fun providesPresenter(
        schedulerProvider: SchedulerProvider,
        indicatorRepository: IndicatorRepository
    ): IndicatorsPresenter {
        return IndicatorsPresenter(schedulerProvider, view, indicatorRepository)
    }

    @Provides
    @PerFragment
    fun provideRepository(
        d2: D2,
        ruleEngineRepository: RuleEngineRepository,
        charts: Charts?
    ): IndicatorRepository {
        return if (visualizationType == VisualizationType.TRACKER) {
            TrackerAnalyticsRepository(
                d2,
                ruleEngineRepository,
                charts,
                programUid,
                recordUid
            )
        } else {
            EventIndicatorRepository(
                d2,
                ruleEngineRepository,
                programUid,
                recordUid
            )
        }
    }

    /*@Provides
    @PerFragment
    fun ruleEngineRepository(
        @NonNull formRepository: FormRepository,
        d2: D2
    ): RuleEngineRepository {
        return if (visualizationType == VisualizationType.TRACKER) {
            var enrollmentRepository = d2.enrollmentModule()
                .enrollments().byTrackedEntityInstance().eq(recordUid)
            if (programUid.isNotEmpty()) {
                enrollmentRepository = enrollmentRepository.byProgram().eq(programUid)
            }

            val uid = enrollmentRepository.one().blockingGet().uid()
            EnrollmentRuleEngineRepository(formRepository, uid, d2)
        } else {
            EventRuleEngineRepository(d2, formRepository, recordUid)
        }
    }*/
}
