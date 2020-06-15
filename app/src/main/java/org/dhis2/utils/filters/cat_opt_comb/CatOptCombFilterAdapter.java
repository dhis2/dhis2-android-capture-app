package org.dhis2.utils.filters.cat_opt_comb;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.databinding.ItemSelectedOuFilterBinding;
import org.dhis2.utils.filters.FilterManager;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;

public class CatOptCombFilterAdapter extends RecyclerView.Adapter<SelectedCatOptCombHolder> {


    public CatOptCombFilterAdapter() {
    }

    @NonNull
    @Override
    public SelectedCatOptCombHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new SelectedCatOptCombHolder(ItemSelectedOuFilterBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull SelectedCatOptCombHolder holder, int position) {
        holder.bind(FilterManager.getInstance().getCatOptComboFilters().get(position), catOptComboToClear -> {
            FilterManager.getInstance().addCatOptCombo((CategoryOptionCombo) catOptComboToClear);
            notifyDataSetChanged();
        });
    }

    @Override
    public int getItemCount() {
        return FilterManager.getInstance().getCatOptComboFilters().size();
    }

    public interface OnClear {
        void onClear(BaseIdentifiableObject organisationUnitToClear);
    }
}
