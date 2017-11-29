package com.dhis2.usescases.teiDashboard.dashboardfragments;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.databinding.FragmentScheduleBinding;
import com.dhis2.usescases.general.FragmentGlobalAbstract;
import com.dhis2.usescases.teiDashboard.ScheduleAdapter;

import java.util.ArrayList;

import dagger.android.support.AndroidSupportInjection;

/**
 * Created by ppajuelo on 29/11/2017.
 */

public class ScheduleFragment extends FragmentGlobalAbstract {

    FragmentScheduleBinding binding;

    static ScheduleFragment instance;

    public static ScheduleFragment getInstance() {
        if (instance == null)
            instance = new ScheduleFragment();
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AndroidSupportInjection.inject(this);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_schedule, container, false);
        binding.scheduleRecycler.setAdapter(new ScheduleAdapter(new ArrayList<>()));
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        instance = null;
        super.onDestroy();
    }
}
