package org.dhis2.usescases.programStageSelection;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.utils.Result;
import org.dhis2.utils.RulesUtilsProvider;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.rules.models.RuleEffect;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

import static androidx.annotation.VisibleForTesting.PRIVATE;

/**
 * QUADRAM. Created by ppajuelo on 31/10/2017.
 */

public class ProgramStageSelectionPresenter implements ProgramStageSelectionContract.Presenter {

    private final RulesUtilsProvider ruleUtils;
    private final SchedulerProvider schedulerProvider;
    private ProgramStageSelectionContract.View view;
    public CompositeDisposable compositeDisposable;
    private ProgramStageSelectionRepository programStageSelectionRepository;

    public ProgramStageSelectionPresenter(ProgramStageSelectionContract.View view, ProgramStageSelectionRepository programStageSelectionRepository, RulesUtilsProvider ruleUtils, SchedulerProvider schedulerProvider) {
        this.view = view;
        this.programStageSelectionRepository = programStageSelectionRepository;
        this.ruleUtils = ruleUtils;
        this.schedulerProvider = schedulerProvider;
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void onBackClick() {
        if (view != null)
            view.back();
    }

    @Override
    public void getProgramStages(String programId, @NonNull String uid) {

        Flowable<List<ProgramStage>> stagesFlowable = programStageSelectionRepository.enrollmentProgramStages(programId, uid);
        Flowable<Result<RuleEffect>> ruleEffectFlowable = programStageSelectionRepository.calculate();

        // Combining results of two repositories into a single stream.
        Flowable<List<ProgramStage>> stageModelsFlowable = Flowable.zip(
                stagesFlowable.subscribeOn(schedulerProvider.io()),
                ruleEffectFlowable.subscribeOn(schedulerProvider.io()),
                this::applyEffects);

        compositeDisposable.add(stageModelsFlowable
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        view::setData,
                        Timber::e));
    }

    @VisibleForTesting()
    public List<ProgramStage> applyEffects(List<ProgramStage> stageModels, Result<RuleEffect> calcResult) {
        if (calcResult.error() != null) {
            Timber.e(calcResult.error());
            return stageModels;
        }

        Map<String, ProgramStage> stageView = toMap(stageModels);

        ruleUtils.applyRuleEffects(stageView, calcResult);

        return new ArrayList<>(stageView.values());
    }

    @NonNull
    private static Map<String, ProgramStage> toMap(@NonNull List<ProgramStage> stageViews) {
        Map<String, ProgramStage> map = new LinkedHashMap<>();
        for (ProgramStage stageModel : stageViews) {
            map.put(stageModel.uid(), stageModel);
        }
        return map;
    }

    @Override
    public void onDettach() {
        compositeDisposable.clear();
    }

    @Override
    public void displayMessage(String message) {
        view.displayMessage(message);
    }

    @Override
    public void onProgramStageClick(ProgramStage programStage) {
        view.setResult(programStage.uid(), programStage.repeatable(), programStage.periodType());
    }

    @Override
    public int getStandardInterval(String programStageUid) {
        return programStageSelectionRepository.getStage(programStageUid).standardInterval() != null ?
                programStageSelectionRepository.getStage(programStageUid).standardInterval() :
                0;
    }
}