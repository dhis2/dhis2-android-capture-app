package com.dhis2.usescases.programStageSelection;

import android.support.annotation.NonNull;

import org.hisp.dhis.android.core.program.ProgramStageModel;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by ppajuelo on 31/10/2017.
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
        compositeDisposable.add(programStageSelectionRepository.enrollmentProgramStages(programId, uid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view::setData,
                        Timber::e));
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