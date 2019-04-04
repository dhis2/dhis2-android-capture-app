package org.dhis2.usescases.teiDashboard.dashboardfragments.notes;

import android.annotation.SuppressLint;

import org.dhis2.data.metadata.MetadataRepository;
import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.teiDashboard.DashboardProgramModel;
import org.dhis2.usescases.teiDashboard.DashboardRepository;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class NotesPresenterImpl implements NotesPresenter {

    private final DashboardRepository dashboardRepository;
    private final MetadataRepository metadataRepository;

    private String teiUid;
    private String programUid;

    private boolean programWritePermission;

    private CompositeDisposable compositeDisposable;

    public NotesPresenterImpl(DashboardRepository dashboardRepository,
                              MetadataRepository metadataRepository,
                              String programUid, String teiUid) {
        this.dashboardRepository = dashboardRepository;
        this.metadataRepository = metadataRepository;
        this.programUid = programUid;
        this.teiUid = teiUid;
        compositeDisposable = new CompositeDisposable();

        dashboardRepository.setDashboardDetails(teiUid, programUid);

        getData();
    }

    @SuppressLint({"CheckResult"})
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
                            dashboardModel -> this.programWritePermission = dashboardModel.getCurrentProgram().accessDataWrite(),
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
                                this.programWritePermission = dashboardModel.getCurrentProgram().accessDataWrite();
                            },
                            Timber::e)
            );
        }
    }

    @Override
    public void setNoteProcessor(Flowable<Pair<String, Boolean>> noteProcessor) {
        compositeDisposable.add(noteProcessor
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        dashboardRepository.handleNote(),
                        Timber::d
                ));
    }

    @Override
    public void subscribeToNotes(NotesFragment notesFragment) {
        compositeDisposable.add(dashboardRepository.getNotes(programUid, teiUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        notesFragment.swapNotes(),
                        Timber::d
                )
        );
    }

    @Override
    public Boolean hasProgramWritePermission() {
        return programWritePermission;
    }
}
