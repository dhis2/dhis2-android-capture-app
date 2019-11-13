package org.dhis2.usescases.teiDashboard.dashboardfragments.notes

import io.reactivex.disposables.CompositeDisposable
import io.reactivex.processors.FlowableProcessor
import io.reactivex.processors.PublishProcessor
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.data.tuples.Pair
import org.dhis2.usescases.teiDashboard.DashboardRepository
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.note.NoteCreateProjection
import timber.log.Timber

class NotesPresenter(
    val d2: D2,
    val dashboardRepository: DashboardRepository,
    val schedulerProvider: SchedulerProvider,
    val view: NotesContracts.View
) {

    var compositeDisposable: CompositeDisposable = CompositeDisposable()
    var processor: FlowableProcessor<Boolean> = PublishProcessor.create()
    private lateinit var programUid: String
    private lateinit var teiUid: String

    fun init(programUid: String, teiUid: String) {
        this.programUid = programUid
        this.teiUid = teiUid
    }

    fun onDettach() = compositeDisposable.clear()

    fun displayMessage(message: String) = view.displayMessage(message)

    fun setNoteProcessor(noteProcessor: FlowableProcessor<Pair<String, Boolean>>) {
        compositeDisposable.add(
            noteProcessor
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    dashboardRepository::handleNote,
                    Timber::d
                )
        )
    }

    fun subscribeToNotes() {
        compositeDisposable.add(
            processor.startWith(true)
                .flatMapSingle {
                    d2.noteModule().notes()
                        .byEnrollmentUid().eq(
                        d2.enrollmentModule().enrollments().byProgram().eq(programUid)
                            .byTrackedEntityInstance().eq(teiUid)
                            .byStatus().eq(EnrollmentStatus.ACTIVE).one().blockingGet().uid()
                    )
                        .get()
                }.subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
                .subscribe(
                    view::swapNotes,
                    Timber::e
                )
        )
    }

    fun hasProgramWritePermission(): Boolean {
        return d2.programModule().programs().uid(programUid).blockingGet().access().data().write()
    }

    fun saveNote(message: String) {
        try {
            d2.noteModule().notes().blockingAdd(
                NoteCreateProjection.builder()
                    .enrollment(
                        d2.enrollmentModule().enrollments().byProgram().eq(programUid)
                            .byTrackedEntityInstance().eq(teiUid)
                            .byStatus().eq(EnrollmentStatus.ACTIVE).one().blockingGet().uid()
                    )
                    .value(message)
                    .build()
            )
            processor.onNext(true)
        } catch (d2Error: D2Error) {
            Timber.e(d2Error)
        }
    }
}
