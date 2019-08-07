package org.dhis2.usescases.teiDashboard.dashboardfragments.notes;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.teiDashboard.DashboardRepository;
import org.hisp.dhis.android.core.D2;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 09/04/2019.
 */
public class NotesPresenterImpl implements NotesContracts.Presenter {
    private final DashboardRepository dashboardRepository;
    private final String programUid;
    private final String teiUid;
    private final D2 d2;
    private NotesContracts.View view;
    private CompositeDisposable compositeDisposable;

    NotesPresenterImpl(D2 d2, DashboardRepository dashboardRepository, String programUid, String teiUid) {
        this.d2 = d2;
        this.dashboardRepository = dashboardRepository;
        this.programUid = programUid;
        this.teiUid = teiUid;
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
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        dashboardRepository.handleNote(),
                        Timber::d
                ));
    }

    @Override
    public void subscribeToNotes() {
        compositeDisposable.add(dashboardRepository.getNotes(programUid, teiUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view.swapNotes(),
                        Timber::d
                )
        );
    }

    @Override
    public boolean hasProgramWritePermission() {
        return d2.programModule().programs.uid(programUid).withAllChildren().blockingGet().access().data().write();
    }
}
