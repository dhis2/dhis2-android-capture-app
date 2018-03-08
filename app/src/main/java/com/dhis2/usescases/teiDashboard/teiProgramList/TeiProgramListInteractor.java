package com.dhis2.usescases.teiDashboard.teiProgramList;

import com.dhis2.Bindings.Bindings;
import com.dhis2.data.metadata.MetadataRepository;

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
    private String programId;
    private CompositeDisposable compositeDisposable;
    private final MetadataRepository metadataRepository;
    private final TeiProgramListRepository teiProgramListRepository;

    TeiProgramListInteractor(TeiProgramListRepository teiProgramListRepository, MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
        this.teiProgramListRepository = teiProgramListRepository;
        Bindings.setMetadataRepository(metadataRepository);
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void init(TeiProgramListContract.View view, String trackedEntityId, String programId) {
        this.view = view;
        this.trackedEntityId = trackedEntityId;
        this.programId = programId;
        getActiveEnrollments();
        getOtherEnrollments();
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
                        (enrollments) -> {
                            view.setOtherEnrollments(enrollments);
                        },
                        Timber::d)
        );
    }

    @Override
    public void onDettach() {
        compositeDisposable.dispose();
    }
}
