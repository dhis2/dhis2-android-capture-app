package org.dhis2.usescases.eventsWithoutRegistration.eventTEIDetails;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ObservableBoolean;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.commons.animations.ViewAnimationsKt;
import org.dhis2.commons.data.SearchTeiModel;
import org.dhis2.commons.popupmenu.AppMenuHelper;
import org.dhis2.commons.resources.ColorType;
import org.dhis2.commons.resources.ColorUtils;
import org.dhis2.commons.sync.ConflictType;
import org.dhis2.commons.data.EventViewModel;
import org.dhis2.commons.data.StageSection;
import org.dhis2.commons.dialogs.CustomDialog;
import org.dhis2.commons.dialogs.DialogClickListener;
import org.dhis2.databinding.FragmentEventTeiDetailsBinding;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity;
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.commons.orgunitselector.OUTreeFragment;
//import org.dhis2.commons.orgunitselector.OnOrgUnitSelectionFinished;
import org.dhis2.usescases.programStageSelection.ProgramStageSelectionActivity;
import org.dhis2.usescases.teiDashboard.DashboardProgramModel;
import org.dhis2.usescases.teiDashboard.DashboardViewModel;
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity;
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.DashboardProgramAdapter;
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TEIDataContracts;
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TEIDataModule;
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TEIDataPresenter;
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents.EventAdapter;
import org.dhis2.commons.data.EventViewModelType;
import org.dhis2.usescases.teiDashboard.ui.DetailsButtonKt;
import org.dhis2.commons.Constants;
import org.dhis2.usescases.teiDashboard.ui.FollowupButtonKt;
import org.dhis2.usescases.teiDashboard.ui.LockButtonKt;
import org.dhis2.utils.CustomComparator;
import org.dhis2.utils.DateUtils;
import org.dhis2.commons.data.EventCreationType;
import org.dhis2.commons.resources.ObjectStyleUtils;
import org.dhis2.utils.OrientationUtilsKt;
import org.dhis2.utils.category.CategoryDialog;
import org.dhis2.utils.dialFloatingActionButton.DialItem;
import org.dhis2.commons.filters.FilterItem;
import org.dhis2.commons.filters.FilterManager;
import org.dhis2.commons.filters.FiltersAdapter;
import org.dhis2.utils.granularsync.SyncStatusDialog;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import kotlin.Unit;
import timber.log.Timber;

import static android.app.Activity.RESULT_OK;
import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;
import static org.dhis2.commons.Constants.ENROLLMENT_UID;
import static org.dhis2.commons.Constants.EVENT_CREATION_TYPE;
import static org.dhis2.commons.Constants.EVENT_PERIOD_TYPE;
import static org.dhis2.commons.Constants.EVENT_REPEATABLE;
import static org.dhis2.commons.Constants.EVENT_SCHEDULE_INTERVAL;
import static org.dhis2.commons.Constants.ORG_UNIT;
import static org.dhis2.commons.Constants.PROGRAM_UID;
import static org.dhis2.commons.Constants.TRACKED_ENTITY_INSTANCE;
import static org.dhis2.utils.analytics.AnalyticsConstants.CREATE_EVENT_TEI;
import static org.dhis2.utils.analytics.AnalyticsConstants.TYPE_EVENT_TEI;

public class EventTeiDetailsFragment extends FragmentGlobalAbstract implements TEIDataContracts.View {

    private static final int REQ_DETAILS = 1001;
    private static final int REQ_EVENT = 2001;

    private static final int RC_GENERATE_EVENT = 1501;
    private static final int RC_EVENTS_COMPLETED = 1601;

    private static final int REFERAL_ID = 3;
    private static final int ADD_NEW_ID = 2;
    private static final int SCHEDULE_ID = 1;

    private static final String PREF_COMPLETED_EVENT = "COMPLETED_EVENT";

    private FragmentEventTeiDetailsBinding binding;

//    @Inject
//    TEIDataContracts.Presenter presenter;

    @Inject
    TEIDataPresenter presenter;

    @Inject
    FilterManager filterManager;

    @Inject
    FiltersAdapter filtersAdapter;


    private EventAdapter adapter;
    private CustomDialog dialog;
    private ProgramStage programStageFromEvent;
    private final ObservableBoolean followUp = new ObservableBoolean(false);

