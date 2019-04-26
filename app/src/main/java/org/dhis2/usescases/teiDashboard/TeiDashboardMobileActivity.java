package org.dhis2.usescases.teiDashboard;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;

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
import org.dhis2.utils.Constants;
import org.dhis2.utils.HelpManager;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import javax.inject.Inject;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;
import me.toptas.fancyshowcase.FancyShowCaseView;
import me.toptas.fancyshowcase.FocusShape;

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
        ((App) getApplicationContext()).createDashboardComponent(new TeiDashboardModule(teiUid, programUid)).inject(this);
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

        if(((App) getApplicationContext()).dashboardComponent()==null)
            ((App) getApplicationContext())
                    .createDashboardComponent(new TeiDashboardModule(teiUid, programUid))
                    .inject(this);

        String prevDashboardProgram = getSharedPreferences(Constants.SHARE_PREFS, Context.MODE_PRIVATE)
                .getString(Constants.PREVIOUS_DASHBOARD_PROGRAM, null);
        if (!changingProgram && prevDashboardProgram != null && !prevDashboardProgram.equals(programUid)) {
            finish();
        } else {
            orientation = Resources.getSystem().getConfiguration().orientation;
            init(teiUid, programUid);
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
        }

    }

    @Override
    public void setData(DashboardProgramModel program) {

        dashboardViewModel.updateDashboard(program);


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
        if (getIntent().getStringExtra(Constants.EVENT_UID) != null && enrollmentStatus)
            dashboardViewModel.updateEventUid(getIntent().getStringExtra(Constants.EVENT_UID));

        if (!HelpManager.getInstance().isTutorialReadyForScreen(getClass().getName())) {
            setTutorial();
        }
    }

    @Override
    public void restoreAdapter(String programUid) {
        this.adapter = null;
        this.tabletAdapter = null;
        this.currentAdapter = null;
        this.programUid = programUid;
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
        startActivityForResult(intent, Constants.RQ_ENROLLMENTS);
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
        if (requestCode == Constants.RQ_ENROLLMENTS && resultCode == RESULT_OK) {
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
                Constants.SHARE_PREFS, Context.MODE_PRIVATE);

        new Handler().postDelayed(() -> {
            FancyShowCaseView tuto1 = new FancyShowCaseView.Builder(getAbstractActivity())
                    .title(getString(R.string.tuto_dashboard_1))
                    .closeOnTouch(true)
                    .build();
            FancyShowCaseView tuto2 = new FancyShowCaseView.Builder(getAbstractActivity())
                    .title(getString(R.string.tuto_dashboard_2))
                    .focusOn(getAbstractActivity().findViewById(R.id.viewMore))
                    .focusShape(FocusShape.ROUNDED_RECTANGLE)
                    .titleGravity(Gravity.BOTTOM)
                    .closeOnTouch(true)
                    .build();
            FancyShowCaseView tuto3 = new FancyShowCaseView.Builder(getAbstractActivity())
                    .title(getString(R.string.tuto_dashboard_3))
                    .focusOn(getAbstractActivity().findViewById(R.id.shareContainer))
                    .focusShape(FocusShape.ROUNDED_RECTANGLE)
                    .titleGravity(Gravity.BOTTOM)
                    .closeOnTouch(true)
                    .build();
            FancyShowCaseView tuto4 = new FancyShowCaseView.Builder(getAbstractActivity())
                    .title(getString(R.string.tuto_dashboard_4))
                    .focusOn(getAbstractActivity().findViewById(R.id.follow_up))
                    .closeOnTouch(true)
                    .build();
            FancyShowCaseView tuto5 = new FancyShowCaseView.Builder(getAbstractActivity())
                    .title(getString(R.string.tuto_dashboard_5))
                    .focusOn(getAbstractActivity().findViewById(R.id.fab))
                    .closeOnTouch(true)
                    .build();
            FancyShowCaseView tuto6 = new FancyShowCaseView.Builder(getAbstractActivity())
                    .title(getString(R.string.tuto_dashboard_6))
                    .focusOn(getAbstractActivity().findViewById(R.id.tei_recycler))
                    .focusShape(FocusShape.ROUNDED_RECTANGLE)
                    .titleGravity(Gravity.TOP)
                    .closeOnTouch(true)
                    .build();
            FancyShowCaseView tuto7 = new FancyShowCaseView.Builder(getAbstractActivity())
                    .title(getString(R.string.tuto_dashboard_7))
                    .focusOn(getAbstractActivity().findViewById(R.id.tab_layout))
                    .focusShape(FocusShape.ROUNDED_RECTANGLE)
                    .closeOnTouch(true)
                    .build();
            FancyShowCaseView tuto8 = new FancyShowCaseView.Builder(getAbstractActivity())
                    .title(getString(R.string.tuto_dashboard_8))
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

            if (!prefs.getBoolean("TUTO_DASHBOARD_SHOWN", false) && !BuildConfig.DEBUG) {
                HelpManager.getInstance().showHelp();/* getAbstractActivity().fancyShowCaseQueue.show();*/
                prefs.edit().putBoolean("TUTO_DASHBOARD_SHOWN", true).apply();
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

}