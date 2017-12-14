package com.dhis2.usescases.teiDashboard.dashboardfragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.databinding.FragmentTeiDataBinding;
import com.dhis2.usescases.general.FragmentGlobalAbstract;
import com.dhis2.usescases.teiDashboard.DashboardProgramModel;
import com.dhis2.usescases.teiDashboard.TeiDashboardPresenter;
import com.dhis2.usescases.teiDashboard.adapters.EventAdapter;
import com.dhis2.usescases.teiDashboard.mobile.TeiDashboardMobileActivity;
import com.dhis2.usescases.teiDashboard.teiDataDetail.TeiDataDetailActivity;
import com.dhis2.usescases.teiDashboard.teiDataDetail.TeiDataDetailPresenter;

import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

import javax.inject.Inject;

/**
 * Created by ppajuelo on 29/11/2017.
 */

public class TEIDataFragment extends FragmentGlobalAbstract {

    FragmentTeiDataBinding binding;
    private TeiDashboardPresenter presenter;

    static TEIDataFragment instance;
    private static TrackedEntityInstance trackedEntity;
    private static DashboardProgramModel program;

    static public TEIDataFragment getInstance() {
        if (instance == null)
            instance = new TEIDataFragment();

        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_tei_data, container, false);
        presenter = ((TeiDashboardMobileActivity)getActivity()).getPresenter();
        binding.setPresenter(presenter);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (trackedEntity != null && program != null) {

            for (Enrollment enrollment : trackedEntity.enrollments())
                if (enrollment.program().equals(program.getProgram().uid()))
                    binding.teiRecycler.setAdapter(new EventAdapter(program.getProgramStages(), enrollment.events()));

            binding.setTrackEntity(trackedEntity);
            binding.setProgram(program.getProgram());
            binding.setDashboardModel(program);
            binding.executePendingBindings();
        }
    }

    @Override
    public void onDestroy() {
        instance = null;
        super.onDestroy();
    }

    public void setData(TrackedEntityInstance trackedEntity, DashboardProgramModel program) {
        this.trackedEntity = trackedEntity;
        this.program = program;
        onResume();
    }

}
