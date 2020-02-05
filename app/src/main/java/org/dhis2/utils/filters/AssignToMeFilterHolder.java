package org.dhis2.utils.filters;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.databinding.ObservableField;

import org.dhis2.R;
import org.dhis2.databinding.ItemFilterAssignedBinding;

class AssignToMeFilterHolder extends FilterHolder {

    AssignToMeFilterHolder(@NonNull ItemFilterAssignedBinding binding, ObservableField<Filters> openedFilter) {
        super(binding, openedFilter);
        filterType = Filters.ASSIGNED_TO_ME;
        filterArrow.setVisibility(View.GONE);
    }

    @Override
    public void bind() {
        super.bind();
        ItemFilterAssignedBinding mBinding = (ItemFilterAssignedBinding) binding;
        filterIcon.setImageDrawable(AppCompatResources.getDrawable(itemView.getContext(), R.drawable.ic_assignment));
        filterTitle.setText(R.string.filters_title_assigned);

        mBinding.filterSwitch.setChecked(
                FilterManager.getInstance().getAssignedFilter()
        );

        mBinding.filterSwitch.setOnCheckedChangeListener((compoundButton, isCheked) ->
                FilterManager.getInstance().setAssignedToMe(isCheked));
    }
}
