package org.dhis2.usescases.teiDashboard;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager2.widget.ViewPager2;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.databinding.ActivityDashboardMobileBinding;
import org.dhis2.usescases.enrollment.EnrollmentActivity;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.teiDashboard.adapters.DashboardPagerAdapter;
import org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.MapButtonObservable;
import org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.TEIDataFragment;
import org.dhis2.usescases.teiDashboard.teiProgramList.TeiProgramListActivity;
import org.dhis2.commons.resources.ColorUtils;
import org.dhis2.utils.Constants;
import org.dhis2.utils.HelpManager;
import org.dhis2.utils.OrientationUtilsKt;
import org.dhis2.utils.customviews.navigationbar.NavigationPageConfigurator;
import org.dhis2.commons.filters.FilterManager;
import org.dhis2.commons.filters.Filters;
import org.dhis2.utils.granularsync.GranularSyncContracts;
import org.dhis2.utils.granularsync.SyncStatusDialog;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.inject.Inject;

import timber.log.Timber;

import static org.dhis2.usescases.teiDashboard.DataConstantsKt.CHANGE_PROGRAM;
import static org.dhis2.usescases.teiDashboard.DataConstantsKt.CHANGE_PROGRAM_ENROLLMENT;
import static org.dhis2.usescases.teiDashboard.DataConstantsKt.GO_TO_ENROLLMENT;
import static org.dhis2.usescases.teiDashboard.DataConstantsKt.GO_TO_ENROLLMENT_PROGRAM;
import static org.dhis2.utils.Constants.ENROLLMENT_UID;
import static org.dhis2.utils.Constants.PROGRAM_UID;
import static org.dhis2.utils.Constants.TEI_UID;
import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.SHOW_HELP;

public class TeiDashboardMobileActivity extends ActivityGlobalAbstract implements TeiDashboardContracts.View, MapButtonObservable {

    public static final int OVERVIEW_POS = 0;

    private int currentOrientation = -1;

    @Inject
    public TeiDashboardContracts.Presenter presenter;

    @Inject
    public FilterManager filterManager;

    @Inject
    public NavigationPageConfigurator pageConfigurator;

    protected DashboardProgramModel programModel;

    protected String teiUid;
    protected String programUid;
    protected String enrollmentUid;

    ActivityDashboardMobileBinding binding;
    protected DashboardPagerAdapter adapter;

    private DashboardViewModel dashboardViewModel;
    private boolean fromRelationship;

    private MutableLiveData<Boolean> groupByStage;
    private MutableLiveData<Boolean> filtersShowing;
    private MutableLiveData<String> currentEnrollment;
    private MutableLiveData<Boolean> relationshipMap;
    private float elevation = 0f;
    private static final String TEI_SYNC = "SYNC_TEI";
    private boolean restartingActivity = false;

    public static Intent intent(Context context,
                                String teiUid,
                                String programUid,
                                String enrollmentUid) {
        Intent intent = new Intent(context, TeiDashboardMobileActivity.class);

        intent.putExtra(TEI_UID, teiUid);
        intent.putExtra(PROGRAM_UID, programUid);
        intent.putExtra(ENROLLMENT_UID, enrollmentUid);

        return intent;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        if (savedInstanceState != null && savedInstanceState.containsKey(Constants.TRACKED_ENTITY_INSTANCE)) {
            teiUid = savedInstanceState.getString(Constants.TRACKED_ENTITY_INSTANCE);
            programUid = savedInstanceState.getString(PROGRAM_UID);
        } else {
            teiUid = getIntent().getStringExtra(TEI_UID);
            programUid = getIntent().getStringExtra(PROGRAM_UID);
            enrollmentUid = getIntent().getStringExtra(ENROLLMENT_UID);
        }

        ((App) getApplicationContext()).createDashboardComponent(new TeiDashboardModule(this, teiUid, programUid, enrollmentUid, OrientationUtilsKt.isPortrait())).inject(this);
        setTheme(presenter.getProgramTheme(R.style.AppTheme));
        super.onCreate(savedInstanceState);
        groupByStage = new MutableLiveData<>(presenter.getProgramGrouping());
        filtersShowing = new MutableLiveData<>(false);
        currentEnrollment = new MutableLiveData<>();
        relationshipMap = new MutableLiveData<>(false);
        dashboardViewModel = ViewModelProviders.of(this).get(DashboardViewModel.class);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_dashboard_mobile);
        showLoadingProgress(true);
        binding.setPresenter(presenter);

