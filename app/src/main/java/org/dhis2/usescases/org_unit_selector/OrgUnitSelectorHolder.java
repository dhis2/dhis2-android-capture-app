package org.dhis2.usescases.org_unit_selector;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.databinding.ItemOuTreeBinding;
import org.dhis2.utils.filters.FilterManager;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

class OrgUnitSelectorHolder extends RecyclerView.ViewHolder {
    private final ItemOuTreeBinding binding;

    public OrgUnitSelectorHolder(@NonNull ItemOuTreeBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(OrganisationUnit organisationUnit) {
        binding.ouName.setText(organisationUnit.displayName());
        ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) binding.icon.getLayoutParams();

        marginParams.leftMargin += (organisationUnit.level() - 1) * 25;

        binding.checkBox.setChecked(FilterManager.getInstance().getOrgUnitFilters().contains(organisationUnit));
        binding.checkBox.setOnCheckedChangeListener((compoundButton, b) ->
                FilterManager.getInstance().addOrgUnit(organisationUnit));
    }
}
