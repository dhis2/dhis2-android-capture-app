package com.dhis2.usescases.teiDashboard.teiProgramList;

import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.ArrayList;
import java.util.List;

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

    private void getActiveEnrollments(){
        compositeDisposable.add(teiProgramListRepository.activeEnrollments(trackedEntityId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (enrollments) -> {
                            view.setActiveEnrollments(enrollments);
                        },
                        Timber::d)
        );
    }

    private void getOtherEnrollments(){
        compositeDisposable.add(teiProgramListRepository.otherEnrollments(trackedEntityId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        (enrollments) -> view.setOtherEnrollments(enrollments),
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
                        (alreadyEnrolledPrograms) -> deleteRepeatedPrograms(programs, alreadyEnrolledPrograms),
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
        compositeDisposable.dispose();
    }
}
