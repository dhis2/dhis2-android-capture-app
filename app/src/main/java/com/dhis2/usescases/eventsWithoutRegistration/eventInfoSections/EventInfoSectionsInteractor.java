package com.dhis2.usescases.eventsWithoutRegistration.eventInfoSections;

import android.support.annotation.NonNull;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by Cristian on 01/03/2018.
 *
 */

public class EventInfoSectionsInteractor implements EventInfoSectionsContract.Interactor {

    private final EventInfoSectionsRepository eventInfoSectionsRepository;
    private EventInfoSectionsContract.View view;
    private CompositeDisposable compositeDisposable;

    EventInfoSectionsInteractor(EventInfoSectionsRepository eventInfoSectionsRepository) {
        this.eventInfoSectionsRepository = eventInfoSectionsRepository;
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void init(@NonNull EventInfoSectionsContract.View view, @NonNull String eventId, @NonNull String programStageUid) {
        this.view = view;
        getProgramStageSections(programStageUid);
    }

    private void getProgramStageSections(String eventUid) {
        compositeDisposable.add(eventInfoSectionsRepository.sections(eventUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        sections -> view.setSections(sections),
                        Timber::e
                ));
    }

    @Override
    public void onDettach() {
        compositeDisposable.clear();
    }
}
