package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators;

import org.dhis2.data.forms.dataentry.RuleEngineRepository;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.teiDashboard.DashboardRepository;
import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.enrollment.EnrollmentCollectionRepository;
import org.hisp.dhis.android.core.program.ProgramIndicator;
import org.hisp.dhis.rules.models.RuleAction;
import org.hisp.dhis.rules.models.RuleActionDisplayKeyValuePair;
import org.hisp.dhis.rules.models.RuleActionDisplayText;
import org.hisp.dhis.rules.models.RuleEffect;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 09/04/2019.
 */
public class IndicatorsPresenterImpl implements IndicatorsContracts.Presenter {

    private final D2 d2;
    private CompositeDisposable compositeDisposable;
    private final String programUid;
    private final String enrollmentUid;
    private final DashboardRepository dashboardRepository;
    private final RuleEngineRepository ruleEngineRepository;
    private IndicatorsContracts.View view;


    IndicatorsPresenterImpl(D2 d2, String programUid, String teiUid, DashboardRepository dashboardRepository,
                            RuleEngineRepository ruleEngineRepository) {
        this.d2 = d2;
        this.programUid = programUid;
        this.dashboardRepository = dashboardRepository;
        this.ruleEngineRepository = ruleEngineRepository;

        EnrollmentCollectionRepository enrollmentRepository = d2.enrollmentModule().enrollments
                .byTrackedEntityInstance().eq(teiUid);
        if (!isEmpty(programUid))
            enrollmentRepository = enrollmentRepository.byProgram().eq(programUid);

        enrollmentUid = enrollmentRepository.one().blockingGet() == null ? "" : enrollmentRepository.one().blockingGet().uid();
    }

    @Override
    public void init(IndicatorsContracts.View view) {
        this.view = view;
        this.compositeDisposable = new CompositeDisposable();

        compositeDisposable.add(
                dashboardRepository.getIndicators(programUid)
                        .filter(indicators -> !isEmpty(enrollmentUid))
                        .map(indicators ->
                                Observable.fromIterable(indicators)
                                        .filter(indicator -> indicator.displayInForm() != null && indicator.displayInForm())
                                        .map(indicator -> {
                                            String indicatorValue = d2.programModule().programIndicatorEngine.getProgramIndicatorValue(
                                                    enrollmentUid,
                                                    null,
                                                    indicator.uid());
                                            return Pair.create(indicator, indicatorValue == null ? "" : indicatorValue);
                                        })
                                        .filter(pair -> !pair.val1().isEmpty())
                                        .flatMap(pair -> dashboardRepository.getLegendColorForIndicator(pair.val0(), pair.val1()))
                                        .toList()
                        )
                        .flatMap(Single::toFlowable)
                        .flatMap(indicators -> ruleEngineRepository.updateRuleEngine()
                                .flatMap(ruleEngine -> ruleEngineRepository.reCalculate())
                                .map(this::applyRuleEffects) //Restart rule engine to take into account value changes
                                .map(ruleIndicators -> {
                                    for (Trio<ProgramIndicator, String, String> indicator : ruleIndicators)
                                        if (!indicators.contains(indicator))
                                            indicators.add(indicator);
                                    return indicators;
                                }))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view.swapIndicators(),
                                Timber::d
                        )
        );
    }

    private List<Trio<ProgramIndicator, String, String>> applyRuleEffects(Result<RuleEffect> calcResult) {

        List<Trio<ProgramIndicator, String, String>> indicators = new ArrayList<>();

        if (calcResult.error() != null) {
            Timber.e(calcResult.error());
            return new ArrayList<>();
        }

        for (RuleEffect ruleEffect : calcResult.items()) {
            RuleAction ruleAction = ruleEffect.ruleAction();
            if (!ruleEffect.data().contains("#{")) //Avoid display unavailable variables
                if (ruleAction instanceof RuleActionDisplayKeyValuePair) {
                    Trio<ProgramIndicator, String, String> indicator = Trio.create(
                            ProgramIndicator.builder()
                                    .uid(((RuleActionDisplayKeyValuePair) ruleAction).content())
                                    .displayName(((RuleActionDisplayKeyValuePair) ruleAction).content())
                                    .build(),
                            ruleEffect.data(), "");
                    indicators.add(indicator);
                } else if (ruleAction instanceof RuleActionDisplayText) {
                    Trio<ProgramIndicator, String, String> indicator = Trio.create(null,
                            ((RuleActionDisplayText) ruleAction).content() + ruleEffect.data(), "");
                    indicators.add(indicator);
                }
        }

        return indicators;
    }

    @Override
    public void onDettach() {
        compositeDisposable.clear();
    }

    @Override
    public void displayMessage(String message) {
        view.displayMessage(message);
    }
}
