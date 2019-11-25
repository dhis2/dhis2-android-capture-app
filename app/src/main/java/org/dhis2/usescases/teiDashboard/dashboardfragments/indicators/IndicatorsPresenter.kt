package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import android.text.TextUtils.isEmpty
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import org.dhis2.data.forms.dataentry.RuleEngineRepository
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.tuples.Pair
import org.dhis2.data.tuples.Trio
import org.dhis2.usescases.teiDashboard.DashboardRepository
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.program.ProgramIndicator
import timber.log.Timber

class IndicatorsPresenter(val d2: D2,
                          val schedulerProvider: SchedulerProvider,
                          val programUid: String,
                          val teiUid: String,
                          val dashboardRepository: DashboardRepository,
                          val ruleEngineRepository: RuleEngineRepository,
                          val view: IndicatorsView) {

    private val compositeDisposable = CompositeDisposable()
    private lateinit var enrollmentUid : String

    init {
        var enrollmentRepository = d2.enrollmentModule().enrollments()
            .byTrackedEntityInstance().eq(teiUid)

        if (!isEmpty(programUid))
            enrollmentRepository = enrollmentRepository.byProgram().eq(programUid)

        enrollmentUid = if(enrollmentRepository.one().blockingGet() == null)
                            ""
                        else
                            enrollmentRepository.one().blockingGet().uid()

    }

    fun init() {
        compositeDisposable.add(dashboardRepository.getIndicators(programUid)
            .filter{
                !isEmpty(enrollmentUid)
            }.map {
                Observable.fromIterable(it)
                    .filter{ indicator ->
                        (indicator.displayInForm() != null && indicator.displayInForm()!!)
                    }.map { indicator ->
                        val indicatorValue =
                            d2.programModule().programIndicatorEngine().getProgramIndicatorValue(
                                enrollmentUid,
                                null,
                                indicator.uid())
                        return@map Pair.create(indicator, indicatorValue?: "")
                    }.filter { pair -> !pair.val1().isNullOrEmpty() }
                    .flatMap { pair -> dashboardRepository
                                    .getLegendColorForIndicator(pair.val0(), pair.val1()) }
                    .toList()
            }.flatMap { it.toFlowable() }
            .flatMap { indicators -> ruleEngineRepository.updateRuleEngine()
                .flatMap { ruleEngineRepository.reCalculate() }
                .map { this::ap }}
            .subscribeOn(schedulerProvider.io())
            .observeOn(schedulerProvider.ui())
            .subscribe(
                {view::swapIndicators},
                Timber::e
        ))
    }
}