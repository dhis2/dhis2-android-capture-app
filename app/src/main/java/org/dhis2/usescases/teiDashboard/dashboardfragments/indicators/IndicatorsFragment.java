package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.dhis2.App;
import org.dhis2.R;
import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.FragmentIndicatorsBinding;
import org.dhis2.usescases.general.FragmentGlobalAbstract;
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity;
import org.hisp.dhis.android.core.program.ProgramIndicator;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import io.reactivex.functions.Consumer;


/**
 * QUADRAM. Created by ppajuelo on 29/11/2017.
 */

public class IndicatorsFragment extends FragmentGlobalAbstract implements IndicatorsContracts.View {

    @Inject
    IndicatorsContracts.Presenter presenter;
    private FragmentIndicatorsBinding binding;
    private IndicatorsAdapter adapter;


    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        TeiDashboardMobileActivity activity = (TeiDashboardMobileActivity) context;
        if (((App) context.getApplicationContext()).dashboardComponent() != null)
            ((App) context.getApplicationContext())
                    .dashboardComponent()
                    .plus(new IndicatorsModule(activity.getProgramUid(), activity.getTeiUid()))
                    .inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_indicators, container, false);
        adapter = new IndicatorsAdapter();
        binding.indicatorsRecycler.setAdapter(adapter);
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.init(this);
    }

    @Override
    public void onPause() {
        presenter.onDettach();
        super.onPause();
    }

    @Override
    public Consumer<List<Trio<ProgramIndicator, String, String>>> swapIndicators() {
        return indicators -> {
            if (adapter != null) {
                adapter.setIndicators(indicators);
            }
            if (indicators != null && !indicators.isEmpty()) {
                binding.emptyIndicators.setVisibility(View.GONE);
            } else {
                binding.emptyIndicators.setVisibility(View.VISIBLE);
            }
        };
    }
}