package org.dhis2.usescases.programEventDetail;

import android.os.Bundle;
import android.os.Handler;
import android.transition.ChangeBounds;
import android.transition.Transition;
import android.transition.TransitionManager;
import android.util.SparseBooleanArray;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.view.ViewCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import org.dhis2.App;
import org.dhis2.Bindings.ExtensionsKt;
import org.dhis2.Bindings.ViewExtensionsKt;
import org.dhis2.R;
import org.dhis2.databinding.ActivityProgramEventDetailBinding;
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity;
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.commons.orgunitselector.OUTreeFragment;
import org.dhis2.commons.orgunitselector.OnOrgUnitSelectionFinished;
import org.dhis2.usescases.programEventDetail.eventList.EventListFragment;
import org.dhis2.usescases.programEventDetail.eventMap.EventMapFragment;
import org.dhis2.utils.Constants;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.EventCreationType;
import org.dhis2.utils.EventMode;
import org.dhis2.utils.HelpManager;
import org.dhis2.utils.analytics.AnalyticsConstants;
import org.dhis2.utils.category.CategoryDialog;
import org.dhis2.commons.filters.FilterItem;
import org.dhis2.commons.filters.FilterManager;
import org.dhis2.commons.filters.FiltersAdapter;
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator;
import org.dhis2.utils.granularsync.SyncStatusDialog;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.program.Program;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import static android.view.View.GONE;
import static org.dhis2.R.layout.activity_program_event_detail;
import static org.dhis2.utils.Constants.ORG_UNIT;
import static org.dhis2.utils.Constants.PROGRAM_UID;

import dhis2.org.analytics.charts.ui.GroupAnalyticsFragment;

