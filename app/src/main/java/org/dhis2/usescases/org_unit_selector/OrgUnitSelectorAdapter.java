package org.dhis2.usescases.org_unit_selector;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.databinding.ItemOuTreeBinding;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

import java.util.List;

class OrgUnitSelectorAdapter extends RecyclerView.Adapter<OrgUnitSelectorHolder> {
    private final List<OrganisationUnit> orgUnits;
    private final OnOrgUnitClick listener;

    public OrgUnitSelectorAdapter(List<OrganisationUnit> organisationUnits, OnOrgUnitClick ouClickListener) {
        this.orgUnits = organisationUnits;
        this.listener = ouClickListener;
    }

    @NonNull
    @Override
    public OrgUnitSelectorHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new OrgUnitSelectorHolder(ItemOuTreeBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        ));
    }

    @Override
    public void onBindViewHolder(@NonNull OrgUnitSelectorHolder holder, int position) {
        holder.bind(orgUnits.get(position));
        holder.itemView.setOnClickListener(view -> listener.onOrgUnitClick(orgUnits.get(position), position));
    }

    public void addOrgUnits(int location, List<OrganisationUnit> organisationUnits) {
        if (orgUnits.containsAll(organisationUnits)) {
            orgUnits.removeAll(organisationUnits);
            notifyItemRangeRemoved(location + 1, organisationUnits.size());
        } else {
            orgUnits.addAll(location + 1, organisationUnits);
            notifyItemRangeInserted(location + 1, organisationUnits.size());
        }
    }

    @Override
    public int getItemCount() {
        return orgUnits.size();
    }

    public interface OnOrgUnitClick {
        void onOrgUnitClick(OrganisationUnit organisationUnit, int position);
    }
}
