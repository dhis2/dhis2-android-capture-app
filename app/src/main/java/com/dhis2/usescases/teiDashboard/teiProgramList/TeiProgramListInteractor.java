package com.dhis2.usescases.teiDashboard.teiProgramList;

import android.content.ContentValues;
import android.database.sqlite.SQLiteConstraintException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.dhis2.data.forms.FormActivity;
import com.dhis2.data.forms.FormViewArguments;

import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValueModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by Cristian on 06/03/2018.
 *
 */

public class TeiProgramListInteractor implements TeiProgramListContract.Interactor {

    private TeiProgramListContract.View view;
    private String trackedEntityId;
    private CompositeDisposable compositeDisposable;
    private final TeiProgramListRepository teiProgramListRepository;

    TeiProgramListInteractor(TeiProgramListRepository teiProgramListRepository) {
        this.teiProgramListRepository = teiProgramListRepository;
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void init(TeiProgramListContract.View view, String trackedEntityId) {
        this.view = view;
        this.trackedEntityId = trackedEntityId;
        getActiveEnrollments();
        getOtherEnrollments();
        getPrograms();
    }

    @Override
    public void enroll(String programUid, String uid) {
        //TODO: NEED TO SELECT ORG UNIT AND THEN SAVE AND CREATE ENROLLMENT BEFORE DOING THIS: FOR DEBUG USE ORG UNIT DiszpKrYNg8
        compositeDisposable.add(
                teiProgramListRepository.saveToEnroll(trackedEntityId, "DiszpKrYNg8", programUid, uid, null)
                        .subscribeOn(Schedulers.computation())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(enrollmentUid -> {
                                    FormViewArguments formViewArguments = FormViewArguments.createForEnrollment(enrollmentUid);
                                    this.view.getContext().startActivity(FormActivity.create(this.view.getAbstractActivity(), formViewArguments, true));
                                },
                                Timber::d)
        );
    }

    private void getActiveEnrollments(){
        compositeDisposable.add(teiProgramListRepository.activeEnrollments(trackedEntityId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view::setActiveEnrollments,
                        Timber::d)
        );
    }

    private void getOtherEnrollments(){
        compositeDisposable.add(teiProgramListRepository.otherEnrollments(trackedEntityId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view::setOtherEnrollments,
                        Timber::d)
        );
    }

    private void getPrograms(){
        compositeDisposable.add(teiProgramListRepository.allPrograms(trackedEntityId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::getAlreadyEnrolledPrograms,
                        Timber::d)
        );
    }

    private void getAlreadyEnrolledPrograms(List<ProgramModel> programs){
        compositeDisposable.add(teiProgramListRepository.alreadyEnrolledPrograms(trackedEntityId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        alreadyEnrolledPrograms -> deleteRepeatedPrograms(programs, alreadyEnrolledPrograms),
                        Timber::d)
        );
    }

    private void deleteRepeatedPrograms(List<ProgramModel> allPrograms, List<ProgramModel> alreadyEnrolledPrograms){
        ArrayList<ProgramModel> programListToPrint = new ArrayList<>();
        for (ProgramModel programModel1 : allPrograms){
            boolean isAlreadyEnrolled = false;
            for (ProgramModel programModel2 : alreadyEnrolledPrograms){
                if (programModel1.uid().equals(programModel2.uid())){
                    isAlreadyEnrolled = true;
                }
            }
            if (!isAlreadyEnrolled || !programModel1.onlyEnrollOnce()){
                programListToPrint.add(programModel1);
            }
        }
        view.setPrograms(programListToPrint);
    }

    @Override
    public void onDettach() {
        compositeDisposable.clear();
    }
}
