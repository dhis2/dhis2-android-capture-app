package org.dhis2.usescases.programStageSelection;

import android.support.annotation.NonNull;

import org.dhis2.utils.Result;

import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.rules.models.RuleAction;
import org.hisp.dhis.rules.models.RuleActionHideProgramStage;
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

    private ProgramStageSelectionContract.View view;
    private CompositeDisposable compositeDisposable;
    private ProgramStageSelectionRepository programStageSelectionRepository;

    ProgramStageSelectionPresenter(ProgramStageSelectionRepository programStageSelectionRepository) {
        this.programStageSelectionRepository = programStageSelectionRepository;
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

        Flowable<List<ProgramStageModel>> stagesFlowable = programStageSelectionRepository.enrollmentProgramStages(programId, uid);

        Flowable<Result<RuleEffect>> ruleEffectFlowable = programStageSelectionRepository.calculate().subscribeOn(Schedulers.computation());

        // Combining results of two repositories into a single stream.
        Flowable<List<ProgramStageModel>> stageModelsFlowable = Flowable.zip(stagesFlowable, ruleEffectFlowable, this::applyEffects);

        compositeDisposable.add(stageModelsFlowable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view::setData,
                        Timber::e));
    }

    private List<ProgramStageModel> applyEffects(List<ProgramStageModel> stageModels, Result<RuleEffect> calcResult) {
        if (calcResult.error() != null) {
            calcResult.error().printStackTrace();
            return stageModels;
        }

        Map<String, ProgramStageModel> stageViewModels = toMap(stageModels);

        for (RuleEffect ruleEffect : calcResult.items()) {
            RuleAction ruleAction = ruleEffect.ruleAction();
            if (ruleAction instanceof RuleActionHideProgramStage) {
                RuleActionHideProgramStage hideProgramStage = (RuleActionHideProgramStage) ruleAction;
                stageViewModels.remove(hideProgramStage.programStage());
            }
        }

        return new ArrayList<>(stageViewModels.values());
    }

    @NonNull
    private static Map<String, ProgramStageModel> toMap(@NonNull List<ProgramStageModel> stageViewModels) {
        Map<String, ProgramStageModel> map = new LinkedHashMap<>();
        for (ProgramStageModel stageModelModel : stageViewModels) {
            map.put(stageModelModel.uid(), stageModelModel);
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
    public void onProgramStageClick(ProgramStageModel programStage) {
        view.setResult(programStage.uid(), programStage.repeatable(), programStage.periodType());
    }
}