        filterManager.setUnsupportedFilters(Filters.ENROLLMENT_DATE, Filters.ENROLLMENT_STATUS);
        binding.setTotalFilters(filterManager.getTotalFilters());
        binding.navigationBar.setVisibility(programUid != null ? View.VISIBLE : View.GONE);
        binding.navigationBar.pageConfiguration(pageConfigurator);
        binding.navigationBar.setOnNavigationItemSelectedListener(item -> {
            if (adapter == null) return true;
            int pagePosition = adapter.getNavigationPagePosition(item.getItemId());
            if (pagePosition != -1) {
                if (OrientationUtilsKt.isLandscape()) {
                    binding.teiTablePager.setCurrentItem(pagePosition);
                } else {
                    binding.syncButton.setVisibility(pagePosition == 0 ? View.VISIBLE : View.GONE);
                    binding.teiPager.setCurrentItem(pagePosition);
                }
            }
            return true;
        });

        binding.toolbarTitle.setLines(1);
        binding.toolbarTitle.setEllipsize(TextUtils.TruncateAt.END);

        presenter.prefSaveCurrentProgram(programUid);

        filtersShowing.observe(this, showFilter -> {
            if (OrientationUtilsKt.isPortrait()) {
                presenter.handleShowHideFilters(showFilter);
            }
        });

        elevation = ViewCompat.getElevation(binding.toolbar);

        binding.relationshipMapIcon.setOnClickListener(v -> {
                    if (!relationshipMap.getValue()) {
                        binding.relationshipMapIcon.setImageResource(R.drawable.ic_list);
                    } else {
                        binding.relationshipMapIcon.setImageResource(R.drawable.ic_map);
                    }
                    boolean showMap = !relationshipMap.getValue();
                    if (showMap) {
                        binding.toolbarProgress.setVisibility(View.VISIBLE);
                        binding.toolbarProgress.hide();
                    }
                    relationshipMap.setValue(showMap);
                }
        );

