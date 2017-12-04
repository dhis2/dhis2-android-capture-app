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
import com.dhis2.usescases.teiDashboard.adapters.EventAdapter;

import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;

/**
 * Created by ppajuelo on 29/11/2017.
 */

public class TEIDataFragment extends FragmentGlobalAbstract {

    FragmentTeiDataBinding binding;

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
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (trackedEntity != null && program != null) {
            binding.setTrackEntity(trackedEntity);
            binding.setProgram(program.getProgram());
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
        for (Enrollment enrollment : trackedEntity.enrollments())
            if (enrollment.program().equals(program.getProgram().uid()))
                binding.teiRecycler.setAdapter(new EventAdapter(program.getProgramStages(), enrollment.events()));
        onResume();
    }

}
