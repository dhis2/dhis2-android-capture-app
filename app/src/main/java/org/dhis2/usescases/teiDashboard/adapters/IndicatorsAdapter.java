package org.dhis2.usescases.teiDashboard.adapters;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import org.dhis2.R;
import org.dhis2.data.tuples.Trio;
import org.dhis2.databinding.ItemIndicatorBinding;

import org.hisp.dhis.android.core.program.ProgramIndicatorModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ppajuelo on 29/11/2017.
 */

public class IndicatorsAdapter extends RecyclerView.Adapter<IndicatorViewHolder> {

    private List<Trio<ProgramIndicatorModel, String, String>> programIndicators;

    public IndicatorsAdapter() {
        programIndicators = new ArrayList<>();
    }

    @Override
    public IndicatorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemIndicatorBinding binding = DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.item_indicator, parent, false);
        return new IndicatorViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(IndicatorViewHolder holder, int position) {
        holder.bind(programIndicators.get(position));
    }

    @Override
    public int getItemCount() {
        return programIndicators != null ? programIndicators.size() : 0;
    }

    public void setIndicators(List<Trio<ProgramIndicatorModel, String, String>> indicators) {
        this.programIndicators = indicators;
        notifyDataSetChanged();
    }
}