        binding.syncButton.setOnClickListener(v -> {
            openSyncDialog();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (currentOrientation != -1) {
            int nextOrientation = OrientationUtilsKt.isLandscape() ? 1 : 0;
            if (currentOrientation != nextOrientation && adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }
        currentOrientation = OrientationUtilsKt.isLandscape() ? 1 : 0;

        if (adapter == null) {
            restoreAdapter(programUid);
        }

        presenter.refreshTabCounters();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPause() {
        presenter.onDettach();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        ((App) getApplicationContext()).releaseDashboardComponent();
        super.onDestroy();
    }

    private void openSyncDialog() {
        SyncStatusDialog syncDialog = new SyncStatusDialog.Builder()
                .setConflictType(SyncStatusDialog.ConflictType.TEI)
                .setUid(enrollmentUid)
                .onDismissListener(hasChanged -> {
                    if(hasChanged && !restartingActivity) {
                        restartingActivity = true;
                        startActivity(intent(getContext(), teiUid, programUid, enrollmentUid));
                        finish();
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    }
                })
                .build();
        syncDialog.show(getSupportFragmentManager(), TEI_SYNC);
    }

    private void setViewpagerAdapter() {
        adapter = new DashboardPagerAdapter(this,
                programUid,
                teiUid,
                enrollmentUid,
                pageConfigurator.displayAnalytics(),
                pageConfigurator.displayRelationships()
        );

        if (OrientationUtilsKt.isPortrait()) {
            binding.teiPager.setAdapter(null);
            binding.teiPager.setUserInputEnabled(false);
            binding.teiPager.setAdapter(adapter);
            binding.teiPager.registerOnPageChangeCallback(
                    new ViewPager2.OnPageChangeCallback() {
                        @Override
                        public void onPageSelected(int position) {
                            showLoadingProgress(false);
                            if (position != OVERVIEW_POS || programUid == null) {
                                binding.filterCounter.setVisibility(View.GONE);
                                binding.searchFilterGeneral.setVisibility(View.GONE);
                            } else {
                                binding.filterCounter.setVisibility(View.VISIBLE);
                                binding.searchFilterGeneral.setVisibility(View.VISIBLE);
                            }
                            if (adapter.pageType(position) == DashboardPagerAdapter.DashboardPageType.RELATIONSHIPS) {
                                binding.relationshipMapIcon.setVisibility(View.VISIBLE);
                            } else {
                                binding.relationshipMapIcon.setVisibility(View.GONE);
                            }
                            binding.navigationBar.selectItemAt(position);
                        }
                    }
            );

            if (fromRelationship)
                binding.teiPager.setCurrentItem(2, false);

        } else {
            binding.teiTablePager.setAdapter(adapter);
            binding.teiTablePager.setUserInputEnabled(false);
            binding.teiTablePager.registerOnPageChangeCallback(
                    new ViewPager2.OnPageChangeCallback() {
                        @Override
                        public void onPageSelected(int position) {
                            showLoadingProgress(false);
                            switch (adapter.pageType(position)) {
                                case ANALYTICS:
                                case NOTES:
                                    binding.relationshipMapIcon.setVisibility(View.GONE);
                                    break;
                                case RELATIONSHIPS:
                                    binding.relationshipMapIcon.setVisibility(View.VISIBLE);
                                    break;
                                default:
                                    break;
                            }
                            binding.navigationBar.selectItemAt(position);
                        }
                    }
            );
            if (fromRelationship)
                binding.teiTablePager.setCurrentItem(1, false);

        }
    }

    private void showLoadingProgress(boolean showProgress) {
        if (showProgress) {
            binding.toolbarProgress.show();
        } else {
            binding.toolbarProgress.hide();
        }
    }

    @Override
    public void setData(DashboardProgramModel program) {

        dashboardViewModel.updateDashboard(program);
        ObjectStyle style = program.getObjectStyleForProgram(program.getCurrentProgram().uid());
        setProgramColor(style == null ? "" : style.color());


        binding.setDashboardModel(program);
        binding.setTrackEntity(program.getTei());
        String title = String.format("%s %s - %s",
                program.getTrackedEntityAttributeValueBySortOrder(1) != null ? program.getTrackedEntityAttributeValueBySortOrder(1) : "",
                program.getTrackedEntityAttributeValueBySortOrder(2) != null ? program.getTrackedEntityAttributeValueBySortOrder(2) : "",
                program.getCurrentProgram() != null ? program.getCurrentProgram().displayName() : getString(R.string.dashboard_overview)
        );
        binding.setTitle(title);

        binding.executePendingBindings();
        this.programModel = program;
        this.enrollmentUid = program.getCurrentEnrollment().uid();

        if (OrientationUtilsKt.isLandscape()) {
            if (binding.teiTablePager.getAdapter() == null) {
                setViewpagerAdapter();
            }
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.tei_main_view, TEIDataFragment.newInstance(programUid, teiUid, enrollmentUid))
                    .commitAllowingStateLoss();
        } else {
            if (binding.teiPager.getAdapter() == null) {
                setViewpagerAdapter();
            }
        }

        boolean enrollmentStatus = program.getCurrentEnrollment() != null && program.getCurrentEnrollment().status() == EnrollmentStatus.ACTIVE;
        if (getIntent().getStringExtra(Constants.EVENT_UID) != null && enrollmentStatus)
            dashboardViewModel.updateEventUid(getIntent().getStringExtra(Constants.EVENT_UID));

        presenter.initNoteCounter();
    }

    @Override
    public void restoreAdapter(String programUid) {
        this.adapter = null;
        this.programUid = programUid;
        presenter.init();
    }

    @Override
    public void handleTeiDeletion() {
        finish();
    }

    @Override
    public void handleEnrollmentDeletion(Boolean hasMoreEnrollments) {
        if (hasMoreEnrollments) {
            startActivity(intent(this, teiUid, null, null));
            finish();
        } else
            finish();
    }

    @Override
    public void authorityErrorMessage() {
        displayMessage(getString(R.string.delete_authority_error));
    }

    @Override
    public void setDataWithOutProgram(DashboardProgramModel program) {
        dashboardViewModel.updateDashboard(program);
        setProgramColor("");
        binding.setDashboardModel(program);
        binding.setTrackEntity(program.getTei());
        String title = String.format("%s %s - %s",
                program.getTrackedEntityAttributeValueBySortOrder(1) != null ? program.getTrackedEntityAttributeValueBySortOrder(1) : "",
                program.getTrackedEntityAttributeValueBySortOrder(2) != null ? program.getTrackedEntityAttributeValueBySortOrder(2) : "",
                program.getCurrentProgram() != null ? program.getCurrentProgram().displayName() : getString(R.string.dashboard_overview)
        );
        binding.setTitle(title);
        binding.executePendingBindings();
        this.programModel = program;

        setViewpagerAdapter();
        binding.searchFilterGeneral.setVisibility(View.GONE);
        binding.relationshipMapIcon.setVisibility(View.GONE);

        if (OrientationUtilsKt.isLandscape()) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.tei_main_view, TEIDataFragment.newInstance(programUid, teiUid, enrollmentUid))
                    .commitAllowingStateLoss();

            binding.filterCounter.setVisibility(View.GONE);
            binding.searchFilterGeneral.setVisibility(View.GONE);

        }
    }

