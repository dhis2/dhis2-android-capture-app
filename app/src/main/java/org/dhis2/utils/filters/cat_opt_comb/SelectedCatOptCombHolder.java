package org.dhis2.utils.filters.cat_opt_comb;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.databinding.ItemSelectedOuFilterBinding;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;

class SelectedCatOptCombHolder extends RecyclerView.ViewHolder {
    private final ItemSelectedOuFilterBinding binding;

    public SelectedCatOptCombHolder(@NonNull ItemSelectedOuFilterBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(CategoryOptionCombo catOptCombo, CatOptCombFilterAdapter.OnClear clearListener) {
        binding.setItem(catOptCombo);
        binding.clear.setOnClickListener(view -> clearListener.onClear(catOptCombo));
    }
}
