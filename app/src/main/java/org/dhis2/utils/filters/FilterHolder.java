package org.dhis2.utils.filters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.databinding.Observable;
import androidx.databinding.ObservableField;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.BR;
import org.dhis2.R;

abstract class FilterHolder extends RecyclerView.ViewHolder {
    protected final View filterArrow;
    protected final ImageView filterIcon;
    protected final TextView filterTitle;
    protected final TextView filterValues;
    protected Filters filterType;
    private ObservableField<Filters> openFilter;
    protected ViewDataBinding binding;

    FilterHolder(@NonNull ViewDataBinding binding, ObservableField<Filters> openedFilter) {
        super(binding.getRoot());
        this.binding = binding;
        this.openFilter = openedFilter;
        this.filterArrow = binding.getRoot().findViewById(R.id.filterArrow);
        this.filterIcon = binding.getRoot().findViewById(R.id.filterIcon);
        this.filterTitle = binding.getRoot().findViewById(R.id.filterTitle);
        this.filterValues = binding.getRoot().findViewById(R.id.filterValues);
    }

    protected void bind() {
        binding.setVariable(BR.currentFilter, openFilter);
        binding.setVariable(BR.filterType, filterType);
        binding.setVariable(BR.filterCount, FilterManager.getInstance().observeField(filterType));
        binding.executePendingBindings();

        itemView.setOnClickListener(view ->
                openFilter.set(openFilter.get() != filterType ? filterType : null)
        );

        openFilter.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                filterArrow.animate().scaleY(openFilter.get() != filterType ? 1 : -1).setDuration(200).start();
            }
        });
    }
}
