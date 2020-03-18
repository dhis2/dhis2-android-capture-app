package org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.databinding.FragmentTeiDataBinding;
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.usescases.orgunitselector.OUTreeActivity;
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionActivity;
import org.dhis2.usescases.teiDashboard.DashboardProgramModel;
import org.dhis2.usescases.teiDashboard.DashboardViewModel;
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity;
import org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data.tei_events.EventAdapter;
import org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data.tei_events.EventViewModel;
import org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data.tei_events.EventViewModelType;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.DialogClickListener;
import org.dhis2.utils.EventCreationType;
import org.dhis2.utils.ObjectStyleUtils;
import org.dhis2.utils.OrientationUtilsKt;
import org.dhis2.utils.customviews.CategoryComboDialog;
import org.dhis2.utils.customviews.CustomDialog;
import org.dhis2.utils.filters.FilterManager;
import org.dhis2.utils.filters.FiltersAdapter;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import timber.log.Timber;

import static android.app.Activity.RESULT_OK;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static org.dhis2.utils.Constants.ENROLLMENT_UID;
import static org.dhis2.utils.Constants.EVENT_CREATION_TYPE;
import static org.dhis2.utils.Constants.EVENT_PERIOD_TYPE;
import static org.dhis2.utils.Constants.EVENT_REPEATABLE;
import static org.dhis2.utils.Constants.EVENT_SCHEDULE_INTERVAL;
import static org.dhis2.utils.Constants.ORG_UNIT;
import static org.dhis2.utils.Constants.PROGRAM_UID;
import static org.dhis2.utils.Constants.TRACKED_ENTITY_INSTANCE;
import static org.dhis2.utils.analytics.AnalyticsConstants.CREATE_EVENT_TEI;
import static org.dhis2.utils.analytics.AnalyticsConstants.TYPE_EVENT_TEI;

/**
 * -Created by ppajuelo on 29/11/2017.
 */

public class TEIDataFragment extends FragmentGlobalAbstract implements TEIDataContracts.View {

    private static final int REQ_DETAILS = 1001;
    private static final int REQ_EVENT = 2001;

    private static final int RC_GENERATE_EVENT = 1501;
    private static final int RC_EVENTS_COMPLETED = 1601;


    private FragmentTeiDataBinding binding;

    @Inject
    TEIDataContracts.Presenter presenter;

    @Inject
    FilterManager filterManager;

    private EventAdapter adapter;
    private CustomDialog dialog;
    private String lastModifiedEventUid;
    private ProgramStage programStageFromEvent;
    private ObservableBoolean followUp = new ObservableBoolean(false);

    private boolean hasCatComb;
    private ArrayList<Event> catComboShowed = new ArrayList<>();
    private Context context;
    private DashboardViewModel dashboardViewModel;
    private DashboardProgramModel dashboardModel;
    private TeiDashboardMobileActivity activity;
    private FiltersAdapter filtersAdapter;

