package org.dhis2.usescases.teiDashboard.eventDetail;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import org.dhis2.R;
import org.dhis2.data.forms.FormFragment;
import org.dhis2.data.forms.dataentry.DataEntryFragment;
import org.dhis2.data.schedulers.SchedulerProvider;
import org.dhis2.databinding.WidgetDatepickerBinding;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.OnDialogClickListener;
import org.dhis2.utils.custom_views.OrgUnitDialog;
import org.dhis2.utils.custom_views.PeriodDialog;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.period.PeriodType;
import org.hisp.dhis.android.core.program.ProgramStage;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 19/12/2017.
 */

public class EventDetailPresenter implements EventDetailContracts.Presenter {

    private final EventDetailRepository eventDetailRepository;
    private final DataEntryStore dataEntryStore;
    private final SchedulerProvider schedulerProvider;
    private EventDetailContracts.View view;
    private CompositeDisposable disposable;
    private EventDetailModel eventDetailModel;

    private boolean changedEventStatus = false;

    EventDetailPresenter(EventDetailRepository eventDetailRepository, DataEntryStore dataEntryStore, SchedulerProvider schedulerProvider) {
        this.eventDetailRepository = eventDetailRepository;
        this.dataEntryStore = dataEntryStore;
        this.schedulerProvider = schedulerProvider;
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
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .flatMap(
                                data -> Observable.zip(
                                        eventDetailRepository.eventModelDetail(eventUid),
                                        eventDetailRepository.programStage(eventUid),
                                        eventDetailRepository.orgUnit(eventUid),
                                        eventDetailRepository.getCategoryOptionCombos(),
                                        eventDetailRepository.getProgram(eventUid),
                                        eventDetailRepository.isEnrollmentActive(eventUid),
                                        EventDetailModel::new).toFlowable(BackpressureStrategy.LATEST)
                        )
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                data -> {
                                    eventDetailModel = data;
                                    view.setData(data);
                                },
                                throwable -> Log.d("ERROR", throwable.getMessage()))

        );
    }

    @Override
    public void getExpiryDate(String eventUid) {
        disposable.add(
                eventDetailRepository.getExpiryDateFromEvent(eventUid)
                        .subscribeOn(schedulerProvider.io())
                        .observeOn(schedulerProvider.ui())
                        .subscribe(
                                view::isEventExpired,
                                Timber::d
                        )
        );
    }

    @Override
    public void back() {
        if (view != null &&
                view.getAbstractActivity() != null &&
                !view.getAbstractActivity().getSupportFragmentManager().getFragments().isEmpty()) {
            ((FormFragment) view.getAbstractActivity().getSupportFragmentManager().getFragments().get(0)).getDatesLayout().getRootView().requestFocus();
            new Handler().postDelayed(() -> view.goBack(changedEventStatus), 1500);
        }
    }

    @Override
    public void eventStatus(View buttonView, Event eventModel, ProgramStage stageModel) {

        if (stageModel.access().data().write()) {
            if (eventModel.status() == EventStatus.OVERDUE)
                updateEventStatus(eventModel);
            else {
                FormFragment formFragment = (FormFragment) view.getAbstractActivity().getSupportFragmentManager().getFragments().get(0);
                formFragment.getDatesLayout().getRootView().requestFocus();
                new Handler().postDelayed(() -> {
                    if (formFragment.hasErrorOnComple() != null) { //Checks if there is an error action to display
                        view.showInfoDialog(view.getContext().getString(R.string.error), formFragment.hasErrorOnComple().content());
                    } else if (formFragment.hasError() != null) {
                        view.showInfoDialog(view.getContext().getString(R.string.error), formFragment.hasError().content());
                    } else {
                        if (formFragment.isAdded() && formFragment.getContext() != null) {
                            List<Fragment> sectionFragments = formFragment.getChildFragmentManager().getFragments();
                            boolean mandatoryOk = true;
                            boolean hasError = false;
                            for (Fragment dataEntryFragment : sectionFragments) {
                                mandatoryOk = mandatoryOk && ((DataEntryFragment) dataEntryFragment).checkMandatory();
                                hasError = ((DataEntryFragment) dataEntryFragment).checkErrors();
                            }
                            if (mandatoryOk && !hasError) {

                                if (!isEmpty(formFragment.getMessageOnComplete())) {
                                    final AlertDialog dialog = view.showInfoDialog(view.getContext().getString(R.string.warning_error_on_complete_title), formFragment.getMessageOnComplete(), new OnDialogClickListener() {
                                        @Override
                                        public void onPossitiveClick(AlertDialog alertDialog) {
                                            updateEventStatus(eventModel);
                                        }

                                        @Override
                                        public void onNegativeClick(AlertDialog alertDialog) {

                                        }
                                    });
                                    dialog.show();
                                } else {
                                    updateEventStatus(eventModel);
                                }
                            } else if (!mandatoryOk)
                                view.showInfoDialog(view.getContext().getString(R.string.unable_to_complete), view.getAbstractActivity().getString(R.string.missing_mandatory_fields));
                            else
                                view.showInfoDialog(view.getContext().getString(R.string.unable_to_complete), view.getAbstracContext().getString(R.string.field_errors));
                        }
                    }
                }, 1500);
            }
        } else
            view.displayMessage(null);
    }

    private void updateEventStatus(Event eventModel) {
        dataEntryStore.updateEventStatus(eventModel);
        changedEventStatus = true;
    }

    @Override
    public void confirmDeleteEvent() {
        view.showConfirmDeleteEvent();
    }

    @Override
    public void deleteEvent() {
        if (eventDetailModel != null && eventDetailModel.getEvent() != null) {
            if (eventDetailModel.getEvent().state() == State.TO_POST) {
                eventDetailRepository.deleteNotPostedEvent(eventDetailModel.getEvent().uid());
            } else {
                eventDetailRepository.deletePostedEvent(eventDetailModel.getEvent());
            }
            view.showEventWasDeleted();
        }
    }

    @Override
    public void onOrgUnitClick() {

        OrgUnitDialog orgUnitDialog = OrgUnitDialog.getInstace().setMultiSelection(false);
        orgUnitDialog.setTitle("Event Org Unit")
                .setPossitiveListener(v -> {
                    if (orgUnitDialog.getSelectedOrgUnitModel() == null)
                        orgUnitDialog.dismiss();
                    view.setSelectedOrgUnit(orgUnitDialog.getSelectedOrgUnitModel());
                    orgUnitDialog.dismiss();
                })
                .setNegativeListener(v -> orgUnitDialog.dismiss());

        disposable.add(eventDetailRepository.getOrgUnits()
                .subscribeOn(schedulerProvider.io())
                .observeOn(schedulerProvider.ui())
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

        if (eventDetailModel.getProgramStage().periodType() == null || eventDetailModel.getProgramStage().periodType() == PeriodType.Daily)
            openDailySelector(false);
        else
            openPeriodSelector(false);


    }

    @Override
    public void setDueDate() {

        if (eventDetailModel.getProgramStage().periodType() == null || eventDetailModel.getProgramStage().periodType() == PeriodType.Daily)
            openDailySelector(true);
        else
            openPeriodSelector(true);


    }

    @Override
    public void selectCatOption() {
        view.showCatOptionDialog();
    }

    @Override
    public void changeCatOption(CategoryOptionCombo selectedOption) {
        eventDetailRepository.saveCatOption(selectedOption);
    }

    private void showNativeCalendar(boolean futureOnly) {
        Calendar c = Calendar.getInstance();
        if (futureOnly)
            c.add(Calendar.DAY_OF_YEAR, 1);
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

                    if (eventDetailModel.getProgramStage().access().data().write()) {
                        dataEntryStore.updateEvent(selectedDate, eventDetailModel.getEvent());
                    }
                }),
                year,
                month,
                day);
        if (eventDetailModel.getEvent().status() != EventStatus.SCHEDULE && eventDetailModel.getEvent().status() != EventStatus.OVERDUE) {
            dateDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        }

        if (futureOnly)
            dateDialog.getDatePicker().setMinDate(c.getTimeInMillis());

        if (eventDetailModel.getProgram().expiryPeriodType() != null) {
            Date minDate = DateUtils.getInstance().expDate(null,
                    eventDetailModel.getProgram().expiryDays() != null ? eventDetailModel.getProgram().expiryDays() : 0,
                    eventDetailModel.getProgram().expiryPeriodType());
            dateDialog.getDatePicker().setMinDate(minDate.getTime());
        }

        if (eventDetailModel.orgUnitClosingDate() != null)
            dateDialog.getDatePicker().setMaxDate(eventDetailModel.orgUnitClosingDate().getTime());

        dateDialog.setButton(DialogInterface.BUTTON_NEGATIVE, view.getContext().getString(R.string.date_dialog_clear), (dialog, which) -> {
        });

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            dateDialog.setButton(DialogInterface.BUTTON_NEUTRAL,
                    view.getContext().getResources().getString(R.string.change_calendar), (dialog, which) -> {
                        dateDialog.dismiss();
                        openDailySelector(futureOnly);
                    });
        }
        dateDialog.show();
    }


    private void openDailySelector(boolean futureOnly) {
        LayoutInflater layoutInflater = LayoutInflater.from(view.getContext());
        WidgetDatepickerBinding widgetBinding = WidgetDatepickerBinding.inflate(layoutInflater);
        final DatePicker datePicker = widgetBinding.widgetDatepicker;

        Calendar c = Calendar.getInstance();
        if (futureOnly)
            c.add(Calendar.DAY_OF_YEAR, 1);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        datePicker.updateDate(year, month, day);
        datePicker.setMaxDate(c.getTimeInMillis());

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(view.getContext(), R.style.DatePickerTheme);

        if (eventDetailModel.getEvent().status() != EventStatus.SCHEDULE && eventDetailModel.getEvent().status() != EventStatus.OVERDUE) {
            datePicker.setMaxDate(System.currentTimeMillis());
        }

        if (futureOnly)
            datePicker.setMinDate(c.getTimeInMillis());

        if (eventDetailModel.getProgram().expiryPeriodType() != null) {
            Date minDate = DateUtils.getInstance().expDate(null,
                    eventDetailModel.getProgram().expiryDays() != null ? eventDetailModel.getProgram().expiryDays() : 0,
                    eventDetailModel.getProgram().expiryPeriodType());
            datePicker.setMinDate(minDate.getTime());
        }

        if (eventDetailModel.orgUnitClosingDate() != null)
            datePicker.setMaxDate(eventDetailModel.orgUnitClosingDate().getTime());


        alertDialog.setView(widgetBinding.getRoot());
        Dialog dialog = alertDialog.create();

        widgetBinding.changeCalendarButton.setOnClickListener(calendarButton->{
            showNativeCalendar(futureOnly);
            dialog.dismiss();
        });
        widgetBinding.clearButton.setOnClickListener(clearButton->dialog.dismiss());
        widgetBinding.acceptButton.setOnClickListener(acceptButton->{
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(Calendar.YEAR, datePicker.getYear());
            selectedCalendar.set(Calendar.MONTH, datePicker.getMonth());
            selectedCalendar.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
            selectedCalendar.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY));
            selectedCalendar.set(Calendar.MINUTE, c.get(Calendar.MINUTE));
            Date selectedDate = selectedCalendar.getTime();
            String result = DateUtils.uiDateFormat().format(selectedDate);
            view.setDate(result);

            if (eventDetailModel.getProgramStage().access().data().write()) {
                dataEntryStore.updateEvent(selectedDate, eventDetailModel.getEvent());
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void openPeriodSelector(Boolean futureOnly) {
        PeriodDialog periodDialog = new PeriodDialog()
                .setPeriod(eventDetailModel.getProgramStage().periodType())
                .setPossitiveListener(selectedDate -> {
                    String result = DateUtils.uiDateFormat().format(selectedDate);
                    view.setDate(result);

                    if (eventDetailModel.getProgramStage().access().data().write()) {
                        dataEntryStore.updateEvent(selectedDate, eventDetailModel.getEvent());
                    }
                });

        if (eventDetailModel.getEvent().status() != EventStatus.SCHEDULE && eventDetailModel.getEvent().status() != EventStatus.OVERDUE) {
            periodDialog.setMaxDate(Calendar.getInstance().getTime());
        }

        if (eventDetailModel.orgUnitOpeningDate() != null) {
            periodDialog.setMinDate(eventDetailModel.orgUnitOpeningDate());
        }

        if (futureOnly && eventDetailModel.orgUnitOpeningDate().before(Calendar.getInstance().getTime())) {
            periodDialog.setMinDate(Calendar.getInstance().getTime());
        }

        if (eventDetailModel.orgUnitClosingDate() != null)
            periodDialog.setMaxDate(eventDetailModel.orgUnitClosingDate());

        periodDialog.show(view.getAbstractActivity().getSupportFragmentManager(), PeriodDialog.class.getSimpleName());
    }

    @Override
    public void onDettach() {
        disposable.clear();
    }

    @Override
    public void displayMessage(String message) {
        view.displayMessage(message);
    }
}