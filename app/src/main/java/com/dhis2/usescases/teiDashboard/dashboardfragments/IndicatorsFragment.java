package com.dhis2.usescases.teiDashboard.dashboardfragments;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dhis2.R;
import com.dhis2.databinding.FragmentIndicatorsBinding;
import com.dhis2.usescases.general.FragmentGlobalAbstract;
import com.dhis2.usescases.teiDashboard.adapters.IndicatorsAdapter;

import org.hisp.dhis.android.core.program.ProgramIndicatorModel;

import java.util.List;


/**
 * Created by ppajuelo on 29/11/2017.
 */

public class IndicatorsFragment extends FragmentGlobalAbstract {

    FragmentIndicatorsBinding binding;

    private List<ProgramIndicatorModel> programIndicatorModels;

    static IndicatorsFragment instance;

    private Context context;

    static public IndicatorsFragment getInstance() {
        if (instance == null)
            instance = new IndicatorsFragment();
        return instance;
    }

    public void setData(List<ProgramIndicatorModel> programIndicatorModels){
        this.programIndicatorModels = programIndicatorModels;
        binding.indicatorsRecycler.setLayoutManager(new LinearLayoutManager(context));
        binding.indicatorsRecycler.setAdapter(new IndicatorsAdapter(programIndicatorModels));
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_indicators, container, false);
        this.context = container.getContext();
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setData(programIndicatorModels);
    }

    @Override
    public void onResume() {
        super.onResume();
        setData(programIndicatorModels);
    }

    @Override
    public void onDestroy() {
        instance = null;
        super.onDestroy();
    }
}