    private boolean hasCatComb;
    private final ArrayList<Event> catComboShowed = new ArrayList<>();
    private Context context;
    private DashboardViewModel dashboardViewModel;
    private DashboardProgramModel dashboardModel;
    private EventCaptureActivity activity;
    private PopupMenu popupMenu;
    private Set<String> attributeNames;
    String teiUid;
    String programUid;
    String enrollmentUid;
    String programStageUid;
    public SearchTeiModel teiModel;
    List<ProgramTrackedEntityAttribute> programTrackedEntityAttributes;
    List<TrackedEntityAttributeValue> attributeValues;

    public static EventTeiDetailsFragment newInstance(String programUid, String teiUid, String enrollmentUid, String eventUid, String stageUid, Set<String> attributeNames) {
        EventTeiDetailsFragment fragment = new EventTeiDetailsFragment();
        Bundle args = new Bundle();
        args.putString("PROGRAM_UID", programUid);
        args.putString("TEI_UID", teiUid);
        args.putString("ENROLLMENT_UID", enrollmentUid);
        args.putString(Constants.PROGRAM_STAGE_UID, stageUid);
        ArrayList<String> x = new ArrayList<>(attributeNames);
        args.putStringArrayList("ATTRIBUTE_NAMES", x);

        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(@NotNull Context context) {

        this.attributeNames = new HashSet<>(getArguments().getStringArrayList("ATTRIBUTE_NAMES"));
        this.teiUid = getArguments().getString("TEI_UID");
        this.enrollmentUid = getArguments().getString("ENROLLMENT_UID");
        this.programUid = getArguments().getString("PROGRAM_UID");
        this.programStageUid = getArguments().getString(Constants.PROGRAM_STAGE_UID);

        super.onAttach(context);
        this.context = context;
        activity = (EventCaptureActivity) context;
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

        if (this.teiModel == null) {
            this.teiModel = new SearchTeiModel();
        }

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_event_tei_details, container, false);
        binding.setPresenter(presenter);

        binding.cardFrontLand.setAttributeListOpened(false);

        Handler handler = new Handler();
        int delayMillis = 1000;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ImageView imageView =  activity.findViewById(R.id.showAttributesButton);
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) imageView.getLayoutParams();

                layoutParams.bottomMargin = 90;
                binding.cardFrontLand.showAttributesButton.setLayoutParams(layoutParams);
            }
        };
        handler.postDelayed(runnable, delayMillis);

        binding.cardFrontLand.showAttributesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                binding.cardFrontLand.setAttributeListOpened(!binding.cardFrontLand.getAttributeListOpened());


                binding.cardFrontLand.showAttributesButton.setOnClickListener((event) -> {
                    ImageView imageView =  activity.findViewById(R.id.showAttributesButton);
                    ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) imageView.getLayoutParams();

                    if (binding.cardFrontLand.getAttributeListOpened()) {
                        binding.cardFrontLand.showAttributesButton.setImageResource(R.drawable.ic_arrow_up);
                        binding.cardFrontLand.setAttributeListOpened(false);

                        layoutParams.bottomMargin = 0;
                        binding.cardFrontLand.showAttributesButton.setLayoutParams(layoutParams);

                    } else {
                        binding.cardFrontLand.showAttributesButton.setImageResource(R.drawable.ic_arrow_down);
                        binding.cardFrontLand.setAttributeListOpened(true);
                        binding.cardFrontLand.entityAttribute1.setGravity(Gravity.END);
                        binding.cardFrontLand.entityAttribute2.setGravity(Gravity.END);
                        binding.cardFrontLand.entityAttribute3.setGravity(Gravity.END);
                        binding.cardFrontLand.entityAttribute4.setGravity(Gravity.END);

                        layoutParams.bottomMargin = 90;
                        binding.cardFrontLand.showAttributesButton.setLayoutParams(layoutParams);

                    }

                });
            }
        });


        try {
            binding.filterLayout.setAdapter(filtersAdapter);
        } catch (Exception e) {
            Timber.e(e);
        }

        binding.cardFrontLand.entityAttribute1.setGravity(Gravity.END);
        binding.cardFrontLand.entityAttribute2.setGravity(Gravity.END);
        binding.cardFrontLand.entityAttribute3.setGravity(Gravity.END);
        binding.cardFrontLand.entityAttribute4.setGravity(Gravity.END);

        binding.cardFrontLand.setAttributeListOpened(true);

        return binding.getRoot();

    }

