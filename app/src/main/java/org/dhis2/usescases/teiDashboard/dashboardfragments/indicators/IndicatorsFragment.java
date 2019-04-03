package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dhis2.Components;
import org.dhis2.R;
import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.FragmentIndicatorsBinding;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.usescases.teiDashboard.adapters.IndicatorsAdapter;
import org.hisp.dhis.android.core.program.ProgramIndicatorModel;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

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

    private String programUid;
    private String teiUid;

//    @Inject
    private IndicatorsPresenter presenter;

    public static Fragment createInstance(String programUid, String teiUid) {
        instance = new IndicatorsFragment();
        instance.programUid = programUid;
        instance.teiUid = teiUid;

        return instance;
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        ((Components) context.getApplicationContext()).userComponent()
                .plus(new IndicatorsModule(teiUid, programUid)).inject(this);
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
        destroyInstance();
        super.onDestroy();
    }

    private static void destroyInstance() {
        instance = null;
    }

    public Consumer<List<Trio<ProgramIndicatorModel, String, String>>> swapIndicators() {
        return indicators -> adapter.setIndicators(indicators);
    }

    public void addIndicator(Trio<ProgramIndicatorModel, String, String> indicator) {
        if (getActivity() != null && isAdded()) {
            getActivity().runOnUiThread(() -> adapter.addIndicator(indicator));
        }
    }
}