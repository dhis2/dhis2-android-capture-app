package org.dhis2.usescases.teiDashboard.dashboardfragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.FragmentIndicatorsBinding;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.usescases.teiDashboard.TeiDashboardContracts;
import org.dhis2.usescases.teiDashboard.adapters.IndicatorsAdapter;
import org.dhis2.usescases.teiDashboard.mobile.TeiDashboardMobileActivity;
import org.hisp.dhis.android.core.program.ProgramIndicatorModel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import io.reactivex.functions.Consumer;


/**
 * QUADRAM. Created by ppajuelo on 29/11/2017.
 */

public class IndicatorsFragment extends FragmentGlobalAbstract {

    private static IndicatorsFragment instance;
    private IndicatorsAdapter adapter;

    private TeiDashboardContracts.Presenter presenter;

    static public IndicatorsFragment getInstance() {
        if (instance == null)
            instance = new IndicatorsFragment();
        return instance;
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        presenter = ((TeiDashboardMobileActivity) context).getPresenter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        FragmentIndicatorsBinding binding = DataBindingUtil.inflate(inflater, R.layout.fragment_indicators, container, false);
        adapter = new IndicatorsAdapter();
        binding.indicatorsRecycler.setAdapter(adapter);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.subscribeToIndicators(this);
    }

    @Override
    public void onDestroy() {
        instance = null;
        super.onDestroy();
    }

    public Consumer<List<Trio<ProgramIndicatorModel, String, String>>> swapIndicators() {
        return indicators -> adapter.setIndicators(indicators);
    }

    public void addIndicator(Trio<ProgramIndicatorModel, String, String> indicator) {

        getActivity().runOnUiThread(() -> adapter.addIndicator(indicator));
    }

    public static Fragment createInstance() {
        return instance = new IndicatorsFragment();
    }
}