//    private void updateEnrollment(String enrollmentUid) {
//        presenter.getEnrollment(enrollmentUid);
//    }

    private void updateFabItems() {
        List<DialItem> dialItems = new ArrayList<>();
        dialItems.add(
                new DialItem(REFERAL_ID, getString(R.string.referral), R.drawable.ic_arrow_forward)
        );
        dialItems.add(
                new DialItem(ADD_NEW_ID, getString(R.string.add_new), R.drawable.ic_note_add)
        );
        dialItems.add(
                new DialItem(SCHEDULE_ID, getString(R.string.schedule_new), R.drawable.ic_date_range)
        );
        binding.dialFabLayout.addDialItems(dialItems, clickedId -> {
            switch (clickedId) {
                case REFERAL_ID:
                    createEvent(EventCreationType.REFERAL, 0);
                    break;
                case ADD_NEW_ID:
                    createEvent(EventCreationType.ADDNEW, 0);
                    break;
                case SCHEDULE_ID:
                    createEvent(EventCreationType.SCHEDULE, 0);
                    break;
                default:
                    break;
            }
            return Unit.INSTANCE;
        });
    }

    @Override
    public void setEnrollment(Enrollment enrollment) {
        binding.setEnrollment(enrollment);

        dashboardViewModel.updateDashboard(dashboardModel);
        if (adapter != null) {
            adapter.setEnrollment(enrollment);
        }
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
        presenter.setOpeningFilterToNone();
        presenter.onDettach();
        super.onPause();
    }

