package com.dhis2.usescases.eventsWithoutRegistration.eventSummary;

import android.support.annotation.Nullable;

import com.dhis2.Bindings.Bindings;
import com.dhis2.data.metadata.MetadataRepository;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by Cristian on 01/03/2018.
 *
 */

public class EventSummaryInteractor implements EventSummaryContract.Interactor {

    private final MetadataRepository metadataRepository;
    private final EventSummaryRepository eventSummaryRepository;
    private EventSummaryContract.View view;
    private CompositeDisposable compositeDisposable;


    EventSummaryInteractor(EventSummaryRepository eventSummaryRepository, MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
        this.eventSummaryRepository = eventSummaryRepository;
        Bindings.setMetadataRepository(metadataRepository);
        compositeDisposable = new CompositeDisposable();
    }

    @Override
    public void init(EventSummaryContract.View view, String programId, @Nullable String eventId) {
        this.view = view;
        getProgram(programId);
        getEventSections(eventId);
    }

    @Override
    public void getProgram(String programUid) {
        compositeDisposable.add(metadataRepository.getProgramWithId(programUid)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view::setProgram,
                        Timber::e

                ));
    }

    @Override
    public void getEventSections(String eventId) {
        compositeDisposable.add(eventSummaryRepository.programStageSections(eventId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        view::onEventSections,
                        Timber::e

                ));
    }

    @Override
    public void onDettach() {
        compositeDisposable.clear();
    }
}
