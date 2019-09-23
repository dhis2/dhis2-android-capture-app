package org.dhis2.utils.filters;

import android.view.View;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.ObservableField;

import org.dhis2.R;
import org.dhis2.data.tuples.Pair;
import org.dhis2.databinding.ItemFilterCatOptCombBinding;
import org.dhis2.utils.CatComboAdapter;
import org.dhis2.utils.filters.cat_opt_comb.CatOptCombFilterAdapter;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;

import java.util.List;

class CatOptCombFilterHolder extends FilterHolder {

    private final Pair<CategoryCombo, List<CategoryOptionCombo>> catComboData;

    CatOptCombFilterHolder(@NonNull ItemFilterCatOptCombBinding binding, ObservableField<Filters> openedFilter, Pair<CategoryCombo, List<CategoryOptionCombo>> catCombData) {
        super(binding, openedFilter);
        filterType = Filters.CAT_OPT_COMB;
        this.catComboData = catCombData;
    }

    @Override
    public void bind() {
        super.bind();
        filterIcon.setImageDrawable(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_filter_sync));
        filterTitle.setText(catComboData.val0().displayName());
        filterValues.setText(
                FilterManager.getInstance().getStateFilters().isEmpty() ? "No filters applied" : "Filters applying"
        );
        ItemFilterCatOptCombBinding localBinding = (ItemFilterCatOptCombBinding) binding;


        CatOptCombFilterAdapter adapter = new CatOptCombFilterAdapter();
        localBinding.filterCatOptComb.catCombOptRecycler.setAdapter(adapter);

        CatComboAdapter spinnerAdapter = new CatComboAdapter(itemView.getContext(),
                R.layout.spinner_layout,
                R.id.spinner_text,
                catComboData.val1(),
                catComboData.val0().displayName(),
                R.color.white_faf);

        localBinding.filterCatOptComb.catOptCombSpinner.setAdapter(spinnerAdapter);
        localBinding.filterCatOptComb.catOptCombSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                if (position != 0) {
                    FilterManager.getInstance().addCatOptCombo(catComboData.val1().get(position - 1));
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}
