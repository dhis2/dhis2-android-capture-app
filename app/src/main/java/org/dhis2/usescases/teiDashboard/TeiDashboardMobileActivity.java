package org.dhis2.usescases.teiDashboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
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
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayout;

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
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.inject.Inject;

import timber.log.Timber;

import static org.dhis2.utils.analytics.AnalyticsConstants.CLICK;
import static org.dhis2.utils.analytics.AnalyticsConstants.SHOW_HELP;

/**
 * QUADRAM. Created by ppajuelo on 29/11/2017.
 */

public class TeiDashboardMobileActivity extends ActivityGlobalAbstract implements TeiDashboardContracts.View {

    @Inject
    public TeiDashboardContracts.Presenter presenter;

    protected DashboardProgramModel programModel;

    protected String teiUid;
    protected String programUid;

    ActivityDashboardMobileBinding binding;
    protected DashboardPagerAdapter adapter;
    protected DashboardPagerTabletAdapter tabletAdapter;
    protected FragmentStatePagerAdapter currentAdapter;
    private int orientation;
    private boolean changingProgram;

    private DashboardViewModel dashboardViewModel;
    private boolean fromRelationship;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(getSharedPreferences().getInt(Constants.PROGRAM_THEME, getSharedPreferences().getInt(Constants.THEME, R.style.AppTheme)));
        if (savedInstanceState != null && savedInstanceState.containsKey(Constants.TRACKED_ENTITY_INSTANCE)) {
            teiUid = savedInstanceState.getString(Constants.TRACKED_ENTITY_INSTANCE);
            programUid = savedInstanceState.getString(Constants.PROGRAM_UID);
        } else {
            teiUid = getIntent().getStringExtra("TEI_UID");
            programUid = getIntent().getStringExtra("PROGRAM_UID");
        }
        ((App) getApplicationContext()).createDashboardComponent(new TeiDashboardModule(this, teiUid, programUid)).inject(this);
        super.onCreate(savedInstanceState);
        dashboardViewModel = ViewModelProviders.of(this).get(DashboardViewModel.class);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_dashboard_mobile);
        binding.setPresenter(presenter);

        binding.tabLayout.setupWithViewPager(binding.teiPager);
        binding.tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        binding.toolbarTitle.setLines(1);
        binding.toolbarTitle.setEllipsize(TextUtils.TruncateAt.END);

        getSharedPreferences(Constants.SHARE_PREFS, Context.MODE_PRIVATE)
                .edit().putString(Constants.PREVIOUS_DASHBOARD_PROGRAM, programUid).apply();
    }


    @Override
    protected void onResume() {
        super.onResume();

        /*if (((App) getApplicationContext()).dashboardComponent() == null)
            ((App) getApplicationContext())
                    .createDashboardComponent(new TeiDashboardModule(this, teiUid, programUid))
                    .inject(this);*/

        String prevDashboardProgram = getSharedPreferences(Constants.SHARE_PREFS, Context.MODE_PRIVATE)
                .getString(Constants.PREVIOUS_DASHBOARD_PROGRAM, null);
        if (!changingProgram && prevDashboardProgram != null && !prevDashboardProgram.equals(programUid)) {
            finish();
        } else {
            orientation = Resources.getSystem().getConfiguration().orientation;
            if (currentAdapter == null) {
                restoreAdapter(programUid);
            }
        }

        presenter.refreshTabCounters();
    }

    @Override
    protected void onPause() {
        presenter.onDettach();
        ((App) getApplicationContext()).releaseDashboardComponent();
        super.onPause();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        orientation = Resources.getSystem().getConfiguration().orientation;
        teiUid = savedInstanceState.getString(Constants.TRACKED_ENTITY_INSTANCE);
        programUid = savedInstanceState.getString(Constants.PROGRAM_UID);
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        outState.clear();
        outState.putString(Constants.TRACKED_ENTITY_INSTANCE, teiUid);
        outState.putString(Constants.PROGRAM_UID, programUid);
        super.onSaveInstanceState(outState);
    }

    private void setViewpagerAdapter() {

        for (Fragment fragment : getSupportFragmentManager().getFragments())
            getSupportFragmentManager().beginTransaction().remove(fragment).commitNow();

        binding.teiPager.setAdapter(null);
        binding.teiPager.invalidate();

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            adapter = new DashboardPagerAdapter(this, getSupportFragmentManager(), programUid, teiUid);
            currentAdapter = adapter;
            binding.teiPager.setAdapter(adapter);
            binding.tabLayout.setVisibility(View.VISIBLE);
            if (fromRelationship)
                binding.teiPager.setCurrentItem(2, false);
        } else {
            tabletAdapter = new DashboardPagerTabletAdapter(this, getSupportFragmentManager(), programUid);
            currentAdapter = tabletAdapter;
            binding.teiPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int i, float v, int i1) {
                    // nothing
                }

                @Override
                public void onPageSelected(int i) {
                    binding.sectionTitle.setText(tabletAdapter.getPageTitle(i));
                }

                @Override
                public void onPageScrollStateChanged(int i) {
                    // nothing
                }
            });
            binding.sectionTitle.setText(tabletAdapter.getPageTitle(0));
            binding.teiPager.setAdapter(tabletAdapter);
            binding.tabLayout.setVisibility(View.GONE);
            binding.dotsIndicator.setVisibility(programUid != null ? View.VISIBLE : View.GONE);
            binding.dotsIndicator.setViewPager(binding.teiPager);
            if (fromRelationship)
                binding.teiPager.setCurrentItem(1, false);
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

        if (binding.teiPager.getAdapter() == null) {
            setViewpagerAdapter();
        }

        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.tei_main_view, new TEIDataFragment())
                    .commitAllowingStateLoss();

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
        binding.teiPager.setAdapter(null);
        presenter.init();
    }

    @Override
    public void handleTEIdeletion() {
        finish();
    }

    @Override
    public void handleEnrollmentDeletion(Boolean hasMoreEnrollments) {
        if (hasMoreEnrollments) {
            Bundle bundle = new Bundle();
            bundle.putString("TEI_UID", teiUid);
            bundle.putString("PROGRAM_UID", null);
            startActivity(TeiDashboardMobileActivity.class, bundle, true, false, null);
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

        if (orientation == Configuration.ORIENTATION_LANDSCAPE)
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
                programUid = data.getStringExtra("CHANGE_PROGRAM");
                adapter = null;
                tabletAdapter = null;
                currentAdapter = null;
                changingProgram = true;
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
        if (binding.tabLayout.getSelectedTabPosition() == 0 && !changingProgram)
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

    public int getOrientation() {
        return orientation;
    }


    private void setProgramColor(String color) {
        int programTheme = ColorUtils.getThemeFromColor(color);
        int programColor = ColorUtils.getColorFrom(color, ColorUtils.getPrimaryColor(this, ColorUtils.ColorType.PRIMARY));


        SharedPreferences prefs = getAbstracContext().getSharedPreferences(
                Constants.SHARE_PREFS, Context.MODE_PRIVATE);
        if (programTheme != -1) {
            prefs.edit().putInt(Constants.PROGRAM_THEME, programTheme).apply();
            binding.toolbar.setBackgroundColor(programColor);
            binding.tabLayout.setBackgroundColor(programColor);
            if (getOrientation() == Configuration.ORIENTATION_LANDSCAPE)
                if (binding.dotsIndicator.getVisibility() == View.VISIBLE) {
                    binding.dotsIndicator.setDotIndicatorColor(programColor);
                    binding.dotsIndicator.setStrokeDotsIndicatorColor(programColor);
                }
        } else {
            prefs.edit().remove(Constants.PROGRAM_THEME).apply();
            int colorPrimary;
            switch (prefs.getInt(Constants.THEME, R.style.AppTheme)) {
                case R.style.AppTheme:
                    colorPrimary = R.color.colorPrimary;
                    break;
                case R.style.RedTheme:
                    colorPrimary = R.color.colorPrimaryRed;
                    break;
                case R.style.OrangeTheme:
                    colorPrimary = R.color.colorPrimaryOrange;
                    break;
                case R.style.GreenTheme:
                    colorPrimary = R.color.colorPrimaryGreen;
                    break;
                default:
                    colorPrimary = R.color.colorPrimary;
                    break;
            }
            binding.toolbar.setBackgroundColor(ContextCompat.getColor(this, colorPrimary));
            binding.tabLayout.setBackgroundColor(ContextCompat.getColor(this, colorPrimary));
            if (getOrientation() == Configuration.ORIENTATION_LANDSCAPE)
                if (binding.dotsIndicator.getVisibility() == View.VISIBLE) {
                    binding.dotsIndicator.setDotIndicatorColor(ContextCompat.getColor(this, colorPrimary));
                    binding.dotsIndicator.setStrokeDotsIndicatorColor(ContextCompat.getColor(this, colorPrimary));
                }
        }

        binding.executePendingBindings();
        setTheme(prefs.getInt(Constants.PROGRAM_THEME, prefs.getInt(Constants.THEME, R.style.AppTheme)));

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
        popupMenu.getMenuInflater().inflate(R.menu.dashboard_menu, popupMenu.getMenu());
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
            }
            return true;

        });
        popupMenu.show();
    }

    @Override
    public void updateNoteBadge(int numberOfNotes) {
        BadgeDrawable badge = binding.tabLayout.getTabAt(binding.tabLayout.getTabCount() - 1).getOrCreateBadge();
        badge.setVisible(numberOfNotes > 0);
        badge.setBackgroundColor(Color.WHITE);
        badge.setBadgeTextColor(ColorUtils.getPrimaryColor(getContext(), ColorUtils.ColorType.PRIMARY));
        badge.setNumber(numberOfNotes);
        badge.setMaxCharacterCount(3);
    }
}