public class ProgramEventDetailActivity extends ActivityGlobalAbstract implements ProgramEventDetailContract.View,
        OnOrgUnitSelectionFinished {

    private static final String FRAGMENT_TAG = "SYNC";

    private ActivityProgramEventDetailBinding binding;

    @Inject
    ProgramEventDetailContract.Presenter presenter;

    @Inject
    FiltersAdapter filtersAdapter;

    @Inject
    NavigationPageConfigurator pageConfigurator;

    private boolean backDropActive;
    private String programUid;

    public static final String EXTRA_PROGRAM_UID = "PROGRAM_UID";
    private ProgramEventDetailViewModel programEventsViewModel;
    public ProgramEventDetailComponent component;
    private boolean isMapVisible = false;

    public static Bundle getBundle(String programUid) {
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_PROGRAM_UID, programUid);
        return bundle;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        initExtras();
        initInjection();
        super.onCreate(savedInstanceState);
        initEventFilters();
        initViewModel();

        binding = DataBindingUtil.setContentView(this, activity_program_event_detail);
        binding.setPresenter(presenter);
        binding.setTotalFilters(FilterManager.getInstance().getTotalFilters());
        binding.navigationBar.pageConfiguration(pageConfigurator);
        binding.navigationBar.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_list_view:
                    if (isMapVisible) {
                        showMap(false);
                    }
                    return true;
                case R.id.navigation_map_view:
                    if (!isMapVisible) {
                        showMap(true);
                    }
                    return true;
                case R.id.navigation_analytics:
                    showAnalytics();
                default:
                    return false;
            }
        });
        ViewExtensionsKt.clipWithRoundedCorners(binding.eventsLayout, ExtensionsKt.getDp(16));
        binding.filterLayout.setAdapter(filtersAdapter);
        presenter.init();
        showMap(false);
    }

    private void initExtras() {
        this.programUid = getIntent().getStringExtra(EXTRA_PROGRAM_UID);
    }

    private void initInjection() {
        component = ((App) getApplicationContext()).userComponent().plus(new ProgramEventDetailModule(this, programUid));
        component.inject(this);
    }

    private void initEventFilters() {
        FilterManager.getInstance().clearCatOptCombo();
        FilterManager.getInstance().clearEventStatus();
    }

    private void initViewModel() {
        programEventsViewModel = ViewModelProviders.of(this).get(ProgramEventDetailViewModel.class);
        programEventsViewModel.progress().observe(this, showProgress -> {
            if (showProgress) {
                binding.toolbarProgress.show();
            } else {
                binding.toolbarProgress.hide();
            }
        });

        programEventsViewModel.getEventSyncClicked().observe(this, eventUid -> {
            if (eventUid != null) {
                presenter.onSyncIconClick(eventUid);
            }
        });

        programEventsViewModel.getEventClicked().observe(this, eventData -> {
            if (eventData != null) {
                navigateToEvent(eventData.component1(), eventData.component2());
            }
        });

        programEventsViewModel.getWritePermission().observe(this, canWrite ->
                binding.addEventButton.setVisibility(canWrite ? View.VISIBLE : GONE));

    }

    @Override
    protected void onResume() {
        super.onResume();
        binding.addEventButton.setEnabled(true);
        binding.setTotalFilters(FilterManager.getInstance().getTotalFilters());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.setOpeningFilterToNone();
        presenter.onDettach();
        FilterManager.getInstance().clearEventStatus();
        FilterManager.getInstance().clearCatOptCombo();
        FilterManager.getInstance().clearWorkingList(false);
        FilterManager.getInstance().clearAssignToMe();
        presenter.clearOtherFiltersIfWebAppIsConfig();
    }

    @Override
    public void setProgram(Program program) {
        binding.setName(program.displayName());
    }

    @Override
    public void showFilterProgress() {
        programEventsViewModel.setProgress(true);
    }

    @Override
    public void renderError(String message) {
        if (getActivity() != null)
            new AlertDialog.Builder(getActivity())
                    .setPositiveButton(android.R.string.ok, null)
                    .setTitle(getString(R.string.error))
                    .setMessage(message)
                    .show();
    }

    @Override
    public void showHideFilter() {
        Transition transition = new ChangeBounds();
        transition.setDuration(200);
        TransitionManager.beginDelayedTransition(binding.backdropLayout, transition);
        backDropActive = !backDropActive;
        ConstraintSet initSet = new ConstraintSet();
        initSet.clone(binding.backdropLayout);
        binding.filterOpen.setVisibility(backDropActive ? View.VISIBLE : View.GONE);
        ViewCompat.setElevation(binding.eventsLayout, backDropActive ? 20 : 0);

        if (backDropActive) {
            initSet.connect(R.id.eventsLayout, ConstraintSet.TOP, R.id.filterLayout, ConstraintSet.BOTTOM, 50);
            binding.navigationBar.hide();
        } else {
            initSet.connect(R.id.eventsLayout, ConstraintSet.TOP, R.id.backdropGuideTop, ConstraintSet.BOTTOM, 0);
            binding.navigationBar.show();
        }

        initSet.applyTo(binding.backdropLayout);
    }

    @Override
    public void startNewEvent() {
        analyticsHelper().setEvent(AnalyticsConstants.CREATE_EVENT, AnalyticsConstants.DATA_CREATION, AnalyticsConstants.CREATE_EVENT);
        binding.addEventButton.setEnabled(false);
        Bundle bundle = EventInitialActivity.getBundle(programUid, null, EventCreationType.ADDNEW.name(),
                null, null, null, presenter.getStageUid(), null,
                0, null);
        startActivity(EventInitialActivity.class, bundle, false, false, null);
    }

    @Override
    public void setWritePermission(Boolean canWrite) {
        programEventsViewModel.getWritePermission().setValue(canWrite);
    }

    @Override
    public void setTutorial() {
        new Handler().postDelayed(() -> {
            SparseBooleanArray stepConditions = new SparseBooleanArray();
            stepConditions.put(2, findViewById(R.id.addEventButton).getVisibility() == View.VISIBLE);
            HelpManager.getInstance().show(getActivity(), HelpManager.TutorialName.PROGRAM_EVENT_LIST,
                    stepConditions);

        }, 500);
    }

    @Override
    public void updateFilters(int totalFilters) {
        binding.setTotalFilters(totalFilters);
        binding.executePendingBindings();
    }

    @Override
    public void showPeriodRequest(FilterManager.PeriodRequest periodRequest) {
        if (periodRequest == FilterManager.PeriodRequest.FROM_TO) {
            DateUtils.getInstance().fromCalendarSelector(this, FilterManager.getInstance()::addPeriod);
        } else {
            DateUtils.getInstance().showPeriodDialog(this, datePeriods -> {
                        FilterManager.getInstance().addPeriod(datePeriods);
                    },
                    true);
        }
    }

    @Override
    public void openOrgUnitTreeSelector() {
        OUTreeFragment ouTreeFragment = OUTreeFragment.Companion.newInstance(true, FilterManager.getInstance().getOrgUnitUidsFilters());
        ouTreeFragment.setSelectionCallback(this);
        ouTreeFragment.show(getSupportFragmentManager(), "OUTreeFragment");
    }

    @Override
    public void onSelectionFinished(List<? extends OrganisationUnit> selectedOrgUnits) {
        presenter.setOrgUnitFilters((List<OrganisationUnit>) selectedOrgUnits);
    }

    @Override
    public void showTutorial(boolean shaked) {
        setTutorial();
    }

    @Override
    public void navigateToEvent(String eventId, String orgUnit) {
        programEventsViewModel.setUpdateEvent(eventId);
        Bundle bundle = new Bundle();
        bundle.putString(PROGRAM_UID, programUid);
        bundle.putString(Constants.EVENT_UID, eventId);
        bundle.putString(ORG_UNIT, orgUnit);
        startActivity(EventCaptureActivity.class,
                EventCaptureActivity.getActivityBundle(eventId, programUid, EventMode.CHECK),
                false, false, null
        );
    }

    @Override
    public void showSyncDialog(String uid) {
        SyncStatusDialog dialog = new SyncStatusDialog.Builder()
                .setConflictType(SyncStatusDialog.ConflictType.EVENT)
                .setUid(uid)
                .onDismissListener(hasChanged -> {
                    if (hasChanged)
                        FilterManager.getInstance().publishData();

                })
                .build();

        dialog.show(getSupportFragmentManager(), FRAGMENT_TAG);
    }

    private void showMap(boolean showMap) {
        isMapVisible = showMap;
        getSupportFragmentManager().beginTransaction().replace(
                R.id.fragmentContainer,
                showMap ? new EventMapFragment() : new EventListFragment()
        ).commitNow();
        binding.addEventButton.setVisibility(showMap && programEventsViewModel.getWritePermission().getValue() ? GONE : View.VISIBLE);
        binding.filter.setVisibility(View.VISIBLE);
    }

    private void showAnalytics() {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, GroupAnalyticsFragment.Companion.forProgram(programUid)).commitNow();
        binding.addEventButton.setVisibility(GONE);
        binding.filter.setVisibility(GONE);
    }

    @Override
    public void showCatOptComboDialog(String catComboUid) {
        new CategoryDialog(
                CategoryDialog.Type.CATEGORY_OPTION_COMBO,
                catComboUid,
                false,
                null,
                selectedCatOptionCombo -> {
                    presenter.filterCatOptCombo(selectedCatOptionCombo);
                    return null;
                }
        ).show(
                getSupportFragmentManager(),
                CategoryDialog.Companion.getTAG()
        );
    }

    @Override
    public void setFilterItems(List<FilterItem> programFilters) {
        filtersAdapter.submitList(programFilters);
    }

    @Override
    public void hideFilters() {
        binding.filter.setVisibility(GONE);
    }
}