package org.dhis2.utils

import androidx.recyclerview.widget.RecyclerView

import org.dhis2.databinding.OrgUnitMenuSelectorItemBinding
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel

/**
 * QUADRAM. Created by ppajuelo on 28/11/2018.
 */
internal class OrgUnitSelectorHolder(private val binding: OrgUnitMenuSelectorItemBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(organisationUnitModel: OrganisationUnitModel) {
        binding.orgUnit = organisationUnitModel
    }
}
