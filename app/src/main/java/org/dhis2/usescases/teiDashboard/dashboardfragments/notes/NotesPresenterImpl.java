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
public class NotesPresenterImpl implements NotesContracts.Presenter {
    private final DashboardRepository dashboardRepository;
    private final String programUid;
    private final String teiUid;
    private final D2 d2;
    private final SchedulerProvider schedulerProvider;
    private NotesContracts.View view;
    private CompositeDisposable compositeDisposable;
    private FlowableProcessor<Boolean> noteProcessor;

    NotesPresenterImpl(D2 d2, DashboardRepository dashboardRepository, String programUid, String teiUid, SchedulerProvider schedulerProvider) {
        this.d2 = d2;
        this.dashboardRepository = dashboardRepository;
        this.programUid = programUid;
        this.teiUid = teiUid;
        this.schedulerProvider = schedulerProvider;
        this.noteProcessor = PublishProcessor.create();
    }

    @Override
    public void init(NotesContracts.View view) {
        this.view = view;
        this.compositeDisposable = new CompositeDisposable();
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
    public void setNoteProcessor(Flowable<Pair<String, Boolean>> noteProcessor) {
        compositeDisposable.add(noteProcessor
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                        dashboardRepository.handleNote(),
                        Timber::d
                ));
    }

    @Override
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

    @Override
    public boolean hasProgramWritePermission() {
        return d2.programModule().programs().uid(programUid).blockingGet().access().data().write();
    }

    @Override
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
