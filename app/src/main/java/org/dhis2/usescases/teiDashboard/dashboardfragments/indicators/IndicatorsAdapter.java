package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.R;
import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.ItemIndicatorBinding;
import org.hisp.dhis.android.core.program.ProgramIndicator;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * QUADRAM. Created by ppajuelo on 29/11/2017.
 */

public class IndicatorsAdapter extends RecyclerView.Adapter<IndicatorViewHolder> {

    private List<Trio<ProgramIndicator, String, String>> programIndicators;

    IndicatorsAdapter() {
        programIndicators = new ArrayList<>();
    }

    @NotNull
    @Override
    public IndicatorViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        ItemIndicatorBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_indicator, parent, false);
        return new IndicatorViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NotNull IndicatorViewHolder holder, int position) {
        holder.bind(programIndicators.get(position));
    }

    @Override
    public int getItemCount() {
        return programIndicators != null ? programIndicators.size() : 0;
    }

    public void setIndicators(List<Trio<ProgramIndicator, String, String>> indicators) {
        this.programIndicators.clear();
        this.programIndicators.addAll(indicators);
        notifyDataSetChanged();
    }

    public void addIndicator(Trio<ProgramIndicator, String, String> indicator) {
        programIndicators.add(indicator);
        notifyItemInserted(programIndicators.size() - 1);
    }
}