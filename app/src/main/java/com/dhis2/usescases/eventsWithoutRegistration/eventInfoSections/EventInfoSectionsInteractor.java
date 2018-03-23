package com.dhis2.usescases.eventsWithoutRegistration.eventInfoSections;

import android.support.annotation.NonNull;

import com.dhis2.Bindings.Bindings;
import com.dhis2.data.metadata.MetadataRepository;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Cristian on 01/03/2018.
 *
 */

public class EventInfoSectionsInteractor implements EventInfoSectionsContract.Interactor {

    private final MetadataRepository metadataRepository;
    private final EventInfoSectionsRepository eventInfoSectionsRepository;
    private EventInfoSectionsContract.View view;
    private CompositeDisposable compositeDisposable;

    EventInfoSectionsInteractor(EventInfoSectionsRepository eventInfoSectionsRepository, MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
        this.eventInfoSectionsRepository = eventInfoSectionsRepository;
        Bindings.setMetadataRepository(metadataRepository);
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void init(@NonNull EventInfoSectionsContract.View view, @NonNull String eventId, @NonNull String programStageUid) {
        this.view = view;
        getProgramStageSections(programStageUid);
    }

    private void getProgramStageSections(String programStageUid) {
        compositeDisposable.add(eventInfoSectionsRepository.programStageSections(programStageUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        programStageSectionModelList -> view.setProgramStageSections(programStageSectionModelList),
                        throwable -> view.renderError(throwable.getMessage())
                ));
    }

    @Override
    public void onDettach() {
        compositeDisposable.dispose();
    }
}
