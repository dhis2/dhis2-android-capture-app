package org.dhis2.usescases.teiDashboard.teiProgramList;

import org.dhis2.usescases.main.program.ProgramViewModel;
import org.dhis2.utils.CustomViews.OrgUnitDialog;

import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by Cristian on 06/03/2018.
 */

public class TeiProgramListInteractor implements TeiProgramListContract.Interactor {

    private TeiProgramListContract.View view;
    private String trackedEntityId;
    private CompositeDisposable compositeDisposable;
    private final TeiProgramListRepository teiProgramListRepository;

    TeiProgramListInteractor(TeiProgramListRepository teiProgramListRepository) {
        this.teiProgramListRepository = teiProgramListRepository;
    }

    @Override
    public void init(TeiProgramListContract.View view, String trackedEntityId) {
        this.view = view;
        this.trackedEntityId = trackedEntityId;
        compositeDisposable = new CompositeDisposable();

        getActiveEnrollments();
        getOtherEnrollments();
        getPrograms();
    }

    @Override
    public void enroll(String programUid, String uid) {
        OrgUnitDialog orgUnitDialog = OrgUnitDialog.newInstace(false);
        orgUnitDialog.setTitle("Enrollment Org Unit")
                .setPossitiveListener(v -> {
                    if (orgUnitDialog.getSelectedOrgUnit() != null)
                        enrollInOrgUnit(orgUnitDialog.getSelectedOrgUnit(), programUid, uid);
                    orgUnitDialog.dismiss();
                })
                .setNegativeListener(v -> orgUnitDialog.dismiss());

        compositeDisposable.add(getOrgUnits()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        orgUnits -> {
                            if (orgUnits.size() > 1) {
                                orgUnitDialog.setOrgUnits(orgUnits);
                                if (!orgUnitDialog.isAdded())
                                    orgUnitDialog.show(view.getAbstracContext().getSupportFragmentManager(), "OrgUnitEnrollment");
                            } else
                                enrollInOrgUnit(orgUnits.get(0).uid(), programUid, uid);
                        },
                        Timber::d
                )
        );
    }

    private void enrollInOrgUnit(String orgUnitUid, String programUid, String teiUid) {
        compositeDisposable.add(
                teiProgramListRepository.saveToEnroll(orgUnitUid, programUid, teiUid)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(enrollmentUid -> {
                                    view.goToEnrollmentScreen(enrollmentUid);
                                },
                                Timber::d)
        );
    }

    public Observable<List<OrganisationUnitModel>> getOrgUnits() {
        return teiProgramListRepository.getOrgUnits();
    }

    private void getActiveEnrollments() {
        compositeDisposable.add(teiProgramListRepository.activeEnrollments(trackedEntityId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view::setActiveEnrollments,
                        Timber::d)
        );
    }

    private void getOtherEnrollments() {
        compositeDisposable.add(teiProgramListRepository.otherEnrollments(trackedEntityId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view::setOtherEnrollments,
                        Timber::d)
        );
    }

    private void getPrograms() {
        compositeDisposable.add(teiProgramListRepository.allPrograms(trackedEntityId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::getAlreadyEnrolledPrograms,
                        Timber::d)
        );
    }

    private void getAlreadyEnrolledPrograms(List<ProgramViewModel> programs) {
        compositeDisposable.add(teiProgramListRepository.alreadyEnrolledPrograms(trackedEntityId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        alreadyEnrolledPrograms -> deleteRepeatedPrograms(programs, alreadyEnrolledPrograms),
                        Timber::d)
        );
    }

    private void deleteRepeatedPrograms(List<ProgramViewModel> allPrograms, List<ProgramModel> alreadyEnrolledPrograms) {
        ArrayList<ProgramViewModel> programListToPrint = new ArrayList<>();
        for (ProgramViewModel programModel1 : allPrograms) {
            boolean isAlreadyEnrolled = false;
            boolean onlyEnrollOnce = false;
            for (ProgramModel programModel2 : alreadyEnrolledPrograms) {
                if (programModel1.id().equals(programModel2.uid())) {
                    isAlreadyEnrolled = true;
                    onlyEnrollOnce = programModel2.onlyEnrollOnce();
                }
            }
            if (!isAlreadyEnrolled || !onlyEnrollOnce) {
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
