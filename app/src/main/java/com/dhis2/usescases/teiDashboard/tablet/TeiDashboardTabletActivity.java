package com.dhis2.usescases.teiDashboard.tablet;

import android.content.pm.ActivityInfo;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.dhis2.R;
import com.dhis2.databinding.ActivityDashboardTabletBinding;
import com.dhis2.usescases.general.ActivityGlobalAbstract;
import com.dhis2.usescases.teiDashboard.DashboardProgramModel;
import com.dhis2.usescases.teiDashboard.TeiDashboardContracts;
import com.dhis2.usescases.teiDashboard.adapters.DashboardPagerAdapter;
import com.dhis2.usescases.teiDashboard.dashboardfragments.TEIDataFragment;

/**
 * Created by ppajuelo on 29/11/2017.
 *
 */

public class TeiDashboardTabletActivity extends ActivityGlobalAbstract implements TeiDashboardContracts.View {

    ActivityDashboardTabletBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_dashboard_tablet);
//        binding.setPresenter(new TeiDashboardPresenter());
        binding.teiPager.setAdapter(new DashboardPagerAdapter(getSupportFragmentManager(), null, true));
        getSupportFragmentManager().beginTransaction().add(R.id.tei_main_view, TEIDataFragment.getInstance()).commit();

    }

    @Override
    public void init(String teUid, String programUid) {

    }

    @Override
    public void setData(DashboardProgramModel program) {
        TEIDataFragment.getInstance().setData(program);
    }

    @Override
    public void setDataWithOutProgram(DashboardProgramModel programModel) {

    }

    @Override
    public String getToolbarTitle() {
        return binding.toolbarTitle.getText().toString();
    }

}
