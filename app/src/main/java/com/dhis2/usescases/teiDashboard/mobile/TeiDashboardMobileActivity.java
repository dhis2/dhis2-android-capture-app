package com.dhis2.usescases.teiDashboard.mobile;

import android.content.pm.ActivityInfo;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.PopupMenu;
import android.view.Menu;
import android.view.View;

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

import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by ppajuelo on 29/11/2017.
 */

public class TeiDashboardMobileActivity extends ActivityGlobalAbstract implements TeiDashboardContracts.View {

    ActivityDashboardMobileBinding binding;
    @Inject
    TeiDashboardPresenter presenter;

    DashboardProgramModel programModel;
    TrackedEntityInstance trackedEntityInstance;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        ((App) getApplicationContext()).getUserComponent().plus(new TeiDashboardModule()).inject(this);
        if (getResources().getBoolean(R.bool.is_tablet))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_dashboard_mobile);
        binding.setPresenter(presenter);
        init(getIntent().getStringExtra("TEI_UID"), getIntent().getStringExtra("PROGRAM_UID"));
        binding.teiPager.setAdapter(new DashboardPagerAdapter(getSupportFragmentManager(), getResources().getBoolean(R.bool.is_tablet)));
        binding.tabLayout.setupWithViewPager(binding.teiPager);
        binding.tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
    }


    @Override
    public void init(String teiUid, String programUid) {
        presenter.init(this,
                teiUid,
                programUid);
    }

    @Override
    public void setData(TrackedEntityInstance trackedEntityModel, DashboardProgramModel program) {
        binding.setDashboardModel(program);
        binding.setTrackEntity(trackedEntityModel);
        binding.executePendingBindings();
        this.programModel = program;
        this.trackedEntityInstance = trackedEntityModel;
        TEIDataFragment.getInstance().setData(trackedEntityModel, program);
        RelationshipFragment.getInstance().setData(trackedEntityModel.relationships(), program.getRelationshipTypeModel());
        ScheduleFragment.getInstance().setData(trackedEntityModel, program);
    }

    @Override
    public void setDataWithOutProgram(TrackedEntityInstance trackedEntityInstance, DashboardProgramModel programModel) {
        binding.setDashboardModel(programModel);
        binding.setTrackEntity(trackedEntityInstance);
        binding.tabLayout.setVisibility(View.GONE);
        binding.executePendingBindings();
        TEIDataFragment.getInstance().setData(trackedEntityInstance, null);
    }

    @Override
    public void showEnrollmentList(List<Enrollment> enrollments) {
        PopupMenu menu = new PopupMenu(this, binding.programSelectorButton);
        int cont = 0;
        for (ProgramModel program : programModel.getEnrollmentProgramModels()) {
            menu.getMenu().add(Menu.NONE, cont++, Menu.NONE, program.displayShortName());
        }
        menu.setOnMenuItemClickListener(item -> {
            init(trackedEntityInstance.uid(), programModel.getEnrollmentProgramModels().get(item.getItemId()).uid());
            return true;
        });
        menu.show();
    }

    public TeiDashboardPresenter getPresenter() {
        return presenter;
    }

}