    @Override
    public void goToEnrollmentList() {
        Intent intent = new Intent(this, TeiProgramListActivity.class);
        intent.putExtra("TEI_UID", teiUid);
        startActivityForResult(intent, Constants.RQ_ENROLLMENTS);
    }

    public TeiDashboardContracts.Presenter getPresenter() {
        return presenter;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.RQ_ENROLLMENTS && resultCode == RESULT_OK) {
            if (data.hasExtra(GO_TO_ENROLLMENT)) {
                Intent intent = EnrollmentActivity.Companion.getIntent(this,
                        data.getStringExtra(GO_TO_ENROLLMENT),
                        data.getStringExtra(GO_TO_ENROLLMENT_PROGRAM),
                        EnrollmentActivity.EnrollmentMode.NEW,
                        false);
                startActivity(intent);
                finish();
            }

            if (data.hasExtra(CHANGE_PROGRAM)) {
                startActivity(intent(this, teiUid, data.getStringExtra(CHANGE_PROGRAM),
                        data.getStringExtra(CHANGE_PROGRAM_ENROLLMENT)));
                finish();
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void setTutorial() {
        new Handler().postDelayed(() -> {
            if (getAbstractActivity() != null)
                HelpManager.getInstance().show(getActivity(), HelpManager.TutorialName.TEI_DASHBOARD, null);
        }, 500);
    }

    @Override
    public void showTutorial(boolean shaked) {
        if (OrientationUtilsKt.isLandscape()) {
            setTutorial();
        } else {
            if (binding.teiPager.getCurrentItem() == 0)
                setTutorial();
            else
                showToast(getString(R.string.no_intructions));
        }
    }

    public String getTeiUid() {
        return teiUid;
    }

    public String getProgramUid() {
        return programUid;
    }

    public void toRelationships() {
        fromRelationship = true;
    }

    private void setProgramColor(String color) {
        int programTheme = ColorUtils.getThemeFromColor(color);
        int programColor = ColorUtils.getColorFrom(color, ColorUtils.getPrimaryColor(this, ColorUtils.ColorType.PRIMARY));

        if (programTheme != -1) {
            presenter.saveProgramTheme(programTheme);
            binding.toolbar.setBackgroundColor(programColor);
            binding.navigationBar.setIconsColor(programColor);
        } else {
            presenter.removeProgramTheme();
            int colorPrimary;
            switch (presenter.getProgramTheme(R.style.AppTheme)) {
                case R.style.RedTheme:
                    colorPrimary = R.color.colorPrimaryRed;
                    break;
                case R.style.OrangeTheme:
                    colorPrimary = R.color.colorPrimaryOrange;
                    break;
                case R.style.GreenTheme:
                    colorPrimary = R.color.colorPrimaryGreen;
                    break;
                case R.style.AppTheme:
                default:
                    colorPrimary = R.color.colorPrimary;
                    break;
            }
            binding.toolbar.setBackgroundColor(ContextCompat.getColor(this, colorPrimary));
            binding.navigationBar.setIconsColor(ContextCompat.getColor(this, colorPrimary));
        }

        binding.executePendingBindings();
        setTheme(presenter.getProgramTheme(R.style.AppTheme));

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            TypedValue typedValue = new TypedValue();
            TypedArray a = obtainStyledAttributes(typedValue.data, new int[]{R.attr.colorPrimaryDark});
            int colorToReturn = a.getColor(0, 0);
            a.recycle();
            window.setStatusBarColor(colorToReturn);
        }
    }

    public void showMoreOptions(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view, Gravity.BOTTOM);
        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Exception e) {
            Timber.e(e);
        }

