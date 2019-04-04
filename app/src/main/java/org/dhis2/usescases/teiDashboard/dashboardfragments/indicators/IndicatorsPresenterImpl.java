package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators;

import org.dhis2.data.forms.dataentry.RuleEngineRepository;
import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.teiDashboard.DashboardProgramModel;
import org.dhis2.usescases.teiDashboard.DashboardRepository;
import org.dhis2.utils.Result;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.program.ProgramIndicatorModel;
import org.hisp.dhis.rules.models.RuleAction;
import org.hisp.dhis.rules.models.RuleActionDisplayKeyValuePair;
import org.hisp.dhis.rules.models.RuleActionDisplayText;
import org.hisp.dhis.rules.models.RuleEffect;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 30/11/2017.
 */

public class IndicatorsPresenterImpl implements IndicatorsPresenter {

    private final DashboardRepository dashboardRepository;
    private final D2 d2;
    private final RuleEngineRepository ruleRepository;
    private final MetadataRepository metadataRepository;

    private String programUid;
    private String teiUid;

    private CompositeDisposable compositeDisposable;
    private DashboardProgramModel dashboardProgramModel;

    public IndicatorsPresenterImpl(D2 d2, DashboardRepository dashboardRepository,
                                   MetadataRepository metadataRepository,
                                   RuleEngineRepository formRepository,
                                   String programUid, String teiUid) {
        this.d2 = d2;
        this.dashboardRepository = dashboardRepository;
        this.ruleRepository = formRepository;
        this.metadataRepository = metadataRepository;
        this.programUid = programUid;
        this.teiUid = teiUid;
        compositeDisposable = new CompositeDisposable();

        getData();
    }

    private void getData() {
        if (programUid != null)
            compositeDisposable.add(Observable.zip(
                    metadataRepository.getTrackedEntityInstance(teiUid),
                    dashboardRepository.getEnrollment(programUid, teiUid),
                    dashboardRepository.getProgramStages(programUid),
                    dashboardRepository.getTEIEnrollmentEvents(programUid, teiUid),
                    metadataRepository.getProgramTrackedEntityAttributes(programUid),
                    dashboardRepository.getTEIAttributeValues(programUid, teiUid),
                    metadataRepository.getTeiOrgUnit(teiUid, programUid),
                    metadataRepository.getTeiActivePrograms(teiUid),
                    DashboardProgramModel::new)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            dashboardModel -> {
                                this.dashboardProgramModel = dashboardModel;
                            },
                            Timber::e
                    )
            );

        else {
            compositeDisposable.add(Observable.zip(
                    metadataRepository.getTrackedEntityInstance(teiUid),
                    metadataRepository.getProgramTrackedEntityAttributes(null),
                    dashboardRepository.getTEIAttributeValues(null, teiUid),
                    metadataRepository.getTeiOrgUnit(teiUid),
                    metadataRepository.getTeiActivePrograms(teiUid),
                    metadataRepository.getTEIEnrollments(teiUid),
                    DashboardProgramModel::new)
                    .flatMap(dashboardProgramModel1 -> metadataRepository.getObjectStylesForPrograms(dashboardProgramModel1.getEnrollmentProgramModels())
                            .map(stringObjectStyleMap -> {
                                dashboardProgramModel1.setProgramsObjectStyles(stringObjectStyleMap);
                                return dashboardProgramModel1;
                            }))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            dashboardModel -> {
                                this.dashboardProgramModel = dashboardModel;
                            },
                            Timber::e)
            );
        }
    }

    @Override
    public void subscribeToIndicators(IndicatorsFragment indicatorsFragment) {
        compositeDisposable.add(dashboardRepository.getIndicators(programUid)
                .map(indicators ->
                        Observable.fromIterable(indicators)
                                .filter(indicator -> indicator.displayInForm() != null && indicator.displayInForm())
                                .map(indicator -> {
                                    String indicatorValue = d2.programModule().programIndicatorEngine.getProgramIndicatorValue(
                                            dashboardProgramModel.getCurrentEnrollment().uid(),
                                            null,
                                            indicator.uid());
                                    return Pair.create(indicator, indicatorValue == null ? "" : indicatorValue);
                                })
                                .filter(pair -> !pair.val1().isEmpty())
                                .flatMap(pair -> dashboardRepository.getLegendColorForIndicator(pair.val0(), pair.val1()))
                                .toList()
                )
                .flatMap(Single::toFlowable)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        indicatorsFragment.swapIndicators(),
                        Timber::d
                )
        );

        compositeDisposable.add(ruleRepository.calculate()
                .subscribe(
                        calcResult -> applyRuleEffects(calcResult, indicatorsFragment),
                        Timber::e
                ));
    }

    @Override
    public void onDettach() {
        compositeDisposable.clear();
    }

    private void applyRuleEffects(Result<RuleEffect> calcResult, IndicatorsFragment indicatorsFragment) {

        if (calcResult.error() != null) {
            Timber.e(calcResult.error());
            return;
        }

        for (RuleEffect ruleEffect : calcResult.items()) {
            RuleAction ruleAction = ruleEffect.ruleAction();
            if (ruleAction instanceof RuleActionDisplayKeyValuePair) {
                Trio<ProgramIndicatorModel, String, String> indicator = Trio.create(
                        ProgramIndicatorModel.builder().displayName(((RuleActionDisplayKeyValuePair) ruleAction).content()).build(),
                        ruleEffect.data(), "");
                indicatorsFragment.addIndicator(indicator);
            } else if (ruleAction instanceof RuleActionDisplayText) {
                Trio<ProgramIndicatorModel, String, String> indicator = Trio.create(
                        ProgramIndicatorModel.builder().displayName(((RuleActionDisplayText) ruleAction).content()).build(),
                        ruleEffect.data(), "");
                indicatorsFragment.addIndicator(indicator);
            }
        }
    }
}