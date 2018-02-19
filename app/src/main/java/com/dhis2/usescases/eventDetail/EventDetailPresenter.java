package com.dhis2.usescases.eventDetail;

import com.dhis2.data.metadata.MetadataRepository;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by ppajuelo on 19/12/2017.
 *
 */

public class EventDetailPresenter implements EventDetailContracts.Presenter {

    private final EventDetailRepository eventDetailRepository;
    private final MetadataRepository metadataRepository;
    private EventDetailContracts.View view;

    public EventDetailPresenter(EventDetailRepository eventDetailRepository, MetadataRepository metadataRepository) {
        this.metadataRepository = metadataRepository;
        this.eventDetailRepository = eventDetailRepository;
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
}