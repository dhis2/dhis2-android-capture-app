package org.dhis2.utils.filters.ou;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.databinding.ItemSelectedOuFilterBinding;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

class SelectedOUHolder extends RecyclerView.ViewHolder {
    private final ItemSelectedOuFilterBinding binding;

    public SelectedOUHolder(@NonNull ItemSelectedOuFilterBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(OrganisationUnit organisationUnit, OUFilterAdapter.OnClear clearListener) {
        binding.setItem(organisationUnit);
        binding.clear.setOnClickListener(view -> clearListener.onClear(organisationUnit));
    }
}
