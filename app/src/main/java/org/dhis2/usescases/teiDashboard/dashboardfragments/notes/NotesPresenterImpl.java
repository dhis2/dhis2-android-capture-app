package org.dhis2.usescases.teiDashboard.dashboardfragments.notes;

import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.teiDashboard.DashboardRepository;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.note.NoteCreateProjection;

import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 09/04/2019.
 */
public class NotesPresenterImpl {
    private final DashboardRepository dashboardRepository;
    private String programUid;
    private String teiUid;
    private final D2 d2;
    private final SchedulerProvider schedulerProvider;
    private NotesContracts.View view;
    public CompositeDisposable compositeDisposable;
    private FlowableProcessor<Boolean> noteProcessor;

    public NotesPresenterImpl(D2 d2, DashboardRepository dashboardRepository, SchedulerProvider schedulerProvider,
                       NotesContracts.View view) {
        this.d2 = d2;
        this.dashboardRepository = dashboardRepository;
        this.view = view;
        this.schedulerProvider = schedulerProvider;
        this.noteProcessor = PublishProcessor.create();
        this.compositeDisposable = new CompositeDisposable();
    }

    public void init(String programUid, String teiUid) {
        this.programUid = programUid;
        this.teiUid = teiUid;
    }

    public void onDettach() {
        compositeDisposable.clear();
    }

    public void displayMessage(String message) {
        view.displayMessage(message);
    }

    public void setNoteProcessor(Flowable<Pair<String, Boolean>> noteProcessor) {
        compositeDisposable.add(noteProcessor
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        dashboardRepository.handleNote(),
                        Timber::d
                ));
    }

    public void subscribeToNotes() {
        compositeDisposable.add(
                noteProcessor.startWith(true)
                .flatMapSingle(getNotes->
                d2.noteModule().notes()
                .byEnrollmentUid().eq(
                        d2.enrollmentModule().enrollments().byProgram().eq(programUid)
                                .byTrackedEntityInstance().eq(teiUid)
                                .byStatus().eq(EnrollmentStatus.ACTIVE).one().blockingGet().uid()
                )
                .get()
                )
                .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                view.swapNotes(),
                                Timber::d
                        )
        );
    }

    public boolean hasProgramWritePermission() {
        return d2.programModule().programs().uid(programUid).blockingGet().access().data().write();
    }

    public void saveNote(String message){
        try {
            d2.noteModule().notes().blockingAdd(
                    NoteCreateProjection.builder()
                            .enrollment(d2.enrollmentModule().enrollments().byProgram().eq(programUid)
                                    .byTrackedEntityInstance().eq(teiUid)
                                    .byStatus().eq(EnrollmentStatus.ACTIVE).one().blockingGet().uid())
                            .value(message)
                            .build()
            );
            noteProcessor.onNext(true);
        } catch (D2Error d2Error) {
            Timber.e(d2Error);
        }
    }
}
