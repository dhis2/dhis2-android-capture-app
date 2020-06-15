package org.dhis2.utils.filters.ou;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.databinding.ItemSelectedOuFilterBinding;
import org.dhis2.utils.filters.FilterManager;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

public class OUFilterAdapter extends RecyclerView.Adapter<SelectedOUHolder> {


    public OUFilterAdapter() {
    }

    @NonNull
    @Override
    public SelectedOUHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SelectedOUHolder(ItemSelectedOuFilterBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedOUHolder holder, int position) {
        holder.bind(FilterManager.getInstance().getOrgUnitFilters().get(position), organisationUnitToClear -> {
            FilterManager.getInstance().addOrgUnit(organisationUnitToClear);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return FilterManager.getInstance().getOrgUnitFilters().size();
    }

    public interface OnClear {
        void onClear(OrganisationUnit organisationUnitToClear);
    }
}
