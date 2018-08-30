package com.dhis2.usescases.teiDashboard.eventDetail;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.dhis2.R;
import com.dhis2.data.forms.FormFragment;
import com.dhis2.data.forms.dataentry.DataEntryFragment;
import com.dhis2.data.metadata.MetadataRepository;
import com.dhis2.utils.CustomViews.OrgUnitDialog;
import com.dhis2.utils.DateUtils;

import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.program.ProgramStageModel;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
                                        eventDetailRepository.getProgram(eventUid),
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
            FormFragment formFragment = (FormFragment) view.getAbstractActivity().getSupportFragmentManager().getFragments().get(0);
            List<Fragment> sectionFragments = formFragment.getChildFragmentManager().getFragments();
            boolean mandatoryOk = true;
            boolean hasError = false;
            for (Fragment dataEntryFragment : sectionFragments) {
                mandatoryOk = mandatoryOk && ((DataEntryFragment) dataEntryFragment).checkMandatory();
                hasError =((DataEntryFragment) dataEntryFragment).checkErrors();
            }
            if (mandatoryOk && !hasError) {
                dataEntryStore.updateEventStatus(eventModel);
                changedEventStatus = true;
            } else if(!mandatoryOk)
                view.displayMessage(view.getAbstractActivity().getString(R.string.missing_mandatory_fields));
            else
                view.showInfoDialog("Unable to complete","Some fields have errors. Please, check them to be able to complete");
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

    @Override
    public void setDate() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dateDialog = new DatePickerDialog(view.getContext(), (
                (datePicker, year1, month1, day1) -> {
                    Calendar selectedCalendar = Calendar.getInstance();
                    selectedCalendar.set(Calendar.YEAR, year1);
                    selectedCalendar.set(Calendar.MONTH, month1);
                    selectedCalendar.set(Calendar.DAY_OF_MONTH, day1);
                    selectedCalendar.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
                    selectedCalendar.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
                    Date selectedDate = selectedCalendar.getTime();
                    String result = DateUtils.uiDateFormat().format(selectedDate);
                    view.setDate(result);

                    if (eventDetailModel.getProgramStage().accessDataWrite()) {
                        dataEntryStore.updateEvent(selectedDate, eventDetailModel.getEventModel());
                    }
                }),
                year,
                month,
                day);
        if(eventDetailModel.getEventModel().status() != EventStatus.SCHEDULE){
            dateDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        }
        dateDialog.setButton(DialogInterface.BUTTON_NEGATIVE, view.getContext().getString(R.string.date_dialog_clear), (dialog, which) -> {
        });
        dateDialog.show();
    }
}