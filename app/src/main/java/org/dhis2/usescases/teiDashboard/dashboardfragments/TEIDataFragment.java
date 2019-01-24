package org.dhis2.usescases.teiDashboard.dashboardfragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.databinding.FragmentTeiDataBinding;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionActivity;
import org.dhis2.usescases.teiDashboard.DashboardProgramModel;
import org.dhis2.usescases.teiDashboard.TeiDashboardContracts;
import org.dhis2.usescases.teiDashboard.adapters.DashboardProgramAdapter;
import org.dhis2.usescases.teiDashboard.adapters.EventAdapter;
import org.dhis2.usescases.teiDashboard.mobile.TeiDashboardMobileActivity;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.DialogClickListener;
import org.dhis2.utils.EventCreationType;
import org.dhis2.utils.custom_views.CustomDialog;
import org.dhis2.utils.custom_views.PeriodDialog;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.period.PeriodType;
import org.hisp.dhis.android.core.program.ProgramStageModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;

import static android.app.Activity.RESULT_OK;
import static org.dhis2.utils.Constants.ENROLLMENT_UID;
import static org.dhis2.utils.Constants.EVENT_CREATION_TYPE;
import static org.dhis2.utils.Constants.EVENT_SCHEDULE_INTERVAL;
import static org.dhis2.utils.Constants.ORG_UNIT;
import static org.dhis2.utils.Constants.PROGRAM_UID;
import static org.dhis2.utils.Constants.TRACKED_ENTITY_INSTANCE;

/**
 * -Created by ppajuelo on 29/11/2017.
 */

public class TEIDataFragment extends FragmentGlobalAbstract implements DialogClickListener {

    private static final int REQ_DETAILS = 1001;
    private static final int REQ_EVENT = 2001;

    private static final int RC_GENERATE_EVENT = 1501;
    private static final int RC_EVENTS_COMPLETED = 1601;


    FragmentTeiDataBinding binding;

    static TEIDataFragment instance;
    TeiDashboardContracts.Presenter presenter;

    private EventAdapter adapter;
    private CustomDialog dialog;
    private String lastModifiedEventUid;
    private ProgramStageModel programStageFromEvent;
    private ObservableBoolean followUp = new ObservableBoolean(false);

    private boolean hasCatComb;
    private ArrayList<EventModel> catComboShowed = new ArrayList<>();


    public static TEIDataFragment getInstance() {
        if (instance == null)
            instance = new TEIDataFragment();

        return instance;
    }

    public static TEIDataFragment createInstance() {
        return instance = new TEIDataFragment();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        presenter = ((TeiDashboardMobileActivity) context).getPresenter();
    }

    @Override
    public void onDestroy() {
        instance = null;
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tei_data, container, false);
        binding.setPresenter(presenter);

        binding.fab.setOptionsClick(integer -> {
            if (integer == null)
                return;
            switch (integer) {
                case R.id.referral:
                    createEvent(EventCreationType.REFERAL, 0);
                    break;
                case R.id.addnew:
                    createEvent(EventCreationType.ADDNEW, 0);
                    break;
                case R.id.schedulenew:
                    createEvent(EventCreationType.SCHEDULE, 0);
                    break;
                default:
                    break;
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter = ((TeiDashboardMobileActivity) getActivity()).getPresenter();

        binding.setPresenter(presenter);

        setData(presenter.getDashBoardData());
    }

    public void setData(DashboardProgramModel nprogram) {

        if (nprogram != null && nprogram.getCurrentEnrollment() != null) {
            SharedPreferences prefs = getContext().getSharedPreferences(Constants.SHARE_PREFS, Context.MODE_PRIVATE);
            hasCatComb = !nprogram.getCurrentProgram().categoryCombo().equals(prefs.getString(Constants.DEFAULT_CAT_COMBO, ""));
            List<EventModel> events = new ArrayList<>();
            adapter = new EventAdapter(presenter, nprogram.getProgramStages(), events, nprogram.getCurrentEnrollment());
            binding.teiRecycler.setLayoutManager(new LinearLayoutManager(getAbstracContext()));
            binding.teiRecycler.setAdapter(adapter);
            binding.setTrackEntity(nprogram.getTei());
            binding.setEnrollment(nprogram.getCurrentEnrollment());
            binding.setProgram(nprogram.getCurrentProgram());
            binding.setDashboardModel(nprogram);
            presenter.getTEIEvents(this);
            followUp.set(nprogram.getCurrentEnrollment().followUp() != null ? nprogram.getCurrentEnrollment().followUp() : false);
            binding.setFollowup(followUp);

        } else if (nprogram != null) {
            binding.fab.setVisibility(View.GONE);
            binding.teiRecycler.setLayoutManager(new LinearLayoutManager(getAbstracContext()));
            binding.teiRecycler.setAdapter(new DashboardProgramAdapter(presenter, nprogram));
            binding.teiRecycler.addItemDecoration(new DividerItemDecoration(getAbstracContext(), DividerItemDecoration.VERTICAL));
            binding.setTrackEntity(nprogram.getTei());
            binding.setEnrollment(null);
            binding.setProgram(null);
            binding.setDashboardModel(nprogram);
            binding.setFollowup(followUp);
        }

        binding.executePendingBindings();

    }

    public static int getDetailsRequestCode() {
        return REQ_DETAILS;
    }

    public static int getEventRequestCode() {
        return REQ_EVENT;
    }

    @SuppressLint("CheckResult")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_DETAILS) {
            if (resultCode == RESULT_OK) {
                presenter.getData();
            }
        }
        if (requestCode == REQ_EVENT && resultCode == RESULT_OK) {
            presenter.getTEIEvents(this);
            if (data != null) {
                lastModifiedEventUid = data.getStringExtra(Constants.EVENT_UID);
                if (lastModifiedEventUid != null)
                    presenter.displayGenerateEvent(this, lastModifiedEventUid);
            }

        }
    }

