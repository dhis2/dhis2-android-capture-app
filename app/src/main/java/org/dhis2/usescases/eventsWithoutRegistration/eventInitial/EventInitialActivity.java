package org.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import static org.dhis2.commons.Constants.ENROLLMENT_UID;
import static org.dhis2.commons.Constants.EVENT_CREATION_TYPE;
import static org.dhis2.commons.Constants.EVENT_PERIOD_TYPE;
import static org.dhis2.commons.Constants.ORG_UNIT;
import static org.dhis2.commons.Constants.PERMANENT;
import static org.dhis2.commons.Constants.PROGRAM_UID;
import static org.dhis2.commons.Constants.TRACKED_ENTITY_INSTANCE;
import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.CREATE_EVENT;
import static org.dhis2.utils.analytics.AnalyticsConstants.DELETE_EVENT;
import static org.dhis2.utils.analytics.AnalyticsConstants.SHOW_HELP;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.SparseBooleanArray;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentTransaction;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.commons.data.EventCreationType;
import org.dhis2.commons.dialogs.CustomDialog;
import org.dhis2.commons.dialogs.DialogClickListener;
import org.dhis2.commons.popupmenu.AppMenuHelper;
import org.dhis2.databinding.ActivityEventInitialBinding;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity;
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.injection.EventDetailsComponent;
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.injection.EventDetailsComponentProvider;
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.injection.EventDetailsModule;
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventDetails;
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.ui.EventDetailsFragment;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.qrCodes.eventsworegistration.QrEventsWORegistrationActivity;
import org.dhis2.commons.Constants;
import org.dhis2.utils.EventMode;
import org.dhis2.utils.HelpManager;
import org.dhis2.utils.analytics.AnalyticsConstants;
import org.hisp.dhis.android.core.common.Geometry;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.period.PeriodType;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;

import java.util.Objects;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import kotlin.Unit;

public class EventInitialActivity extends ActivityGlobalAbstract implements EventInitialContract.View, EventDetailsComponentProvider {

    @Inject
    EventInitialPresenter presenter;

    private ActivityEventInitialBinding binding;

    //Bundle variables
    private String programUid;
    private String eventUid;
    private EventCreationType eventCreationType;
    private String getTrackedEntityInstance;
    private String enrollmentUid;
    private String selectedOrgUnit;
    private PeriodType periodType;
    private String programStageUid;
    private EnrollmentStatus enrollmentStatus;
    private int eventScheduleInterval;

    private ProgramStage programStage;
    private Program program;
    private Boolean accessData;
    private EventDetails eventDetails = new EventDetails();

    private final CompositeDisposable disposable = new CompositeDisposable();

    public EventInitialComponent eventInitialComponent;