        int menu;
        if (enrollmentUid == null) {
            menu = R.menu.dashboard_tei_menu;
        } else if (groupByStage.getValue()) {
            menu = R.menu.dashboard_menu_group;
        } else {
            menu = R.menu.dashboard_menu;
        }
        popupMenu.getMenuInflater().inflate(menu, popupMenu.getMenu());

        if (enrollmentUid != null) {
            EnrollmentStatus status = presenter.getEnrollmentStatus(enrollmentUid);
            if (status == EnrollmentStatus.COMPLETED) {
                popupMenu.getMenu().findItem(R.id.complete).setVisible(false);
            } else if (status == EnrollmentStatus.CANCELLED) {
                popupMenu.getMenu().findItem(R.id.deactivate).setVisible(false);
            } else {
                popupMenu.getMenu().findItem(R.id.activate).setVisible(false);
            }
        }

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.showHelp:
                    analyticsHelper().setEvent(SHOW_HELP, CLICK, SHOW_HELP);
                    showTutorial(true);
                    break;
                case R.id.deleteTei:
                    presenter.deleteTei();
                    break;
                case R.id.deleteEnrollment:
                    presenter.deleteEnrollment();
                    break;
                case R.id.programSelector:
                    presenter.onEnrollmentSelectorClick();
                    break;
                case R.id.groupEvents:
                    groupByStage.setValue(true);
                    break;
                case R.id.showTimeline:
                    groupByStage.setValue(false);
                    break;
                case R.id.complete:
                    presenter.updateEnrollmentStatus(enrollmentUid, EnrollmentStatus.COMPLETED);
                    break;
                case R.id.activate:
                    presenter.updateEnrollmentStatus(enrollmentUid, EnrollmentStatus.ACTIVE);
                    break;
                case R.id.deactivate:
                    presenter.updateEnrollmentStatus(enrollmentUid, EnrollmentStatus.CANCELLED);
                    break;
            }
            return true;

        });
        popupMenu.show();
    }

    @Override
    public void updateNoteBadge(int numberOfNotes) {
        binding.navigationBar.updateBadge(R.id.navigation_notes, numberOfNotes);
    }

    public LiveData<Boolean> observeGrouping() {
        return groupByStage;
    }

    @NotNull
    @Override
    public LiveData<Boolean> relationshipMap() {
        return relationshipMap;
    }

    @Override
    public void setFiltersLayoutState() {
        filtersShowing.setValue(!filtersShowing.getValue());
    }

    public LiveData<Boolean> observeFilters() {
        return filtersShowing;
    }

    @Override
    public void updateTotalFilters(Integer totalFilters) {
        binding.setTotalFilters(totalFilters);
    }

    @Override
    public void hideTabsAndDisableSwipe() {
        ViewCompat.setElevation(binding.toolbar, 0);
    }

    @Override
    public void showTabsAndEnableSwipe() {
        ViewCompat.setElevation(binding.toolbar, elevation);
    }

    @Override
    public void updateStatus() {
        currentEnrollment.setValue(programModel.getCurrentEnrollment().uid());
    }

    public LiveData<String> updatedEnrollment() {
        return currentEnrollment;
    }

    @Override
    public void displayStatusError(StatusChangeResultCode statusCode) {
        switch (statusCode) {
            case FAILED:
                displayMessage(getString(R.string.something_wrong));
                break;
            case ACTIVE_EXIST:
                displayMessage(getString(R.string.status_change_error_active_exist));
                break;
            case WRITE_PERMISSION_FAIL:
                displayMessage(getString(R.string.permission_denied));
                break;
        }

    }

    @Override
    public void onRelationshipMapLoaded() {
        binding.toolbarProgress.hide();
    }

    public void hideFilter() {
        binding.searchFilterGeneral.setVisibility(View.GONE);
    }
}