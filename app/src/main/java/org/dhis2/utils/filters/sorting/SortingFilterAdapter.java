package org.dhis2.utils.filters.sorting;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.databinding.ItemSelectedSortingBinding;
import org.dhis2.utils.filters.FilterManager;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

public class SortingFilterAdapter extends RecyclerView.Adapter<SelectedSortingHolder> {


    public SortingFilterAdapter() {
    }

    @NonNull
    @Override
    public SelectedSortingHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SelectedSortingHolder(ItemSelectedSortingBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedSortingHolder holder, int position) {
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