//  @Override
//    public void setAttributeValues(List<TrackedEntityAttributeValue> attributeValues) {
//    }

    public TrackedEntityAttributeValue getMatchingAttriburteValue(List<TrackedEntityAttributeValue> attributeValues, String attributeUid) {
        for (TrackedEntityAttributeValue attributeValue : attributeValues) {
            if (attributeValue.trackedEntityAttribute().equals(attributeUid)) {
                return attributeValue;
            }
        }
        return null;
    }

    ;


    @Override
    public void setEnrollmentData(Program program, Enrollment enrollment) {

        if (adapter != null) {
            adapter.setEnrollment(enrollment);
        }
        binding.setProgram(program);
        binding.setEnrollment(enrollment);
        if (enrollment != null) {
            followUp.set(enrollment.followUp() != null ? enrollment.followUp() : false);
        }
        binding.setFollowup(followUp);


        if (this.teiModel == null) {
            this.teiModel = new SearchTeiModel();
        }
        this.teiModel.setCurrentEnrollment(enrollment);
    }

    TrackedEntityAttributeValue getAttributeValue(String attributeUid) {
        List<TrackedEntityAttributeValue> filteredValue = this.attributeValues.stream().filter(value -> {
            return value.trackedEntityAttribute().equals(attributeUid);
        }).collect(Collectors.toList());

        return filteredValue.size() > 0 ? filteredValue.get(0) : null;

    }


    public void setAttributesAndValues(List<TrackedEntityAttributeValue> trackedEntityAttributeValues, List<ProgramTrackedEntityAttribute> programTrackedEntityAttributes) {

        LinkedHashMap<String, TrackedEntityAttributeValue> linkedHashMapOfAttrValues = new LinkedHashMap<>();

        int teiAttributesLoopCounter = 0;
        while (teiAttributesLoopCounter < programTrackedEntityAttributes.size()) {

            TrackedEntityAttributeValue value = getAttributeValue(programTrackedEntityAttributes.get(teiAttributesLoopCounter).trackedEntityAttribute().uid());

            linkedHashMapOfAttrValues.put(programTrackedEntityAttributes.get(teiAttributesLoopCounter).displayShortName().replace("Mother program ", "").replace("Newborn program ", ""), value);
            teiAttributesLoopCounter++;
        }

        this.teiModel.setAttributeValues(linkedHashMapOfAttrValues);

        if (OrientationUtilsKt.isLandscape()) {
            binding.cardFrontLand.setAttributeListOpened(true);
            binding.cardFrontLand.setAttributeNames(this.teiModel.getAttributeValues().keySet());
            binding.cardFrontLand.setAttribute(this.teiModel.getAttributeValues().values().stream().collect(Collectors.toList()));
        }

        if (OrientationUtilsKt.isPortrait()) {

            DetailsButtonKt.setButtonContent(
                    binding.cardFrontLand.detailsButton,
                    "Person",
                    () -> {
                        presenter.seeDetails(binding.cardFrontLand.detailsButton, dashboardModel);
                        return Unit.INSTANCE;
                    }
            );

            FollowupButtonKt.setFollowupButtonContent(binding.cardFrontLand.followupButton, "Person",followUp.get() ,() -> {
                presenter.onFollowUp(dashboardModel);
                return Unit.INSTANCE;
            });

            LockButtonKt.setLockButtonContent(binding.cardFrontLand.lockButton, "Person", () -> {
//                presenter.onFollowUp(dashboardModel);
                showEnrollmentStatusOptions();
                return Unit.INSTANCE;
            });

        } else {
            DetailsButtonKt.setButtonContent(
                    binding.cardFrontLand.detailsButton,
                    "Person",
                    () -> {
//                        presenter.seeDetails(binding.cardFrontLand.detailsButton, dashboardModel);
                        return Unit.INSTANCE;
                    }
            );

            FollowupButtonKt.setFollowupButtonContent(binding.cardFrontLand.followupButton, "Person",followUp.get(), () -> {
                System.out.println("***********************");
                System.out.println("**********WE GOT IN*************");
                System.out.println("***********************");
                presenter.onFollowUp(dashboardModel);
                presenter.init();
                return Unit.INSTANCE;
            });

            LockButtonKt.setLockButtonContent(binding.cardFrontLand.lockButton, "Person", () -> {
//                presenter.onFollowUp(dashboardModel);
                showEnrollmentStatusOptions();
                return Unit.INSTANCE;
            });

        }

    }

    public void showEnrollmentStatusOptions() {

        int menu;

        if (teiModel.getSelectedEnrollment().status() == EnrollmentStatus.ACTIVE) {
            menu = R.menu.tei_detail_options_active;
        } else if (teiModel.getSelectedEnrollment().status() == EnrollmentStatus.COMPLETED) {
            menu = R.menu.tei_detail_options_completed;
        } else {
            menu = R.menu.tei_detail_options_cancelled;
        }

        new AppMenuHelper.Builder()
                .anchor(binding.teiData)
                .menu(activity, menu)
                .onMenuInflated(popupMenu -> {
                            return Unit.INSTANCE;
                        }
                )
                .onMenuItemClicked(itemId -> {
                    switch (itemId) {
                        case R.id.complete:
//                            activity.getPresenter().updateEnrollmentStatus(activity.getEnrollmentUid(), EnrollmentStatus.COMPLETED);
                            break;
                        case R.id.deactivate:
//                            activity.getPresenter().updateEnrollmentStatus(activity.getEnrollmentUid(), EnrollmentStatus.CANCELLED);
                            break;
                        case R.id.reOpen:
//                            activity.getPresenter().updateEnrollmentStatus(activity.getEnrollmentUid(), EnrollmentStatus.ACTIVE);
                            break;
                    }
                    return true;
                })
                .build().show();

    }


