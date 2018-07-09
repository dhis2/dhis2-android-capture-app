package com.dhis2.usescases.teiDashboard.eventDetail;

import android.annotation.SuppressLint;
import android.util.Log;

import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.utils.CustomViews.OrgUnitDialog;

import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 19/12/2017.
 */

public class EventDetailPresenter implements EventDetailContracts.Presenter {

    private final EventDetailRepository eventDetailRepository;
    private final MetadataRepository metadataRepository;
    private final DataEntryStore dataEntryStore;
    private EventDetailContracts.View view;
    private CompositeDisposable disposable;
    private EventDetailModel eventDetailModel;
    private String eventUid;

    private boolean changedEventStatus = false;

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
        this.eventUid = eventUid;

       /* disposable.add(Observable.zip(
                eventDetailRepository.eventModelDetail(eventUid),
                eventDetailRepository.dataValueModelList(eventUid),
                eventDetailRepository.programStageSection(eventUid),
                eventDetailRepository.programStageDataElement(eventUid),
                eventDetailRepository.programStage(eventUid),
                eventDetailRepository.orgUnitName(eventUid),
                eventDetailRepository.getCategoryOptionCombos(),
                EventDetailModel::new)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        data -> {
                            eventDetailModel = data;
                            view.setData(data, metadataRepository);
                        },
                        throwable -> Log.d("ERROR", throwable.getMessage()))
        );*/

        disposable.add(
                eventDetailRepository.eventStatus(eventUid)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .flatMap(
                                data -> Observable.zip(
                                        eventDetailRepository.eventModelDetail(eventUid),
                                        eventDetailRepository.dataValueModelList(eventUid),
                                        eventDetailRepository.programStageSection(eventUid),
                                        eventDetailRepository.programStageDataElement(eventUid),
                                        eventDetailRepository.programStage(eventUid),
                                        eventDetailRepository.orgUnitName(eventUid),
                                        eventDetailRepository.getCategoryOptionCombos(),
                                        EventDetailModel::new).toFlowable(BackpressureStrategy.LATEST)
                        )
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                data -> {
                                    eventDetailModel = data;
                                    view.setData(data, metadataRepository);
                                },
                                throwable -> Log.d("ERROR", throwable.getMessage()))

        );
    }

    @Override
    public void getExpiryDate(String eventUid) {
        disposable.add(
                metadataRepository.getExpiryDateFromEvent(eventUid)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                view::isEventExpired,
                                Timber::d
                        )
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

        view.goBack(changedEventStatus);

    }

    @Override
    public void eventStatus(EventModel eventModel, ProgramStageModel stageModel) {
        if (stageModel.accessDataWrite()) {
            dataEntryStore.updateEventStatus(eventModel);
            changedEventStatus = true;
        } else
            view.displayMessage(null);
    }

    @Override
    public void editData() {
        view.setDataEditable();
    }

    @Override
    public void confirmDeleteEvent() {
        view.showConfirmDeleteEvent();
    }

    @Override
    public void deleteEvent() {
        if (eventDetailModel != null && eventDetailModel.getEventModel() != null) {
            if (eventDetailModel.getEventModel().state() == State.TO_POST) {
                eventDetailRepository.deleteNotPostedEvent(eventDetailModel.getEventModel().uid());
            } else {
                eventDetailRepository.deletePostedEvent(eventDetailModel.getEventModel());
            }
            view.showEventWasDeleted();
        }
    }

    @Override
    public void onOrgUnitClick() {

        OrgUnitDialog orgUnitDialog = OrgUnitDialog.newInstace(false);
        orgUnitDialog.setTitle("Event Org Unit")
                .setPossitiveListener(v -> {
                    view.setSelectedOrgUnit(orgUnitDialog.getSelectedOrgUnitModel());
                    orgUnitDialog.dismiss();
                })
                .setNegativeListener(v -> orgUnitDialog.dismiss());

        disposable.add(eventDetailRepository.getOrgUnits()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        orgUnits -> {
                            orgUnitDialog.setOrgUnits(orgUnits);
                            view.showOrgUnitSelector(orgUnitDialog);
                        },
                        Timber::d
                )
        );


    }
}