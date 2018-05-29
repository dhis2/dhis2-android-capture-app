package com.dhis2.usescases.teiDashboard.mobile;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.text.TextUtils;
import android.view.View;

import com.dhis2.R;
import com.dhis2.databinding.ActivityDashboardMobileBinding;
import com.dhis2.usescases.teiDashboard.DashboardProgramModel;
import com.dhis2.usescases.teiDashboard.TeiDashboardActivity;
import com.dhis2.usescases.teiDashboard.TeiDashboardContracts;
import com.dhis2.usescases.teiDashboard.adapters.DashboardPagerAdapter;
import com.dhis2.usescases.teiDashboard.dashboardfragments.RelationshipFragment;
import com.dhis2.usescases.teiDashboard.dashboardfragments.TEIDataFragment;

import java.util.List;

import io.reactivex.functions.Consumer;

/**
 * QUADRAM. Created by ppajuelo on 29/11/2017.
 */

public class TeiDashboardMobileActivity extends TeiDashboardActivity implements TeiDashboardContracts.View {

    ActivityDashboardMobileBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!getResources().getBoolean(R.bool.is_tablet))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
        else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_dashboard_mobile);
        binding.setPresenter(presenter);

        init(teiUid, programUid);
        binding.tabLayout.setupWithViewPager(binding.teiPager);
        binding.tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        binding.toolbarTitle.setLines(1);
        binding.toolbarTitle.setEllipsize(TextUtils.TruncateAt.END);
    }

    @Override
    protected void onResume() {
        super.onResume();
        init(teiUid, programUid);
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.onDettach();
    }

    @Override
    public void init(String teiUid, String programUid) {
        presenter.init(this, teiUid, programUid);
    }

    @Override
    public void setData(DashboardProgramModel program) {
        adapter = new DashboardPagerAdapter(getSupportFragmentManager(), program, getResources().getBoolean(R.bool.is_tablet));
        binding.teiPager.setAdapter(adapter);

        if (getResources().getBoolean(R.bool.is_tablet))
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.tei_main_view, TEIDataFragment.getInstance())
                    .commit();

        binding.setDashboardModel(program);
        binding.setTrackEntity(program.getTei());
        binding.tabLayout.setVisibility(View.VISIBLE);
        binding.executePendingBindings();
        this.programModel = program;
        TEIDataFragment.getInstance().setData(programModel);
        RelationshipFragment.getInstance().setData(program);
        binding.teiPager.setOffscreenPageLimit(6);
    }

    @Override
    public void setDataWithOutProgram(DashboardProgramModel program) {
        adapter = new DashboardPagerAdapter(getSupportFragmentManager(), program, getResources().getBoolean(R.bool.is_tablet));
        binding.teiPager.setAdapter(adapter);

        binding.setDashboardModel(program);
        binding.setTrackEntity(program.getTei());
        binding.tabLayout.setVisibility(View.GONE);
        binding.executePendingBindings();
        this.programModel = program;
        TEIDataFragment.getInstance().setData(programModel);
        binding.teiPager.setOffscreenPageLimit(6);

    }

    @Override
    public DashboardPagerAdapter getAdapter() {
        return adapter;
    }

    @Override
    public Consumer<List<Bitmap>> showQR() {
        return bitmaps -> {
            TEIDataFragment.getInstance().flipCard(bitmaps);
        };

    }

    @Override
    public void nextQR() {
        TEIDataFragment.getInstance().nextQR();
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