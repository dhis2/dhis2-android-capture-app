package com.dhis2.usescases.teiDashboard.eventDetail;

import android.annotation.SuppressLint;
import android.util.Log;

import com.dhis2.data.metadata.MetadataRepository;

import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by ppajuelo on 19/12/2017.
 */

public class EventDetailPresenter implements EventDetailContracts.Presenter {

    private final EventDetailRepository eventDetailRepository;
    private final MetadataRepository metadataRepository;
    private final DataEntryStore dataEntryStore;
    private EventDetailContracts.View view;
    private CompositeDisposable disposable;

    EventDetailPresenter(EventDetailRepository eventDetailRepository, MetadataRepository metadataRepository, DataEntryStore dataEntryStore) {
        this.metadataRepository = metadataRepository;
        this.eventDetailRepository = eventDetailRepository;
        this.dataEntryStore = dataEntryStore;
        disposable = new CompositeDisposable();
    }

    @Override
    public void init(EventDetailContracts.View view) {
        this.view = view;
    }

    @SuppressLint("CheckResult")
    @Override
    public void getEventData(String eventUid) {
        disposable.add(Observable.zip(
                eventDetailRepository.eventModelDetail(eventUid),
                eventDetailRepository.dataValueModelList(eventUid),
                eventDetailRepository.programStageSection(eventUid),
                eventDetailRepository.programStageDataElement(eventUid),
                eventDetailRepository.programStage(eventUid),
                EventDetailModel::new)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> view.setData(data, metadataRepository),
                        throwable -> Log.d("ERROR", throwable.getMessage()))
        );
    }

    @Override
    public void saveData(String uid, String value) {
        disposable.add(dataEntryStore.save(uid, value)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> {
                        },
                        Timber::d
                ));
    }

    @Override
    public void back() {
        view.back();
    }

    @Override
    public void eventStatus(EventModel eventModel, ProgramStageModel stageModel) {
        if (stageModel.accessDataWrite())
            dataEntryStore.updateEventStatus(eventModel);
        else
            view.displayMessage("You don't have the required permission to perform this action");
    }

    @Override
    public void editData() {
        view.setDataEditable();
    }

}