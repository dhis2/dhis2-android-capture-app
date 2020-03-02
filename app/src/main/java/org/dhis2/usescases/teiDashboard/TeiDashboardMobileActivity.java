package org.dhis2.usescases.teiDashboard;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
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

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.databinding.ActivityDashboardMobileBinding;
import org.dhis2.usescases.enrollment.EnrollmentActivity;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.teiDashboard.adapters.DashboardPagerAdapter;
import org.dhis2.usescases.teiDashboard.adapters.DashboardPagerTabletAdapter;
import org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data.TEIDataFragment;
import org.dhis2.usescases.teiDashboard.teiProgramList.TeiProgramListActivity;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.Constants;
import org.dhis2.utils.HelpManager;
import org.dhis2.utils.OrientationUtilsKt;
import org.dhis2.utils.filters.FilterManager;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.inject.Inject;

import timber.log.Timber;

import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.SHOW_HELP;

public class TeiDashboardMobileActivity extends ActivityGlobalAbstract implements TeiDashboardContracts.View {

    @Inject
    public TeiDashboardContracts.Presenter presenter;

    @Inject
    public FilterManager filterManager;

    protected DashboardProgramModel programModel;

    protected String teiUid;
    protected String programUid;
    protected String enrollmentUid;

    ActivityDashboardMobileBinding binding;
    protected DashboardPagerAdapter adapter;
    protected DashboardPagerTabletAdapter tabletAdapter;
    protected FragmentStateAdapter currentAdapter;

    private DashboardViewModel dashboardViewModel;
    private boolean fromRelationship;

    private MutableLiveData<Boolean> groupByStage;
    private MutableLiveData<Boolean> filtersShowing;

    public static Intent intent(Context context,
                                String teiUid,
                                String programUid,
                                String enrollmentUid) {
        Intent intent = new Intent(context, TeiDashboardMobileActivity.class);

        intent.putExtra("TEI_UID", teiUid);
        intent.putExtra("PROGRAM_UID", programUid);
        intent.putExtra("ENROLLMENT_UID", enrollmentUid);

        return intent;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        if (savedInstanceState != null && savedInstanceState.containsKey(Constants.TRACKED_ENTITY_INSTANCE)) {
            teiUid = savedInstanceState.getString(Constants.TRACKED_ENTITY_INSTANCE);
            programUid = savedInstanceState.getString(Constants.PROGRAM_UID);
        } else {
            teiUid = getIntent().getStringExtra("TEI_UID");
            programUid = getIntent().getStringExtra("PROGRAM_UID");
            enrollmentUid = getIntent().getStringExtra("ENROLLMENT_UID");
        }

        ((App) getApplicationContext()).createDashboardComponent(new TeiDashboardModule(this, teiUid, programUid)).inject(this);
        setTheme(presenter.getProgramTheme(R.style.AppTheme));
        super.onCreate(savedInstanceState);
        groupByStage = new MutableLiveData<>(presenter.getProgramGrouping());
        filtersShowing = new MutableLiveData<>(false);
        dashboardViewModel = ViewModelProviders.of(this).get(DashboardViewModel.class);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_dashboard_mobile);
        binding.setPresenter(presenter);

