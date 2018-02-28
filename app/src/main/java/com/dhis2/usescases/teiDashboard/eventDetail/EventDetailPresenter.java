package com.dhis2.usescases.teiDashboard.eventDetail;

import android.util.Log;

import com.dhis2.data.metadata.MetadataRepository;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ppajuelo on 19/12/2017.
 */

public class EventDetailPresenter implements EventDetailContracts.Presenter {

    private final EventDetailRepository eventDetailRepository;
    private final MetadataRepository metadataRepository;
    private final DataEntryStore dataEntryStore;
    private EventDetailContracts.View view;

    public EventDetailPresenter(EventDetailRepository eventDetailRepository, MetadataRepository metadataRepository, DataEntryStore dataEntryStore) {
        this.metadataRepository = metadataRepository;
        this.eventDetailRepository = eventDetailRepository;
        this.dataEntryStore = dataEntryStore;
    }

    @Override
    public void init(EventDetailContracts.View view) {
        this.view = view;
    }

    @Override
    public void getEventData(String eventUid) {
        Observable.zip(eventDetailRepository.eventModelDetail(eventUid),
                eventDetailRepository.dataValueModelList(eventUid),
                EventDetailModel::new)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> view.setData(data, metadataRepository),
                        throwable -> Log.d("ERROR", throwable.getMessage()));
    }

    @Override
    public void saveData(String uid, String value) {
        dataEntryStore.save(uid, value)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    @Override
    public void back() {
        view.back();
    }
}