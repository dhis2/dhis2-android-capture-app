package org.dhis2.utils.filters.sorting;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.databinding.ItemSelectedSortingBinding;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

class SelectedSortingHolder extends RecyclerView.ViewHolder {
    private final ItemSelectedSortingBinding binding;

    public SelectedSortingHolder(@NonNull ItemSelectedSortingBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(OrganisationUnit organisationUnit, SortingFilterAdapter.OnClear clearListener) {
        binding.clear.setOnClickListener(view -> clearListener.onClear(organisationUnit));
    }
}
