package com.dhis2.usescases.teiDashboard.mobile;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;

import com.dhis2.App;
import com.dhis2.R;
import com.dhis2.databinding.ActivityDashboardMobileBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.usescases.teiDashboard.DashboardProgramModel;
import com.dhis2.usescases.teiDashboard.TeiDashboardContracts;
import com.dhis2.usescases.teiDashboard.TeiDashboardModule;
import com.dhis2.usescases.teiDashboard.TeiDashboardPresenter;
import com.dhis2.usescases.teiDashboard.adapters.DashboardPagerAdapter;
import com.dhis2.usescases.teiDashboard.dashboardfragments.RelationshipFragment;
import com.dhis2.usescases.teiDashboard.dashboardfragments.ScheduleFragment;
import com.dhis2.usescases.teiDashboard.dashboardfragments.TEIDataFragment;

import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import javax.inject.Inject;

/**
 * Created by ppajuelo on 29/11/2017.
 */

public class TeiDashboardMobileActivity extends ActivityGlobalAbstract implements TeiDashboardContracts.View {

    ActivityDashboardMobileBinding binding;
    @Inject
    TeiDashboardPresenter presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ((App) getApplicationContext()).getUserComponent().plus(new TeiDashboardModule()).inject(this);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dashboard_mobile);
        binding.setPresenter(presenter);
        presenter.init(this,
                getIntent().getStringExtra("TEI_UID"),
                getIntent().getStringExtra("PROGRAM_UID"));
        binding.teiPager.setAdapter(new DashboardPagerAdapter(getSupportFragmentManager(), false));
        binding.tabLayout.setupWithViewPager(binding.teiPager);
        binding.tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
    }


    @Override
    public void setData(TrackedEntityInstance trackedEntityModel, DashboardProgramModel program) {
        TEIDataFragment.getInstance().setData(trackedEntityModel, program);
        RelationshipFragment.getInstance().setData(trackedEntityModel.relationships());
        ScheduleFragment.getInstance().setData(trackedEntityModel, program);
    }

}