//    @Override
//    public void setTrackedEntityInstance(TrackedEntityInstance trackedEntityInstance, OrganisationUnit organisationUnit, List<TrackedEntityAttributeValue> trackedEntityAttributeValues) {
//        if (OrientationUtilsKt.isLandscape()) {
//            binding.cardFrontLand.setOrgUnit(organisationUnit.name());
//            this.attributeValues = trackedEntityAttributeValues;
//
//            if (this.programTrackedEntityAttributes != null) {
//
//                setAttributesAndValues(this.attributeValues, this.programTrackedEntityAttributes);
//
//            }
//        }
//
//        binding.setTrackEntity(trackedEntityInstance);
//
//        if (this.teiModel == null) {
//            this.teiModel = new SearchTeiModel();
//        }
//
//        this.teiModel.setTei(trackedEntityInstance);
//        this.teiModel.setEnrolledOrgUnit(organisationUnit.displayName());
//
//    }


    public void setData(DashboardProgramModel nprogram) {
        this.dashboardModel = nprogram;

        if (nprogram != null && nprogram.getCurrentEnrollment() != null) {
            binding.dialFabLayout.setFabVisible(true);
            presenter.setDashboardProgram(this.dashboardModel);
            SharedPreferences prefs = context.getSharedPreferences(Constants.SHARE_PREFS, Context.MODE_PRIVATE);
            hasCatComb = nprogram.getCurrentProgram() != null && !nprogram.getCurrentProgram().categoryComboUid().equals(prefs.getString(Constants.DEFAULT_CAT_COMBO, ""));
            binding.setDashboardModel(nprogram);
            updateFabItems();
        } else if (nprogram != null) {
            binding.dialFabLayout.setFabVisible(false);
            binding.teiRecycler.setAdapter(new DashboardProgramAdapter(presenter, nprogram));
            binding.teiRecycler.addItemDecoration(new DividerItemDecoration(getAbstracContext(), DividerItemDecoration.VERTICAL));
            binding.setDashboardModel(nprogram);
            showLoadingProgress(false);
        }

        if (OrientationUtilsKt.isPortrait()) {

            DetailsButtonKt.setButtonContent(
                    binding.cardFrontLand.detailsButton,
                    "Person",
                    () -> {
                        presenter.seeDetails(binding.cardFrontLand.detailsButton, dashboardModel);
                        return Unit.INSTANCE;
                    }
            );

            FollowupButtonKt.setFollowupButtonContent(binding.cardFrontLand.followupButton, "Person",followUp.get(), () -> {
                presenter.onFollowUp(dashboardModel);
                return Unit.INSTANCE;
            });

            LockButtonKt.setLockButtonContent(binding.cardFrontLand.lockButton, "Person", () -> {
                presenter.onFollowUp(dashboardModel);
                return Unit.INSTANCE;
            });

        } else {

            DetailsButtonKt.setButtonContent(
                    binding.cardFrontLand.detailsButton,
                    "Person",
                    () -> {
                        presenter.seeDetails(binding.cardFrontLand.detailsButton, dashboardModel);
                        return Unit.INSTANCE;
                    }
            );

            FollowupButtonKt.setFollowupButtonContent(binding.cardFrontLand.followupButton, "Person",followUp.get(), () -> {
                presenter.onFollowUp(dashboardModel);
                presenter.init();
                return Unit.INSTANCE;
            });

            LockButtonKt.setLockButtonContent(binding.cardFrontLand.lockButton, "Person", () -> {
                presenter.onFollowUp(dashboardModel);
                return Unit.INSTANCE;
            });

        }

        binding.executePendingBindings();

        if (getSharedPreferences().getString(PREF_COMPLETED_EVENT, null) != null) {
            presenter.displayGenerateEvent(getSharedPreferences().getString(PREF_COMPLETED_EVENT, null));
            getSharedPreferences().edit().remove(PREF_COMPLETED_EVENT).apply();
        }

    }

    @SuppressLint("CheckResult")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQ_DETAILS) {
                activity.getPresenter().init();
            }
        }
    }