        binding.setTotalFilters(filterManager.getTotalFilters());
        binding.tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == getLastTabPosition()) {
                    BadgeDrawable badge = tab.getOrCreateBadge();
                    if (badge.hasNumber() && badge.getNumber() > 0) {
                        badge.setBackgroundColor(Color.WHITE);
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getPosition() == getLastTabPosition()) {
                    BadgeDrawable badge = tab.getOrCreateBadge();
                    if (badge.hasNumber() && badge.getNumber() > 0) {
                        badge.setBackgroundColor(Color.parseColor("#B3FFFFFF"));
                    }
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                /**/
            }
        });
        binding.toolbarTitle.setLines(1);
        binding.toolbarTitle.setEllipsize(TextUtils.TruncateAt.END);

        presenter.prefSaveCurrentProgram(programUid);

        filtersShowing.observe(this, showFilter -> showHideFilters(showFilter));
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (currentAdapter == null) {
            restoreAdapter(programUid);
        }

        presenter.refreshTabCounters();
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

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        teiUid = savedInstanceState.getString(Constants.TRACKED_ENTITY_INSTANCE);
        programUid = savedInstanceState.getString(Constants.PROGRAM_UID);
        enrollmentUid = savedInstanceState.getString(Constants.ENROLLMENT_UID);
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        outState.clear();
        outState.putString(Constants.TRACKED_ENTITY_INSTANCE, teiUid);
        outState.putString(Constants.PROGRAM_UID, programUid);
        outState.putString(Constants.ENROLLMENT_UID, enrollmentUid);
        super.onSaveInstanceState(outState);
    }

    private void setViewpagerAdapter() {

        if (OrientationUtilsKt.isPortrait()) {
            binding.teiPager.setAdapter(null);
            adapter = new DashboardPagerAdapter(this, programUid, teiUid, enrollmentUid);
            currentAdapter = adapter;
            binding.teiPager.setAdapter(adapter);
            binding.teiPager.registerOnPageChangeCallback(
                    new ViewPager2.OnPageChangeCallback() {
                        @Override
                        public void onPageSelected(int position) {
                            if (position != 0) {
                                binding.filterCounter.setVisibility(View.GONE);
                                binding.searchFilterGeneral.setVisibility(View.GONE);
                            } else {
                                binding.filterCounter.setVisibility(View.VISIBLE);
                                binding.searchFilterGeneral.setVisibility(View.VISIBLE);
                            }
                        }
                    }
            );
            //binding.tabLayout.setVisibility(programUid != null ? View.VISIBLE : View.GONE);
            if (fromRelationship)
                binding.teiPager.setCurrentItem(2, false);

            tabLayoutMediator(binding.teiPager);
        } else {
            tabletAdapter = new DashboardPagerTabletAdapter(this, programUid, teiUid, enrollmentUid);
            currentAdapter = tabletAdapter;
            binding.teiTablePager.registerOnPageChangeCallback(
                    new ViewPager2.OnPageChangeCallback() {
                        @Override
                        public void onPageSelected(int position) {
                            switch (position) {
                                case 1:
                                    binding.sectionTitle.setText(getString(R.string.dashboard_relationships));
                                    break;
                                case 2:
                                    binding.sectionTitle.setText(getString(R.string.dashboard_notes));
                                    break;
                                default:
                                    binding.sectionTitle.setText(getString(R.string.dashboard_indicators));
                                    break;
                            }
                        }
                    }
            );

            binding.teiTablePager.setAdapter(tabletAdapter);
            binding.dotsIndicator.setVisibility(programUid != null ? View.VISIBLE : View.GONE);
            //binding.dotsIndicator.setViewPager(binding.teiPager); // TODO look into dots Indicator integration with viewPager 2
            if (fromRelationship)
                binding.teiTablePager.setCurrentItem(1, false);

            tabLayoutMediator(binding.teiTablePager);
        }
    }

    private void tabLayoutMediator(ViewPager2 viewPager) {
        new TabLayoutMediator(binding.tabLayout,viewPager,
                (tab, position) -> {
                    if (OrientationUtilsKt.isLandscape()) {
                        setupTabletTabTitles(tab, position);
                    } else {
                        setupTabTitles(tab, position);
                    }
                }).attach();
    }

    private void setupTabTitles(TabLayout.Tab tab, int position) {
        switch (position) {
            case 1:
                tab.setText(getString(R.string.dashboard_indicators));
                break;
            case 2:
                tab.setText(getString(R.string.dashboard_relationships));
                break;
            case 3:
                tab.setText(getString(R.string.dashboard_notes));
                break;
            default:
                tab.setText(getString(R.string.dashboard_overview));
                break;
        }
    }

    private void setupTabletTabTitles(TabLayout.Tab tab, int position) {
        if (programUid != null) {
            switch (position) {
                case 1:
                    tab.setText(getString(R.string.dashboard_relationships));
                    break;
                case 2:
                    tab.setText(getString(R.string.dashboard_notes));
                    break;
                default:
                    tab.setText(getString(R.string.dashboard_indicators));
                    break;
            }
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

        Boolean enrollmentStatus = program.getCurrentEnrollment() != null && program.getCurrentEnrollment().status() == EnrollmentStatus.ACTIVE;
        if (getIntent().getStringExtra(Constants.EVENT_UID) != null && enrollmentStatus)
            dashboardViewModel.updateEventUid(getIntent().getStringExtra(Constants.EVENT_UID));

        presenter.initNoteCounter();
    }

    @Override
    public void restoreAdapter(String programUid) {
        this.adapter = null;
        this.tabletAdapter = null;
        this.currentAdapter = null;
        this.programUid = programUid;
        presenter.init();
    }

    @Override
    public void handleTEIdeletion() {
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

        if (OrientationUtilsKt.isLandscape())
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.tei_main_view, new TEIDataFragment())
                    .commitAllowingStateLoss();
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
            if (data.hasExtra("GO_TO_ENROLLMENT")) {
                Intent intent = EnrollmentActivity.Companion.getIntent(this,
                        data.getStringExtra("GO_TO_ENROLLMENT"),
                        data.getStringExtra("GO_TO_ENROLLMENT_PROGRAM"),
                        EnrollmentActivity.EnrollmentMode.NEW);
                startActivity(intent);
                finish();
            }

            if (data.hasExtra("CHANGE_PROGRAM")) {
                startActivity(intent(this, teiUid, data.getStringExtra("CHANGE_PROGRAM"), null));
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
        if (binding.tabLayout.getSelectedTabPosition() == 0)
            setTutorial();
        else
            showToast(getString(R.string.no_intructions));

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
            binding.tabLayout.setBackgroundColor(programColor);
            if (OrientationUtilsKt.isLandscape())
                if (binding.dotsIndicator.getVisibility() == View.VISIBLE) {
                    binding.dotsIndicator.setDotIndicatorColor(programColor);
                    binding.dotsIndicator.setStrokeDotsIndicatorColor(programColor);
                }
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
            binding.tabLayout.setBackgroundColor(ContextCompat.getColor(this, colorPrimary));
            if (OrientationUtilsKt.isLandscape())
                if (binding.dotsIndicator.getVisibility() == View.VISIBLE) {
                    binding.dotsIndicator.setDotIndicatorColor(ContextCompat.getColor(this, colorPrimary));
                    binding.dotsIndicator.setStrokeDotsIndicatorColor(ContextCompat.getColor(this, colorPrimary));
                }
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

        popupMenu.getMenuInflater().inflate(
                groupByStage.getValue() ? R.menu.dashboard_menu_group : R.menu.dashboard_menu,
                popupMenu.getMenu());
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
            }
            return true;

        });
        popupMenu.show();
    }

    @Override
    public void updateNoteBadge(int numberOfNotes) {
        if (binding.tabLayout.getTabCount() > 0) {
            BadgeDrawable badge = binding.tabLayout.getTabAt(getLastTabPosition()).getOrCreateBadge();
            badge.setVisible(numberOfNotes > 0);
            badge.setBackgroundColor(Color.WHITE);
            badge.setBadgeTextColor(ColorUtils.getPrimaryColor(getContext(), ColorUtils.ColorType.PRIMARY));
            badge.setNumber(numberOfNotes);
            badge.setMaxCharacterCount(3);
        }
    }

    private int getLastTabPosition() {
        return binding.tabLayout.getTabCount() - 1;
    }

    public LiveData<Boolean> observeGrouping() {
        return groupByStage;
    }

    @Override
    public void setFiltersLayoutState() {
        filtersShowing.setValue(!filtersShowing.getValue());
    }

    private void showHideFilters(boolean showFilter) {
        if(OrientationUtilsKt.isPortrait()) {
            if (showFilter) {
                binding.tabLayout.setVisibility(View.GONE);
                binding.teiPager.setUserInputEnabled(false);
            } else {
                binding.tabLayout.setVisibility(View.VISIBLE);
                binding.teiPager.setUserInputEnabled(true);
            }
        }
    }

    public LiveData<Boolean> observeFilters() {
        return filtersShowing;
    }

}