    public static Bundle getBundle(String programUid, String eventUid, String eventCreationType,
                                   String teiUid, PeriodType eventPeriodType, String orgUnit, String stageUid,
                                   String enrollmentUid, int eventScheduleInterval, EnrollmentStatus enrollmentStatus) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.PROGRAM_UID, programUid);
        bundle.putString(Constants.EVENT_UID, eventUid);
        bundle.putString(Constants.EVENT_CREATION_TYPE, eventCreationType);
        bundle.putString(Constants.TRACKED_ENTITY_INSTANCE, teiUid);
        bundle.putString(Constants.ENROLLMENT_UID, enrollmentUid);
        bundle.putString(Constants.ORG_UNIT, orgUnit);
        bundle.putSerializable(Constants.EVENT_PERIOD_TYPE, eventPeriodType);
        bundle.putString(Constants.PROGRAM_STAGE_UID, stageUid);
        bundle.putInt(Constants.EVENT_SCHEDULE_INTERVAL, eventScheduleInterval);
        bundle.putSerializable(Constants.ENROLLMENT_STATUS, enrollmentStatus);
        return bundle;
    }

    private void initVariables() {
        programUid = getIntent().getStringExtra(PROGRAM_UID);
        eventUid = getIntent().getStringExtra(Constants.EVENT_UID);
        eventCreationType = getIntent().getStringExtra(EVENT_CREATION_TYPE) != null ?
                EventCreationType.valueOf(getIntent().getStringExtra(EVENT_CREATION_TYPE)) :
                EventCreationType.DEFAULT;
        getTrackedEntityInstance = getIntent().getStringExtra(TRACKED_ENTITY_INSTANCE);
        enrollmentUid = getIntent().getStringExtra(ENROLLMENT_UID);
        selectedOrgUnit = getIntent().getStringExtra(ORG_UNIT);
        periodType = (PeriodType) getIntent().getSerializableExtra(EVENT_PERIOD_TYPE);
        programStageUid = getIntent().getStringExtra(Constants.PROGRAM_STAGE_UID);
        enrollmentStatus = (EnrollmentStatus) getIntent().getSerializableExtra(Constants.ENROLLMENT_STATUS);
        eventScheduleInterval = getIntent().getIntExtra(Constants.EVENT_SCHEDULE_INTERVAL, 0);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        initVariables();
        eventInitialComponent = Objects.requireNonNull(((App) getApplicationContext()).userComponent())
                .plus(
                        new EventInitialModule(this,
                                eventUid,
                                programStageUid,
                                getContext())
                );
        eventInitialComponent.inject(this);
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_initial);
        binding.setPresenter(presenter);

        initProgressBar();

        Bundle bundle = new Bundle();
        bundle.putString(Constants.EVENT_UID, eventUid);
        bundle.putString(Constants.EVENT_CREATION_TYPE, getIntent().getStringExtra(EVENT_CREATION_TYPE));
        bundle.putString(Constants.PROGRAM_STAGE_UID, programStageUid);
        bundle.putString(PROGRAM_UID, programUid);
        bundle.putSerializable(Constants.EVENT_PERIOD_TYPE, periodType);
        bundle.putString(Constants.ENROLLMENT_UID, enrollmentUid);
        bundle.putInt(Constants.EVENT_SCHEDULE_INTERVAL, eventScheduleInterval);
        bundle.putString(Constants.ORG_UNIT, selectedOrgUnit);
        bundle.putSerializable(Constants.ENROLLMENT_STATUS, enrollmentStatus);

        EventDetailsFragment eventDetailsFragment = new EventDetailsFragment();
        eventDetailsFragment.setArguments(bundle);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentDetailsContainer, eventDetailsFragment).commit();

        eventDetailsFragment.setOnEventDetailsChange(eventDetails -> {
            this.eventDetails = eventDetails;
            return Unit.INSTANCE;
        });
        eventDetailsFragment.setOnButtonCallback(() -> {
            onActionButtonClick();
            return Unit.INSTANCE;
        });
        presenter.init(programUid, eventUid, selectedOrgUnit, programStageUid);
    }

    private void onActionButtonClick() {
        String programStageModelUid = programStage == null ? "" : programStage.uid();
        Geometry geometry = null;
        if (eventDetails.getCoordinates() != null) {
            geometry = Geometry.builder()
                    .coordinates(eventDetails.getCoordinates())
                    .type(programStage.featureType())
                    .build();
        }

        if (eventUid == null) { // This is a new Event
            presenter.onEventCreated();
            analyticsHelper().setEvent(CREATE_EVENT, AnalyticsConstants.DATA_CREATION, CREATE_EVENT);
            if (eventCreationType == EventCreationType.REFERAL && eventDetails.getTemCreate() != null && eventDetails.getTemCreate().equals(PERMANENT)) {
                presenter.scheduleEventPermanent(
                        enrollmentUid,
                        getTrackedEntityInstance,
                        programStageModelUid,
                        eventDetails.getSelectedDate(),
                        eventDetails.getSelectedOrgUnit(),
                        null,
                        eventDetails.getCatOptionComboUid(),
                        geometry
                );
            } else if (eventCreationType == EventCreationType.SCHEDULE || eventCreationType == EventCreationType.REFERAL) {
                presenter.scheduleEvent(
                        enrollmentUid,
                        programStageModelUid,
                        eventDetails.getSelectedDate(),
                        eventDetails.getSelectedOrgUnit(),
                        null,
                        eventDetails.getCatOptionComboUid(),
                        geometry
                );
            } else {
                presenter.createEvent(
                        enrollmentUid,
                        programStageModelUid,
                        eventDetails.getSelectedDate(),
                        eventDetails.getSelectedOrgUnit(),
                        null,
                        eventDetails.getCatOptionComboUid(),
                        geometry,
                        getTrackedEntityInstance);
            }
        }else{
            startFormActivity(eventUid,false);
        }
    }

    @Override
    protected void onDestroy() {
        presenter.onDettach();
        disposable.dispose();
        super.onDestroy();
    }

    private void initProgressBar() {
        if (eventUid != null && presenter.getCompletionPercentageVisibility()) {
            binding.completion.setVisibility(View.VISIBLE);
        } else {
            binding.completion.setVisibility(View.GONE);
        }
    }

    @Override
    public void setProgram(@NonNull Program program) {
        this.program = program;

        setUpActivityTitle();
    }

    private void setUpActivityTitle() {
        String activityTitle;
        if (eventCreationType == EventCreationType.REFERAL) {
            activityTitle = program.displayName() + " - " + getString(R.string.referral);
        } else {

            activityTitle = eventUid == null ? program.displayName() + " - " + getString(R.string.new_event) : program.displayName();
        }
        binding.setName(activityTitle);
    }

    @Override
    public void onEventCreated(String eventUid) {
        showToast(getString(R.string.event_created));
        if (eventCreationType != EventCreationType.SCHEDULE && eventCreationType != EventCreationType.REFERAL) {
            startFormActivity(eventUid, true);
        } else {
            finish();
        }
    }

    @Override
    public void onEventUpdated(String eventUid) {
        startFormActivity(eventUid, false);
    }

    private void startFormActivity(String eventUid, boolean isNew) {
        Intent intent = new Intent(this, EventCaptureActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        intent.putExtras(EventCaptureActivity.getActivityBundle(eventUid, programUid, isNew ? EventMode.NEW : EventMode.CHECK));
        startActivity(intent);
        finish();
    }

    @Override
    public void setProgramStage(ProgramStage programStage) {
        this.programStage = programStage;
        binding.setProgramStage(programStage);

        if (periodType == null)
            periodType = programStage.periodType();
    }

    @Override
    public void updatePercentage(float primaryValue) {
        binding.completion.setCompletionPercentage(primaryValue);
    }

    @Override
    public void showProgramStageSelection() {
        presenter.getProgramStage(programStageUid);
    }

    @Override
    public void setAccessDataWrite(Boolean canWrite) {
        this.accessData = canWrite;
    }

    @Override
    public void showQR() {
        Intent intent = new Intent(EventInitialActivity.this, QrEventsWORegistrationActivity.class);
        intent.putExtra(Constants.EVENT_UID, eventUid);
        startActivity(intent);
    }

    @Override
    public void setTutorial() {

        new Handler().postDelayed(() -> {
            SparseBooleanArray stepConditions = new SparseBooleanArray();
            stepConditions.put(0, eventUid == null);
            HelpManager.getInstance().show(getActivity(), HelpManager.TutorialName.EVENT_INITIAL, stepConditions);
        }, 500);
    }

    @Override
    public void showMoreOptions(View view) {
        new AppMenuHelper.Builder().menu(this, R.menu.event_menu).anchor(view)
                .onMenuInflated(popupMenu -> {
                    popupMenu.getMenu().findItem(R.id.menu_delete).setVisible(accessData && presenter.isEnrollmentOpen());
                    popupMenu.getMenu().findItem(R.id.menu_share).setVisible(eventUid != null);
                    return Unit.INSTANCE;
                })
                .onMenuItemClicked(itemId -> {
                    switch (itemId) {
                        case R.id.showHelp:
                            analyticsHelper().setEvent(SHOW_HELP, CLICK, SHOW_HELP);
                            setTutorial();
                            break;
                        case R.id.menu_delete:
                            confirmDeleteEvent();
                            break;
                        case R.id.menu_share:
                            presenter.onShareClick();
                            break;
                        default:
                            break;
                    }
                    return false;
                })
                .build()
                .show();
    }

    public void confirmDeleteEvent() {
        new CustomDialog(
                this,
                getString(R.string.delete_event),
                getString(R.string.confirm_delete_event),
                getString(R.string.delete),
                getString(R.string.cancel),
                0,
                new DialogClickListener() {
                    @Override
                    public void onPositive() {
                        analyticsHelper().setEvent(DELETE_EVENT, CLICK, DELETE_EVENT);
                        presenter.deleteEvent(getTrackedEntityInstance);
                    }

                    @Override
                    public void onNegative() {
                        // dismiss
                    }
                }
        ).show();
    }

    @Override
    public void showEventWasDeleted() {
        showToast(getString(R.string.event_was_deleted));
        finish();
    }

    @Nullable
    @Override
    public EventDetailsComponent provideEventDetailsComponent(@Nullable EventDetailsModule module) {
        return eventInitialComponent.plus(module);
    }
}
