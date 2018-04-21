package com.dhis2.usescases.teiDashboard.dashboardfragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.databinding.FragmentTeiDataBinding;
import com.dhis2.usescases.general.FragmentGlobalAbstract;
import com.dhis2.usescases.teiDashboard.DashboardProgramModel;
import com.dhis2.usescases.teiDashboard.TeiDashboardContracts;
import com.dhis2.usescases.teiDashboard.adapters.DashboardProgramAdapter;
import com.dhis2.usescases.teiDashboard.adapters.EventAdapter;
import com.dhis2.usescases.teiDashboard.mobile.TeiDashboardMobileActivity;

import static android.app.Activity.RESULT_OK;

/**
 * -Created by ppajuelo on 29/11/2017.
 */

public class TEIDataFragment extends FragmentGlobalAbstract {

    private static final int REQ_DETAILS = 1001;

    FragmentTeiDataBinding binding;

    static TEIDataFragment instance;
    TeiDashboardContracts.Presenter presenter;
    private DashboardProgramModel dashboardProgramModel;

    static public TEIDataFragment getInstance() {
        if (instance == null)
            instance = new TEIDataFragment();

        return instance;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        presenter = ((TeiDashboardMobileActivity) context).getPresenter();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tei_data, container, false);
        binding.setPresenter(presenter);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setData(dashboardProgramModel);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter = ((TeiDashboardMobileActivity) getActivity()).getPresenter();
        binding.setPresenter(presenter);
        setData(dashboardProgramModel);
    }

    public void setData(DashboardProgramModel nprogram) {
        this.dashboardProgramModel = nprogram;

        if (nprogram != null && nprogram.getCurrentEnrollment() != null) {
            binding.teiRecycler.setLayoutManager(new LinearLayoutManager(getAbstracContext()));
            binding.teiRecycler.setAdapter(new EventAdapter(presenter, nprogram.getProgramStages(), nprogram.getEvents()));
            binding.setTrackEntity(nprogram.getTei());
            binding.setEnrollment(nprogram.getCurrentEnrollment());
            binding.setProgram(nprogram.getCurrentProgram());
            binding.setDashboardModel(nprogram);
        } else if (nprogram != null) {
            binding.teiRecycler.setLayoutManager(new GridLayoutManager(getContext(), 2, LinearLayoutManager.VERTICAL, false));
            binding.teiRecycler.setAdapter(new DashboardProgramAdapter(presenter, nprogram));

            binding.setTrackEntity(nprogram.getTei());
            binding.setEnrollment(null);
            binding.setProgram(null);
            binding.setDashboardModel(nprogram);
        }

        binding.executePendingBindings();

    }

    public static int getRequestCode() {
        return REQ_DETAILS;
    }

    @SuppressLint("CheckResult")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQ_DETAILS){
            if(resultCode == RESULT_OK){
                presenter.getData();
            }
        }
    }
}