    public static TEIDataFragment newInstance(String programUid, String teiUid, String enrollmentUid) {
        TEIDataFragment fragment = new TEIDataFragment();
        Bundle args = new Bundle();
        args.putString("PROGRAM_UID", programUid);
        args.putString("TEI_UID", teiUid);
        args.putString("ENROLLMENT_UID", enrollmentUid);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        this.context = context;
        activity = (TeiDashboardMobileActivity) context;
        ((App) context.getApplicationContext())
                .dashboardComponent()
                .plus(new TEIDataModule(this,
                        getArguments().getString("PROGRAM_UID"),
                        getArguments().getString("TEI_UID"),
                        getArguments().getString("ENROLLMENT_UID")
                ))
                .inject(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        dashboardViewModel = ViewModelProviders.of(activity).get(DashboardViewModel.class);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tei_data, container, false);
        binding.setPresenter(presenter);
        activity.observeGrouping().observe(this, group -> {
            binding.setIsGrouping(group);
            presenter.onGroupingChanged(group);
        });
        activity.observeFilters().observe(this, showFilters -> showHideFilters(showFilters));
        activity.updatedEnrollment().observe(this, enrollmentUid -> updateEnrollment(enrollmentUid) );
        filtersAdapter = new FiltersAdapter(FiltersAdapter.ProgramType.TRACKER);
        filtersAdapter.addEventStatus();

        try {
            binding.filterLayout.setAdapter(filtersAdapter);
        } catch (Exception e) {
            Timber.e(e);
        }

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

    private void updateEnrollment(String enrollmentUid) {
        presenter.getEnrollment(enrollmentUid);
    }

    @Override
    public void setEnrollment(Enrollment enrollment) {
        binding.setEnrollment(enrollment);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.init();
        dashboardViewModel.dashboardModel().observe(this, this::setData);
        dashboardViewModel.eventUid().observe(this, this::displayGenerateEvent);
    }

    @Override
    public void onPause() {
        presenter.onDettach();
        super.onPause();
    }

    @Override
    public void setEnrollmentData(Program program, Enrollment enrollment) {
        binding.setProgram(program);
        binding.setEnrollment(enrollment);
        if (enrollment != null) {
            followUp.set(enrollment.followUp() != null ? enrollment.followUp() : false);
        }
        binding.setFollowup(followUp);
    }

    @Override
    public void setTrackedEntityInstance(TrackedEntityInstance trackedEntityInstance, OrganisationUnit organisationUnit) {
        binding.setTrackEntity(trackedEntityInstance);
        binding.cardFront.orgUnit.setText(organisationUnit.displayName());
    }

    public void setData(DashboardProgramModel nprogram) {
        this.dashboardModel = nprogram;

        if (nprogram != null && nprogram.getCurrentEnrollment() != null) {
            presenter.setDashboardProgram(this.dashboardModel);
            SharedPreferences prefs = context.getSharedPreferences(Constants.SHARE_PREFS, Context.MODE_PRIVATE);
            hasCatComb = nprogram.getCurrentProgram() != null && !nprogram.getCurrentProgram().categoryComboUid().equals(prefs.getString(Constants.DEFAULT_CAT_COMBO, ""));
            binding.setDashboardModel(nprogram);
        } else if (nprogram != null) {
            binding.fab.setVisibility(View.GONE);
            binding.teiRecycler.setAdapter(new DashboardProgramAdapter(presenter, nprogram));
            binding.teiRecycler.addItemDecoration(new DividerItemDecoration(getAbstracContext(), DividerItemDecoration.VERTICAL));
            binding.setDashboardModel(nprogram);
        }

        binding.executePendingBindings();

        if (getSharedPreferences().getString("COMPLETED_EVENT", null) != null) {
            presenter.displayGenerateEvent(getSharedPreferences().getString("COMPLETED_EVENT", null));
            getSharedPreferences().edit().remove("COMPLETED_EVENT").apply();
        }


    }

    @SuppressLint("CheckResult")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQ_EVENT) {
                if (data != null) {
                    lastModifiedEventUid = data.getStringExtra(Constants.EVENT_UID);
                    if (!OrientationUtilsKt.isLandscape())
                        getSharedPreferences().edit().putString("COMPLETED_EVENT", lastModifiedEventUid).apply();
                    else {
                        if (lastModifiedEventUid != null)
                            presenter.displayGenerateEvent(lastModifiedEventUid);
                    }
                }
            }
            if (requestCode == REQ_DETAILS) {
                activity.getPresenter().init();
            }
            if (requestCode == FilterManager.OU_TREE) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public Flowable<String> observeStageSelection(Program currentProgram, Enrollment currentEnrollment) {
        if (adapter == null) {
            adapter = new EventAdapter(presenter, currentProgram, currentEnrollment);
            binding.teiRecycler.setAdapter(adapter);
        }
        return adapter.stageSelector();
    }

