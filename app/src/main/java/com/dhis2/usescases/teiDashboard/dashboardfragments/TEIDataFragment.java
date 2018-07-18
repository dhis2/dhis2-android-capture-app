package com.dhis2.usescases.teiDashboard.dashboardfragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;

import com.dhis2.R;
import com.dhis2.databinding.FragmentTeiDataBinding;
import com.dhis2.usescases.general.FragmentGlobalAbstract;
import com.dhis2.usescases.programStageSelection.ProgramStageSelectionActivity;
import com.dhis2.usescases.teiDashboard.DashboardProgramModel;
import com.dhis2.usescases.teiDashboard.TeiDashboardContracts;
import com.dhis2.usescases.teiDashboard.adapters.DashboardProgramAdapter;
import com.dhis2.usescases.teiDashboard.adapters.EventAdapter;
import com.dhis2.usescases.teiDashboard.mobile.TeiDashboardMobileActivity;
import com.dhis2.utils.Constants;
import com.dhis2.utils.CustomViews.CustomDialog;
import com.dhis2.utils.DateUtils;
import com.dhis2.utils.DialogClickListener;

import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.functions.Consumer;

import static android.app.Activity.RESULT_OK;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.ADDNEW;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.ENROLLMENT_UID;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.EVENT_CREATION_TYPE;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.NEW_EVENT;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.ORG_UNIT;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.PROGRAM_UID;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.REFERRAL;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.SCHEDULENEW;
import static com.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity.TRACKED_ENTITY_INSTANCE;

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

    private DashboardProgramModel dashboardProgramModel;
    private EventAdapter adapter;
    private List<EventModel> events = new ArrayList<>();
    private CustomDialog dialog;
    private String lastModifiedEventUid;
    private ProgramStageModel programStageFromEvent;
    private Context context;

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
        this.context = context;
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

            Bundle bundle = new Bundle();
            bundle.putString(PROGRAM_UID, presenter.getDashBoardData().getCurrentEnrollment().program());
            bundle.putString(TRACKED_ENTITY_INSTANCE, presenter.getTeUid());
            bundle.putString(ORG_UNIT, presenter.getDashBoardData().getCurrentEnrollment().organisationUnit());
            bundle.putString(ENROLLMENT_UID, presenter.getDashBoardData().getCurrentEnrollment().uid());
            bundle.putBoolean(NEW_EVENT, true);

            switch (integer) {
                case R.id.referral:
                    bundle.putString(EVENT_CREATION_TYPE, REFERRAL);
                    break;
                case R.id.addnew:
                    bundle.putString(EVENT_CREATION_TYPE, ADDNEW);
                    break;
                case R.id.schedulenew:
                    bundle.putString(EVENT_CREATION_TYPE, SCHEDULENEW);
                    break;
                default:
                    break;
            }

            startActivity(ProgramStageSelectionActivity.class, bundle, false, false, null);

        });
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setData(dashboardProgramModel);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter = ((TeiDashboardMobileActivity) getActivity()).getPresenter();
        binding.setPresenter(presenter);
        if (dashboardProgramModel != null)
            setData(dashboardProgramModel);
    }

    public void setData(DashboardProgramModel nprogram) {
        this.dashboardProgramModel = nprogram;

        if (nprogram != null && nprogram.getCurrentEnrollment() != null) {
            this.events = new ArrayList<>();
            adapter = new EventAdapter(presenter, nprogram.getProgramStages(), events, nprogram.getCurrentEnrollment());
            binding.teiRecycler.setLayoutManager(new LinearLayoutManager(getAbstracContext()));
            binding.teiRecycler.setAdapter(adapter);
            binding.setTrackEntity(nprogram.getTei());
            binding.setEnrollment(nprogram.getCurrentEnrollment());
            binding.setProgram(nprogram.getCurrentProgram());
            binding.setDashboardModel(nprogram);
            presenter.getTEIEvents(this);

        } else if (nprogram != null) {
            binding.fab.setVisibility(View.GONE);
            binding.teiRecycler.setLayoutManager(new LinearLayoutManager(getAbstracContext()));
            binding.teiRecycler.setAdapter(new DashboardProgramAdapter(presenter, nprogram));
            binding.teiRecycler.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
            binding.setTrackEntity(nprogram.getTei());
            binding.setEnrollment(null);
            binding.setProgram(null);
            binding.setDashboardModel(nprogram);
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
            }
        };
    }

    public Consumer<ProgramStageModel> displayGenerateEvent() {
        return programStageModel -> {
            this.programStageFromEvent = programStageModel;
            if (programStageModel.displayGenerateEventBox()) {
                dialog = new CustomDialog(
                        getContext(),
                        getString(R.string.dialog_generate_new_event),
                        getString(R.string.message_generate_new_event),
                        getString(R.string.button_ok),
                        getString(R.string.cancel),
                        RC_GENERATE_EVENT,
                        this);
                dialog.show();
            } else
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
                if (programStageFromEvent.standardInterval() != null && programStageFromEvent.standardInterval() > 0)
                    presenter.generateEvent(lastModifiedEventUid, programStageFromEvent.standardInterval());
                else {
                    //TODO: WHAT HAPPENS IF PROGRAM HAS A PERIOD
                    Calendar calendar = Calendar.getInstance();
                    DatePickerDialog datePickerDialog = new DatePickerDialog(context, (view, year, month, dayOfMonth) -> {
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
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onNegative() {
        if (dialog.getRequestCode() == RC_GENERATE_EVENT)
            presenter.areEventsCompleted(this);
    }
}