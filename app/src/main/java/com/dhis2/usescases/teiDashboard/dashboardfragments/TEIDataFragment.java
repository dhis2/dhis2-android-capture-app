package com.dhis2.usescases.teiDashboard.dashboardfragments;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
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
    private static DashboardProgramModel program;
    TeiDashboardContracts.Presenter presenter;

    static public TEIDataFragment getInstance() {
        if (instance == null)
            instance = new TEIDataFragment();

        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tei_data, container, false);
        presenter = ((TeiDashboardMobileActivity) getActivity()).getPresenter();
        binding.setPresenter(presenter);
        if(program!=null)
            setData(program);
        return binding.getRoot();
    }

    public void setData(DashboardProgramModel nprogram) {
        program = nprogram;

        if (program != null && program.getCurrentEnrollment() != null) {

            binding.teiRecycler.setAdapter(new EventAdapter(presenter, program.getProgramStages(), program.getEvents()));

            binding.setTrackEntity(program.getTei());
            binding.setEnrollment(program.getCurrentEnrollment());
            binding.setProgram(program.getCurrentProgram());
            binding.setDashboardModel(program);
        } else if (program != null) {
            binding.teiRecycler.setLayoutManager(new GridLayoutManager(getContext(), 2, LinearLayoutManager.VERTICAL, false));
            binding.teiRecycler.setAdapter(new DashboardProgramAdapter(presenter, program));

            binding.setTrackEntity(program.getTei());
            binding.setEnrollment(null);
            binding.setProgram(null);
            binding.setDashboardModel(program);
        }

        binding.executePendingBindings();

    }

    public static int getRequestCode() {
        return REQ_DETAILS;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQ_DETAILS){
            if(resultCode == RESULT_OK){
                presenter.getProgram()
                        .subscribe(this::setData,
                                throwable -> Log.d("ERROR", throwable.getMessage()));
            }
        }
    }
}
