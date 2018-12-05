package org.dhis2.utils;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;

import org.dhis2.databinding.OrgUnitMenuSelectorItemBinding;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

/**
 * QUADRAM. Created by ppajuelo on 28/11/2018.
 */
class OrgUnitSelectorHolder extends RecyclerView.ViewHolder {

    private final OrgUnitMenuSelectorItemBinding binding;

    public OrgUnitSelectorHolder(@NonNull OrgUnitMenuSelectorItemBinding binding) {
        super(binding.getRoot());
        this.binding = binding;
    }

    public void bind(OrganisationUnitModel organisationUnitModel) {
        binding.setOrgUnit(organisationUnitModel);
    }
}