    @Override
    public Consumer<List<EventViewModel>> setEvents() {
        return events -> {
            if (events.isEmpty()) {
                binding.emptyTeis.setVisibility(View.VISIBLE);
                if (binding.fab.getVisibility() == View.VISIBLE) {
                    binding.emptyTeis.setText(R.string.empty_tei_add);
                } else {
                    binding.emptyTeis.setText(R.string.empty_tei_no_add);
                }
            } else {
                binding.emptyTeis.setVisibility(View.GONE);
                adapter.submitList(events);

                for (EventViewModel eventViewModel : events) {
                    if (eventViewModel.getType() == EventViewModelType.EVENT) {
                        Event event = eventViewModel.getEvent();
                        if (event.eventDate() != null) {
                            if (event.eventDate().after(DateUtils.getInstance().getToday()))
                                binding.teiRecycler.scrollToPosition(events.indexOf(event));
                        }
                        if (hasCatComb && event.attributeOptionCombo() == null && !catComboShowed.contains(event)) {
                            presenter.getCatComboOptions(event);
                            catComboShowed.add(event);
                        } else if (!hasCatComb && event.attributeOptionCombo() == null)
                            presenter.setDefaultCatOptCombToEvent(event.uid());
                    }
                }
            }
        };
    }

    @Override
    public Consumer<ProgramStage> displayGenerateEvent() {
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
                        new DialogClickListener() {
                            @Override
                            public void onPositive() {
                                createEvent(EventCreationType.SCHEDULE, programStageFromEvent.standardInterval() != null ? programStageFromEvent.standardInterval() : 0);
                            }

                            @Override
                            public void onNegative() {
                                if (programStageFromEvent.remindCompleted())
                                    presenter.areEventsCompleted();
                            }
                        });
                dialog.show();
            } else if (programStageModel.remindCompleted())
                showDialogCloseProgram();
        };
    }

    private void showDialogCloseProgram() {

        dialog = new CustomDialog(
                getContext(),
                getString(R.string.event_completed),
                getString(R.string.complete_enrollment_message),
                getString(R.string.button_ok),
                getString(R.string.cancel),
                RC_EVENTS_COMPLETED,
                new DialogClickListener() {
                    @Override
                    public void onPositive() {
                        presenter.completeEnrollment();
                    }

                    @Override
                    public void onNegative() {
                    }
                });
        dialog.show();
    }

    @Override
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
                        new DialogClickListener() {
                            @Override
                            public void onPositive() {
                                presenter.completeEnrollment();
                            }

                            @Override
                            public void onNegative() {
                            }
                        });
                dialog.show();
            }

        };
    }

    @Override
    public Consumer<EnrollmentStatus> enrollmentCompleted() {
        return enrollmentStatus -> {
            if (enrollmentStatus == EnrollmentStatus.COMPLETED)
                activity.getPresenter().init();
        };
    }

    private void createEvent(EventCreationType eventCreationType, Integer scheduleIntervalDays) {
        if (isAdded()) {
            analyticsHelper().setEvent(TYPE_EVENT_TEI, eventCreationType.name(), CREATE_EVENT_TEI);
            Bundle bundle = new Bundle();
            bundle.putString(PROGRAM_UID, dashboardModel.getCurrentEnrollment().program());
            bundle.putString(TRACKED_ENTITY_INSTANCE, dashboardModel.getTei().uid());
            bundle.putString(ORG_UNIT, dashboardModel.getTei().organisationUnit()); //We take the OU of the TEI for the events
            bundle.putString(ENROLLMENT_UID, dashboardModel.getCurrentEnrollment().uid());
            bundle.putString(EVENT_CREATION_TYPE, eventCreationType.name());
            bundle.putInt(EVENT_SCHEDULE_INTERVAL, scheduleIntervalDays);
            Intent intent = new Intent(getContext(), ProgramStageSelectionActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, REQ_EVENT);
        }
    }

    @Override
    public void showCatComboDialog(String eventId, CategoryCombo categoryCombo, List<CategoryOptionCombo> categoryOptionCombos) {
        CategoryComboDialog dialog = new CategoryComboDialog(
                getAbstracContext(),
                categoryCombo,
                123,
                selectedOption ->
                        presenter.changeCatOption(
                                eventId,
                                selectedOption),
                categoryCombo.displayName());

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    public void switchFollowUp(boolean followUp) {
        this.followUp.set(followUp);
    }

    @Override
    public void displayGenerateEvent(String eventUid) {
        if (eventUid != null) {
            presenter.displayGenerateEvent(eventUid);
            dashboardViewModel.updateEventUid(null);
        }
    }

    @Override
    public void restoreAdapter(String programUid, String teiUid, String enrollmentUid) {
        activity.startActivity(TeiDashboardMobileActivity.intent(activity, teiUid, programUid, enrollmentUid));
        activity.finish();
    }

    @Override
    public void seeDetails(Intent intent, Bundle bundle) {
        this.startActivityForResult(intent, REQ_DETAILS, bundle);
    }

    @Override
    public void showQR(Intent intent) {
        startActivity(intent);
    }

    @Override
    public void openEventDetails(Intent intent, Bundle bundle) {
        this.startActivityForResult(intent, REQ_EVENT, bundle);
    }

    @Override
    public void openEventInitial(Intent intent) {
        this.startActivityForResult(intent, REQ_EVENT, null);
    }

    @Override
    public void openEventCapture(Intent intent) {
        this.startActivityForResult(intent, REQ_EVENT, null);
    }

    @Override
    public void showTeiImage(String filePath, String defaultIcon) {
        if (filePath.isEmpty() && defaultIcon.isEmpty()) {
            binding.cardFront.teiImage.setVisibility(View.GONE);
        } else {
            binding.cardFront.teiImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(new File(filePath))
                    .placeholder(
                            ObjectStyleUtils.getIconResource(context, defaultIcon, R.drawable.photo_temp_gray)
                    )
                    .error(
                            ObjectStyleUtils.getIconResource(context, defaultIcon, R.drawable.photo_temp_gray)
                    )
                    .transition(withCrossFade())
                    .transform(new CircleCrop())
                    .into(binding.cardFront.teiImage);
        }
    }

    @Override
    public void showNewEventOptions(View anchor, ProgramStage stage) {
        PopupMenu popupMenu = new PopupMenu(context, anchor);
        popupMenu.inflate(R.menu.dashboard_event_creation);
        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.schedulenew:
                    goToEventInitial(EventCreationType.SCHEDULE, stage);
                    break;
                case R.id.addnew:
                    goToEventInitial(EventCreationType.ADDNEW, stage);
                    break;
                case R.id.referral:
                    goToEventInitial(EventCreationType.REFERAL, stage);
                    break;
            }
            return true;
        });
        popupMenu.show();

    }

    private void goToEventInitial(EventCreationType eventCreationType, ProgramStage programStage) {
        Intent intent = new Intent(activity, EventInitialActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(PROGRAM_UID, dashboardModel.getCurrentProgram().uid());
        bundle.putString(TRACKED_ENTITY_INSTANCE, dashboardModel.getTei().uid());
        bundle.putString(ORG_UNIT, dashboardModel.getCurrentOrgUnit().uid());
        bundle.putString(ENROLLMENT_UID, dashboardModel.getCurrentEnrollment().uid());
        bundle.putString(EVENT_CREATION_TYPE, eventCreationType.name());
        bundle.putBoolean(EVENT_REPEATABLE, programStage.repeatable());
        bundle.putSerializable(EVENT_PERIOD_TYPE, programStage.periodType());
        bundle.putString(Constants.PROGRAM_STAGE_UID, programStage.uid());
        bundle.putInt(EVENT_SCHEDULE_INTERVAL, programStage.standardInterval() != null ? programStage.standardInterval() : 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    private void showHideFilters(boolean showFilters) {
        if (showFilters) {
            binding.teiData.setVisibility(View.GONE);
            binding.filterLayout.setVisibility(View.VISIBLE);
        } else {
            binding.teiData.setVisibility(View.VISIBLE);
            binding.filterLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void showPeriodRequest(FilterManager.PeriodRequest periodRequest) {
        if (periodRequest == FilterManager.PeriodRequest.FROM_TO) {
            DateUtils.getInstance().showFromToSelector(
                    activity,
                    FilterManager.getInstance()::addPeriod);
        } else {
            DateUtils.getInstance().showPeriodDialog(
                    activity,
                    FilterManager.getInstance()::addPeriod,
                    true);
        }
    }

    @Override
    public void openOrgUnitTreeSelector(String programUid) {
        Intent ouTreeIntent = new Intent(context, OUTreeActivity.class);
        if (programUid != null) {
            Bundle bundle = OUTreeActivity.Companion.getBundle(programUid);
            ouTreeIntent.putExtras(bundle);
        }
        this.startActivityForResult(ouTreeIntent, FilterManager.OU_TREE);
    }
}