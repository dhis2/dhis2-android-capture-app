package com.dhis2.usescases.teiDashboard.mobile;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.view.View;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.databinding.ActivityDashboardMobileBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.usescases.teiDashboard.DashboardProgramModel;
import com.dhis2.usescases.teiDashboard.TeiDashboardContracts;
import com.dhis2.usescases.teiDashboard.TeiDashboardModule;
import com.dhis2.usescases.teiDashboard.adapters.DashboardPagerAdapter;
import com.dhis2.usescases.teiDashboard.dashboardfragments.IndicatorsFragment;
import com.dhis2.usescases.teiDashboard.dashboardfragments.RelationshipFragment;
import com.dhis2.usescases.teiDashboard.dashboardfragments.ScheduleFragment;
import com.dhis2.usescases.teiDashboard.dashboardfragments.TEIDataFragment;

import org.hisp.dhis.android.core.program.ProgramIndicatorModel;

import javax.inject.Inject;

/**
 * Created by ppajuelo on 29/11/2017.
 *
 */

public class TeiDashboardMobileActivity extends ActivityGlobalAbstract implements TeiDashboardContracts.View {

    ActivityDashboardMobileBinding binding;
    @Inject
    TeiDashboardContracts.Presenter presenter;

    DashboardProgramModel programModel;
    DashboardPagerAdapter adapter;

    String teiUid;
    String programUid;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ((App) getApplicationContext()).userComponent().plus(new TeiDashboardModule()).inject(this);
        if (getResources().getBoolean(R.bool.is_tablet))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dashboard_mobile);
        binding.setPresenter(presenter);
        teiUid = getIntent().getStringExtra("TEI_UID");
        programUid = getIntent().getStringExtra("PROGRAM_UID");
        init(teiUid, programUid);
        binding.tabLayout.setupWithViewPager(binding.teiPager);
        binding.tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
    }

    @Override
    public void init(String teiUid, String programUid) {
        presenter.init(this, teiUid, programUid);
    }

    @Override
    public void setData(DashboardProgramModel program) {
        adapter = new DashboardPagerAdapter(getSupportFragmentManager(), program, getResources().getBoolean(R.bool.is_tablet));
        binding.teiPager.setAdapter(adapter);

        binding.setDashboardModel(program);
        binding.setTrackEntity(program.getTei());
        binding.executePendingBindings();
        this.programModel = program;
        TEIDataFragment.getInstance().setData(programModel);
        IndicatorsFragment.getInstance().setData(program.getProgramIndicatorModels());
        ScheduleFragment.getInstance().setData(programUid, program);
        RelationshipFragment.getInstance().setData(program);
        binding.teiPager.setOffscreenPageLimit(6);
    }

    @Override
    public void setDataWithOutProgram(DashboardProgramModel program) {
        binding.teiPager.setAdapter(new DashboardPagerAdapter(getSupportFragmentManager(), program, getResources().getBoolean(R.bool.is_tablet)));
        binding.teiPager.setOffscreenPageLimit(6);

        binding.setDashboardModel(program);
        binding.setTrackEntity(program.getTei());
        binding.tabLayout.setVisibility(View.GONE);
        binding.executePendingBindings();
        this.programModel = program;
        TEIDataFragment.getInstance().setData(programModel);
    }

    @Override
    public DashboardPagerAdapter getAdapter() {
        return adapter;
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
        super.onActivityResult(requestCode, resultCode, data);
    }
}