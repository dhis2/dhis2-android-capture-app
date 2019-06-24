package org.dhis2.usescases.teiDashboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
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

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import org.dhis2.App;
import org.dhis2.BuildConfig;
import org.dhis2.R;
import org.dhis2.data.forms.FormActivity;
import org.dhis2.data.forms.FormViewArguments;
import org.dhis2.databinding.ActivityDashboardMobileBinding;
import org.dhis2.usescases.general.ActivityGlobalAbstract;
import org.dhis2.usescases.teiDashboard.adapters.DashboardPagerAdapter;
import org.dhis2.usescases.teiDashboard.adapters.DashboardPagerTabletAdapter;
import org.dhis2.usescases.teiDashboard.dashboardfragments.tei_data.TEIDataFragment;
import org.dhis2.usescases.teiDashboard.teiProgramList.TeiProgramListActivity;
import org.dhis2.utils.ColorUtils;
import org.dhis2.utils.ConstantsKt;
import org.dhis2.utils.HelpManager;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

import javax.inject.Inject;

import me.toptas.fancyshowcase.FancyShowCaseView;
import me.toptas.fancyshowcase.FocusShape;
import timber.log.Timber;

import static org.dhis2.utils.ConstantsKt.*;

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
    private boolean showTutorial;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(getSharedPreferences().getInt(PROGRAM_THEME, getSharedPreferences().getInt(THEME, R.style.AppTheme)));
        if (savedInstanceState != null && savedInstanceState.containsKey(TRACKED_ENTITY_INSTANCE)) {
            teiUid = savedInstanceState.getString(TRACKED_ENTITY_INSTANCE);
            programUid = savedInstanceState.getString(PROGRAM_UID);
        } else {
            teiUid = getIntent().getStringExtra("TEI_UID");
            programUid = getIntent().getStringExtra("PROGRAM_UID");
        }
        ((App) getApplicationContext()).createDashboardComponent(new TeiDashboardModule(teiUid, programUid)).inject(this);
        super.onCreate(savedInstanceState);
        dashboardViewModel = ViewModelProviders.of(this).get(DashboardViewModel.class);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_dashboard_mobile);
        binding.setPresenter(presenter);

        binding.tabLayout.setupWithViewPager(binding.teiPager);
        binding.tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        binding.toolbarTitle.setLines(1);
        binding.toolbarTitle.setEllipsize(TextUtils.TruncateAt.END);

        getSharedPreferences(SHARE_PREFS, Context.MODE_PRIVATE)
                .edit().putString(PREVIOUS_DASHBOARD_PROGRAM, programUid).apply();
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (((App) getApplicationContext()).dashboardComponent() == null)
            ((App) getApplicationContext())
                    .createDashboardComponent(new TeiDashboardModule(teiUid, programUid))
                    .inject(this);

        String prevDashboardProgram = getSharedPreferences(SHARE_PREFS, Context.MODE_PRIVATE)
                .getString(PREVIOUS_DASHBOARD_PROGRAM, null);
        if (!changingProgram && prevDashboardProgram != null && !prevDashboardProgram.equals(programUid)) {
            finish();
        } else {
            orientation = Resources.getSystem().getConfiguration().orientation;
            restoreAdapter(programUid);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ((App) getApplicationContext()).releaseDashboardComponent();
        presenter.onDettach();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        orientation = Resources.getSystem().getConfiguration().orientation;
        teiUid = savedInstanceState.getString(TRACKED_ENTITY_INSTANCE);
        programUid = savedInstanceState.getString(PROGRAM_UID);
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        outState.clear();
        outState.putString(TRACKED_ENTITY_INSTANCE, teiUid);
        outState.putString(PROGRAM_UID, programUid);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void init(String teiUid, String programUid) {
        presenter.init(this, teiUid, programUid);
    }

    private void setViewpagerAdapter() {

        for (Fragment fragment : getSupportFragmentManager().getFragments())
            getSupportFragmentManager().beginTransaction().remove(fragment).commitNow();

        binding.teiPager.setAdapter(null);
        binding.teiPager.invalidate();

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            adapter = new DashboardPagerAdapter(this, getSupportFragmentManager(), programUid);
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
            binding.dotsIndicator.setVisibility(View.VISIBLE);
            binding.dotsIndicator.setViewPager(binding.teiPager);
            if (fromRelationship)
                binding.teiPager.setCurrentItem(1, false);
        }

    }

    @Override
    public void setData(DashboardProgramModel program) {

        dashboardViewModel.updateDashboard(program);
        setProgramColor(program.getObjectStyleForProgram(program.getCurrentProgram().uid()).color());


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

        Boolean enrollmentStatus = program.getCurrentEnrollment() != null && program.getCurrentEnrollment().enrollmentStatus() == EnrollmentStatus.ACTIVE;
        if (getIntent().getStringExtra(EVENT_UID) != null && enrollmentStatus)
            dashboardViewModel.updateEventUid(getIntent().getStringExtra(EVENT_UID));

        if (!HelpManager.getInstance().isTutorialReadyForScreen(getClass().getName()) && !fromRelationship) {
            setTutorial();
        }
    }

    @Override
    public void restoreAdapter(String programUid) {
        this.adapter = null;
        this.tabletAdapter = null;
        this.currentAdapter = null;
        this.programUid = programUid;
        binding.teiPager.setAdapter(null);
        presenter.init(this, teiUid, programUid);
    }

   /* @Override
    public void showCatComboDialog(String eventId, CategoryCombo categoryCombo) {
        CategoryComboDialog dialog = new CategoryComboDialog(getAbstracContext(), categoryCombo, 123,
                selectedOption -> presenter.changeCatOption(eventId, selectedOption), categoryCombo.displayName());
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }*/

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

    /*@Override
    public FragmentStatePagerAdapter getAdapter() {
        return currentAdapter;
    }*/

    /*@Override
    public void showQR() {
        Intent intent = new Intent(TeiDashboardMobileActivity.this, QrActivity.class);
        intent.putExtra("TEI_UID", teiUid);
        startActivity(intent);
    }*/

    @Override
    public void goToEnrollmentList(Bundle extras) {
        Intent intent = new Intent(this, TeiProgramListActivity.class);
        intent.putExtras(extras);
        startActivityForResult(intent, RQ_ENROLLMENTS);
    }

    @Override
    public String getToolbarTitle() {
        return binding.toolbarTitle.getText().toString();
    }

    public TeiDashboardContracts.Presenter getPresenter() {
        return presenter;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RQ_ENROLLMENTS && resultCode == RESULT_OK) {
            if (data.hasExtra("GO_TO_ENROLLMENT")) {
                FormViewArguments formViewArguments = FormViewArguments.createForEnrollment(data.getStringExtra("GO_TO_ENROLLMENT"));
                startActivity(FormActivity.create(this, formViewArguments, true));
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
        super.setTutorial();

        SharedPreferences prefs = getAbstracContext().getSharedPreferences(
                SHARE_PREFS, Context.MODE_PRIVATE);

        new Handler().postDelayed(() -> {
            if (getAbstractActivity() != null) {
                FancyShowCaseView tuto1 = new FancyShowCaseView.Builder(getAbstractActivity())
                        .title(getString(R.string.tuto_dashboard_1))
                        .enableAutoTextPosition()
                        .closeOnTouch(true)
                        .build();
                FancyShowCaseView tuto2 = new FancyShowCaseView.Builder(getAbstractActivity())
                        .title(getString(R.string.tuto_dashboard_2))
                        .enableAutoTextPosition()
                        .focusOn(getAbstractActivity().findViewById(R.id.viewMore))
                        .focusShape(FocusShape.ROUNDED_RECTANGLE)
                        .titleGravity(Gravity.BOTTOM)
                        .closeOnTouch(true)
                        .build();
                FancyShowCaseView tuto3 = new FancyShowCaseView.Builder(getAbstractActivity())
                        .title(getString(R.string.tuto_dashboard_3))
                        .enableAutoTextPosition()
                        .focusOn(getAbstractActivity().findViewById(R.id.shareContainer))
                        .focusShape(FocusShape.ROUNDED_RECTANGLE)
                        .titleGravity(Gravity.BOTTOM)
                        .closeOnTouch(true)
                        .build();
                FancyShowCaseView tuto4 = new FancyShowCaseView.Builder(getAbstractActivity())
                        .title(getString(R.string.tuto_dashboard_4))
                        .enableAutoTextPosition()
                        .focusOn(getAbstractActivity().findViewById(R.id.follow_up))
                        .closeOnTouch(true)
                        .build();
                FancyShowCaseView tuto5 = new FancyShowCaseView.Builder(getAbstractActivity())
                        .title(getString(R.string.tuto_dashboard_5))
                        .enableAutoTextPosition()
                        .focusOn(getAbstractActivity().findViewById(R.id.fab))
                        .closeOnTouch(true)
                        .build();
                FancyShowCaseView tuto6 = new FancyShowCaseView.Builder(getAbstractActivity())
                        .title(getString(R.string.tuto_dashboard_6))
                        .enableAutoTextPosition()
                        .focusOn(getAbstractActivity().findViewById(R.id.tei_recycler))
                        .focusShape(FocusShape.ROUNDED_RECTANGLE)
                        .titleGravity(Gravity.TOP)
                        .closeOnTouch(true)
                        .build();
                FancyShowCaseView tuto7 = new FancyShowCaseView.Builder(getAbstractActivity())
                        .title(getString(R.string.tuto_dashboard_7))
                        .enableAutoTextPosition()
                        .focusOn(getAbstractActivity().findViewById(R.id.tab_layout))
                        .focusShape(FocusShape.ROUNDED_RECTANGLE)
                        .closeOnTouch(true)
                        .build();
                FancyShowCaseView tuto8 = new FancyShowCaseView.Builder(getAbstractActivity())
                        .title(getString(R.string.tuto_dashboard_8))
                        .enableAutoTextPosition()
                        .focusOn(getAbstractActivity().findViewById(R.id.program_selector_button))
                        .closeOnTouch(true)
                        .build();

                ArrayList<FancyShowCaseView> steps = new ArrayList<>();
                steps.add(tuto1);
                steps.add(tuto2);
                steps.add(tuto3);
                steps.add(tuto4);
                steps.add(tuto5);
                steps.add(tuto6);
                steps.add(tuto7);
                steps.add(tuto8);

                HelpManager.getInstance().setScreenHelp(getClass().getName(), steps);

                if (!prefs.getBoolean("TUTO_DASHBOARD_SHOWN", false) && !BuildConfig.DEBUG || showTutorial) {
                    HelpManager.getInstance().showHelp();
                    prefs.edit().putBoolean("TUTO_DASHBOARD_SHOWN", true).apply();
                    showTutorial = true;
                }
            }

        }, 500);


    }

    @Override
    public void showTutorial(boolean shaked) {
        if (binding.tabLayout.getSelectedTabPosition() == 0)
            super.showTutorial(shaked);
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
        int programTheme = ColorUtils.Companion.getThemeFromColor(color);
        int programColor = ColorUtils.Companion.getColorFrom(color, ColorUtils.Companion.getPrimaryColor(this, ColorUtils.ColorType.PRIMARY));


        SharedPreferences prefs = getAbstracContext().getSharedPreferences(
                SHARE_PREFS, Context.MODE_PRIVATE);
        if (programTheme != -1) {
            prefs.edit().putInt(PROGRAM_THEME, programTheme).apply();
            binding.toolbar.setBackgroundColor(programColor);
            binding.tabLayout.setBackgroundColor(programColor);
            if (getOrientation() == Configuration.ORIENTATION_LANDSCAPE)
                if (binding.dotsIndicator.getVisibility() == View.VISIBLE) {
                    binding.dotsIndicator.setDotIndicatorColor(programColor);
                    binding.dotsIndicator.setStrokeDotsIndicatorColor(programColor);
                }
        } else {
            prefs.edit().remove(PROGRAM_THEME).apply();
            int colorPrimary;
            switch (prefs.getInt(THEME, R.style.AppTheme)) {
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
        setTheme(prefs.getInt(PROGRAM_THEME, prefs.getInt(THEME, R.style.AppTheme)));

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
        popupMenu.getMenuInflater().inflate(R.menu.home_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            this.showTutorial = true;
            setTutorial();
            return false;
        });
        popupMenu.show();
    }

    ;
}