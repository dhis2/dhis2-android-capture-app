package com.dhis2.usescases.programStageSelection;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity;

import org.hisp.dhis.android.core.program.ProgramStageModel;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.EVENT_CREATION_TYPE;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.NEW_EVENT;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.ORG_UNIT;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.PROGRAM_UID;

/**
 * Created by ppajuelo on 31/10/2017.
 *
 */

public class ProgramStageSelectionPresenter implements ProgramStageSelectionContract.Presenter {

    private ProgramStageSelectionContract.View view;
    private String programUid;
    private CompositeDisposable compositeDisposable;
    private ProgramStageSelectionRepository programStageSelectionRepository;
    private String eventCreationType;
    private String orgUnit;

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
    public void getProgramStages(@NonNull String programUid, @NonNull ProgramStageSelectionContract.View view) {
        this.view = view;
        this.programUid = programUid;
        compositeDisposable.add(programStageSelectionRepository.getProgramStages(programUid)
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
    public void onProgramStageClick(ProgramStageModel programStage) {
        Bundle bundle = new Bundle();
        bundle.putString(PROGRAM_UID, programUid);
        bundle.putString(EVENT_CREATION_TYPE, eventCreationType);
        bundle.putString(ORG_UNIT, orgUnit);
        bundle.putBoolean(NEW_EVENT, true);

        view.startActivity(EventInitialActivity.class, bundle, false, false, null);
    }

    @Override
    public void setEventCreationType(String eventCreationType) {
        this.eventCreationType = eventCreationType;
    }

    @Override
    public void setOrgUnit(String orgUnit) {
        this.orgUnit = orgUnit;
    }
}