//    @Override
//    public void setFilters(List<FilterItem> filterItems) {
//        filtersAdapter.submitList(filterItems);
//    }

    @Override
    public void hideFilters() {
//        activity.hideFilter();
    }

    @Override
    public Flowable<StageSection> observeStageSelection(Program currentProgram, Enrollment currentEnrollment) {

        if (adapter == null) {
            adapter = new EventAdapter(presenter, currentProgram, new ColorUtils());
            adapter.setEnrollment(currentEnrollment);

            binding.teiRecycler.setAdapter(adapter);

        }

        return adapter.stageSelector();
    }

    @Override
    public void setEvents(List<EventViewModel> events, boolean canAddEvents) {

        binding.setCanAddEvents(canAddEvents);

        if (OrientationUtilsKt.isLandscape()) {
            activity.findViewById(R.id.dialFabLayout).setVisibility(View.GONE);
        }

        if (events.isEmpty()) {
            binding.emptyTeis.setVisibility(View.VISIBLE);
            if (binding.dialFabLayout.isFabVisible()) {
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
        showLoadingProgress(false);
    }

    private void showLoadingProgress(boolean showProgress) {
        if (showProgress) {
            binding.loadingProgress.getRoot().setVisibility(View.VISIBLE);
        } else {
            binding.loadingProgress.getRoot().setVisibility(View.GONE);
        }
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
            if (enrollmentStatus == EnrollmentStatus.COMPLETED) {
            }
        };
    }

    private void createEvent(EventCreationType eventCreationType, Integer scheduleIntervalDays) {
        if (isAdded()) {
            analyticsHelper().setEvent(TYPE_EVENT_TEI, eventCreationType.name(), CREATE_EVENT_TEI);
            Bundle bundle = new Bundle();
            bundle.putString(PROGRAM_UID, dashboardModel.getCurrentEnrollment().program());
            bundle.putString(TRACKED_ENTITY_INSTANCE, dashboardModel.getTei().uid());
            if (presenter.enrollmentOrgUnitInCaptureScope(dashboardModel.getCurrentOrgUnit().uid())) {
                bundle.putString(ORG_UNIT, dashboardModel.getCurrentOrgUnit().uid());
            }
            bundle.putString(ENROLLMENT_UID, dashboardModel.getCurrentEnrollment().uid());
            bundle.putString(EVENT_CREATION_TYPE, eventCreationType.name());
            bundle.putInt(EVENT_SCHEDULE_INTERVAL, scheduleIntervalDays);
            Intent intent = new Intent(getContext(), ProgramStageSelectionActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, REQ_EVENT);
        }
    }


    public void showCatComboDialog(String eventId, Date eventDate, String categoryComboUid) {
        CategoryDialog categoryDialog = new CategoryDialog(
                CategoryDialog.Type.CATEGORY_OPTION_COMBO,
                categoryComboUid,
                true,
                eventDate,
                selectedCatOptComboUid -> {
                    presenter.changeCatOption(eventId, selectedCatOptComboUid);
                    return null;
                }
        );
        categoryDialog.setCancelable(false);
        categoryDialog.show(getChildFragmentManager(), CategoryDialog.Companion.getTAG());
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

    public void seeDetails(Intent intent, Bundle bundle) {
        this.startActivityForResult(intent, REQ_DETAILS, bundle);
    }

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
//            binding.cardFront.teiImage.setVisibility(View.GONE);
        } else {
//            binding.cardFront.teiImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(new File(filePath))
                    .error(
                            ObjectStyleUtils.getIconResource(context, defaultIcon, R.drawable.photo_temp_gray, new ColorUtils())
                    )
                    .transition(withCrossFade())
                    .transform(new CircleCrop());
        }
    }

