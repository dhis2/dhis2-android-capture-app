package org.dhis2.usescases.programStageSelection;

import androidx.annotation.NonNull;

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
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 31/10/2017.
 */

public class ProgramStageSelectionPresenter implements ProgramStageSelectionContract.Presenter {

    private final RulesUtilsProvider ruleUtils;
    private ProgramStageSelectionContract.View view;
    private CompositeDisposable compositeDisposable;
    private ProgramStageSelectionRepository programStageSelectionRepository;

    ProgramStageSelectionPresenter(ProgramStageSelectionRepository programStageSelectionRepository, RulesUtilsProvider ruleUtils) {
        this.programStageSelectionRepository = programStageSelectionRepository;
        this.ruleUtils = ruleUtils;
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void onBackClick() {
        if (view != null)
            view.back();
    }

    @Override
    public void getProgramStages(String programId, @NonNull String uid, @NonNull ProgramStageSelectionContract.View view) {
        this.view = view;

        Flowable<List<ProgramStage>> stagesFlowable = programStageSelectionRepository.enrollmentProgramStages(programId, uid);
        Flowable<Result<RuleEffect>> ruleEffectFlowable = programStageSelectionRepository.calculate();

        // Combining results of two repositories into a single stream.
        Flowable<List<ProgramStage>> stageModelsFlowable = Flowable.zip(
                stagesFlowable.subscribeOn(Schedulers.io()),
                ruleEffectFlowable.subscribeOn(Schedulers.io()),
                this::applyEffects);

        compositeDisposable.add(stageModelsFlowable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view::setData,
                        Timber::e));
    }

    private List<ProgramStage> applyEffects(List<ProgramStage> stageModels, Result<RuleEffect> calcResult) {
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