    public Consumer<List<EventModel>> setEvents() {
        return events -> {
            adapter.swapItems(events);
            for (EventModel event : events) {
                if (event.eventDate() != null) {
                    if (event.eventDate().after(DateUtils.getInstance().getToday()))
                        binding.teiRecycler.scrollToPosition(events.indexOf(event));
                }
                if (hasCatComb && event.attributeOptionCombo() == null && !catComboShowed.contains(event)) {
                    presenter.getCatComboOptions(event);
                    catComboShowed.add(event);
                }
            }
        };
    }

    public Consumer<ProgramStageModel> displayGenerateEvent() {
        return programStageModel -> {
            this.programStageFromEvent = programStageModel;
            if (programStageModel.displayGenerateEventBox() || programStageModel.allowGenerateNextVisit()) {
                dialog = new CustomDialog(
                        getContext(),
                        getString(R.string.dialog_generate_new_event),
                        getString(R.string.message_generate_new_event),
                        getString(R.string.button_ok),
                        getString(R.string.cancel),
                        RC_GENERATE_EVENT,
                        this);
                dialog.show();
            } else if (programStageModel.remindCompleted())
                presenter.areEventsCompleted(this);
        };
    }


    public Consumer<Single<Boolean>> areEventsCompleted() {
        return eventsCompleted -> {
            if (eventsCompleted.blockingGet()) {
                dialog = new CustomDialog(
                        getContext(),
                        getString(R.string.event_completed_title),
                        getString(R.string.event_completed_message),
                        getString(R.string.button_ok),
                        getString(R.string.cancel),
                        RC_EVENTS_COMPLETED,
                        this);
                dialog.show();
            }

        };
    }

    public Consumer<EnrollmentStatus> enrollmentCompleted() {
        return enrollmentStatus -> {
            if (enrollmentStatus == EnrollmentStatus.COMPLETED)
                presenter.getData();
        };
    }

    @Override
    public void onPositive() {
        switch (dialog.getRequestCode()) {
            case RC_EVENTS_COMPLETED:
                presenter.completeEnrollment(this);
                break;
            case RC_GENERATE_EVENT:
                createEvent(EventCreationType.SCHEDULE, programStageFromEvent.standardInterval() != null ? programStageFromEvent.standardInterval() : 0);
                /*if (programStageFromEvent.standardInterval() != null && programStageFromEvent.standardInterval() > 0) {
//                    presenter.generateEvent(lastModifiedEventUid, programStageFromEvent.standardInterval());
                    createEvent(EventCreationType.SCHEDULE, programStageFromEvent.standardInterval());
                } else {
                    if (programStageFromEvent.periodType() == null || programStageFromEvent.periodType() == PeriodType.Daily) {
                        Calendar calendar = Calendar.getInstance();
                        DatePickerDialog datePickerDialog = new DatePickerDialog(getAbstracContext(), (view, year, month, dayOfMonth) -> {
                            Calendar chosenDate = Calendar.getInstance();
                            chosenDate.set(year, month, dayOfMonth);
                            presenter.generateEventFromDate(lastModifiedEventUid, chosenDate);
                        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
                        if (programStageFromEvent != null && programStageFromEvent.hideDueDate())
                            datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
                        else {
                            // ONLY FUTURE DATES
                            calendar.add(Calendar.DAY_OF_YEAR, 1);
                            datePickerDialog.getDatePicker().setMinDate(calendar.getTimeInMillis());
                        }
                        datePickerDialog.show();
                    } else {
                        new PeriodDialog()
                                .setPeriod(programStageFromEvent.periodType())
                                .setMinDate(DateUtils.getInstance().getNextPeriod(programStageFromEvent.periodType(), Calendar.getInstance().getTime(), 0))
                                .setPossitiveListener(selectedDate -> {
                                    Calendar chosenDate = Calendar.getInstance();
                                    chosenDate.setTime(selectedDate);
                                    presenter.generateEventFromDate(lastModifiedEventUid, chosenDate);
                                })
                                .show(getChildFragmentManager(), PeriodDialog.class.getSimpleName());
                    }
                }*/
                break;
            default:
                break;
        }
    }

    private void createEvent(EventCreationType eventCreationType, Integer scheduleIntervalDays) {
        Bundle bundle = new Bundle();
        bundle.putString(PROGRAM_UID, presenter.getDashBoardData().getCurrentEnrollment().program());
        bundle.putString(TRACKED_ENTITY_INSTANCE, presenter.getTeUid());
        bundle.putString(ORG_UNIT, presenter.getDashBoardData().getTei().organisationUnit()); //We take the OU of the TEI for the events
        bundle.putString(ENROLLMENT_UID, presenter.getDashBoardData().getCurrentEnrollment().uid());
        bundle.putString(EVENT_CREATION_TYPE, eventCreationType.name());
        bundle.putInt(EVENT_SCHEDULE_INTERVAL, scheduleIntervalDays);
        Intent intent = new Intent(getContext(), ProgramStageSelectionActivity.class);
        intent.putExtras(bundle);
        startActivityForResult(intent, REQ_EVENT);
    }

    @Override
    public void onNegative() {
        if (dialog.getRequestCode() == RC_GENERATE_EVENT && programStageFromEvent.remindCompleted())
            presenter.areEventsCompleted(this);
    }

    public void switchFollowUp(boolean followUp) {
        this.followUp.set(followUp);
    }
}