//    @Override
    public void showNewEventOptions(View anchor, ProgramStage stage) {
        popupMenu = new PopupMenu(context, anchor);
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

    public void hideDueDate() {
        popupMenu.getMenu().findItem(R.id.schedulenew).setVisible(false);
    }

    public void goToEventInitial(EventCreationType eventCreationType, ProgramStage programStage) {

        Intent intent = new Intent(activity, EventInitialActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString(PROGRAM_UID, this.programUid);
        bundle.putString(TRACKED_ENTITY_INSTANCE, this.teiUid);
        // TODO: remove hardcoded ous
        if (presenter.enrollmentOrgUnitInCaptureScope("V5XvX1wr1kF")) {
            bundle.putString(ORG_UNIT, "V5XvX1wr1kF");
        }
        if (attributeNames != null) {
            ArrayList<String> x = new ArrayList<>(attributeNames);

        }
        bundle.putString(ENROLLMENT_UID, this.enrollmentUid);
        bundle.putString(EVENT_CREATION_TYPE, eventCreationType.name());
        bundle.putBoolean(EVENT_REPEATABLE, programStage.repeatable());
        bundle.putSerializable(EVENT_PERIOD_TYPE, programStage.periodType());
        bundle.putString(Constants.PROGRAM_STAGE_UID, programStage.uid());
        bundle.putInt(EVENT_SCHEDULE_INTERVAL, programStage.standardInterval() != null ? programStage.standardInterval() : 0);
        intent.putExtras(bundle);
        startActivityForResult(intent, REQ_EVENT);
    }

    private void showHideFilters(boolean showFilters) {
        if (showFilters) {
            ViewAnimationsKt.expand(binding.filterLayout, false, () -> {
                binding.teiData.setVisibility(View.GONE);
                binding.filterLayout.setVisibility(View.VISIBLE);
                return Unit.INSTANCE;
            });

        } else {
            ViewAnimationsKt.collapse(binding.filterLayout, () -> {
                binding.teiData.setVisibility(View.VISIBLE);
                binding.filterLayout.setVisibility(View.GONE);
                return Unit.INSTANCE;
            });


        }
    }

    @Override
    public void showPeriodRequest(FilterManager.PeriodRequest periodRequest) {
        if (periodRequest == FilterManager.PeriodRequest.FROM_TO) {
            DateUtils.getInstance().fromCalendarSelector(
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
        OUTreeFragment ouTreeFragment = OUTreeFragment.Companion.newInstance(true, FilterManager.getInstance().getOrgUnitUidsFilters());
//        ouTreeFragment.setSelectionCallback(this);
        ouTreeFragment.show(getChildFragmentManager(), "OUTreeFragment");
    }

    public void showSyncDialog(String uid) {
        SyncStatusDialog dialog = new SyncStatusDialog.Builder()
                .setConflictType(ConflictType.TEI)
                .setUid(uid)
                .onDismissListener(hasChanged -> {
                    if (hasChanged)
                        FilterManager.getInstance().publishData();

                })
                .build();

        dialog.show(getChildFragmentManager(), uid);
    }

    @Override
    public void setRiskColor(String risk) {

        if (risk == "High Risk") {

            binding.setHighRisk(true);
            binding.setLowRisk(false);
        }

        if (risk == "Low Risk") {

            binding.setLowRisk(true);
            binding.setHighRisk(false);
        }

    }

    @Override
    public void setProgramAttributes(List<? extends ProgramTrackedEntityAttribute> programTrackedEntityAttributes) {

        this.programTrackedEntityAttributes = programTrackedEntityAttributes.stream()
                .filter(attr -> attr.displayInList())
                .collect(Collectors.toList());

        Collections.sort(this.programTrackedEntityAttributes, new CustomComparator());

        if (OrientationUtilsKt.isLandscape()) {
            if (this.attributeValues != null) {

                setAttributesAndValues(this.attributeValues, this.programTrackedEntityAttributes);

            }
        }
    }

    public void onSelectionFinished(@NotNull List<? extends OrganisationUnit> selectedOrgUnits) {
        presenter.setOrgUnitFilters((List<OrganisationUnit>) selectedOrgUnits);
    }


    @Override
    public void setAttributeValues(@Nullable List<? extends TrackedEntityAttributeValue> attributeValues) {

    }

    @Override
    public void seeDetails(@NonNull Intent intent, @NonNull ActivityOptionsCompat options) {

    }

    @Override
    public void openEventDetails(@NonNull Intent intent, @NonNull ActivityOptionsCompat options) {

    }


//    @Override
//    public void setProgramAttributes(@Nullable List<? extends ProgramTrackedEntityAttribute> programTrackedEntityAttributes) {
//
//    }


    @Override
    public void showSyncDialog(@NonNull String eventUid, @NonNull String enrollmentUid) {

    }

    @Override
    public void displayCatComboOptionSelectorForEvents(@NonNull List<EventViewModel> data) {

    }

    @Override
    public void showProgramRuleErrorMessage(@NonNull String message) {

    }

    @Override
    public void showCatOptComboDialog(@NonNull String catComboUid) {

    }

    @Override
    public void setFilters(@NonNull List<? extends FilterItem> filterItems) {

    }

    @Override
    public void setTrackedEntityInstance(@Nullable TrackedEntityInstance trackedEntityInstance, @Nullable OrganisationUnit organisationUnit, @Nullable List<? extends TrackedEntityAttributeValue> trackedEntityAttributeValues) {
        if (OrientationUtilsKt.isLandscape()) {
            binding.cardFrontLand.setOrgUnit(organisationUnit.name());
            this.attributeValues = (List<TrackedEntityAttributeValue>) trackedEntityAttributeValues;

            if (this.programTrackedEntityAttributes != null) {

                setAttributesAndValues(this.attributeValues, this.programTrackedEntityAttributes);

            }
        }

        binding.setTrackEntity(trackedEntityInstance);

        if (this.teiModel == null) {
            this.teiModel = new SearchTeiModel();
        }

        this.teiModel.setTei(trackedEntityInstance);
        this.teiModel.setEnrolledOrgUnit(organisationUnit.displayName());
    }
}