package com.dhis2.usescases.teiDashboard.dashboardfragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.databinding.FragmentRelationshipsBinding;
import com.dhis2.usescases.general.FragmentGlobalAbstract;
import com.dhis2.usescases.teiDashboard.DashboardProgramModel;
import com.dhis2.usescases.teiDashboard.adapters.RelationshipAdapter;

/**
 * Created by ppajuelo on 29/11/2017.
 */

public class RelationshipFragment extends FragmentGlobalAbstract {

    FragmentRelationshipsBinding binding;

    static RelationshipFragment instance;
    static RelationshipAdapter relationshipAdapter;

    static public RelationshipFragment getInstance() {
        if (instance == null) {
            instance = new RelationshipFragment();
            relationshipAdapter = new RelationshipAdapter();
        }
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_relationships, container, false);
        binding.relationshipRecycler.setAdapter(new RelationshipAdapter());
        return binding.getRoot();
    }

    public void setData(DashboardProgramModel dashboardProgramModel) {
        binding.setRelationshipType(dashboardProgramModel.getCurrentProgram().relationshipText());
        relationshipAdapter.addItems(dashboardProgramModel.getRelationships());
    }
}
