package org.dhis2.commons.orgunitselector;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;

import org.dhis2.commons.databinding.ItemOuTreeBinding;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;

import java.util.List;

import kotlin.Unit;

class OrgUnitSelectorAdapter extends ListAdapter<TreeNode, OrgUnitSelectorHolder> {
    private final OnOrgUnitClick ouClickListener;
    private final List<String> selectedOrgUnits;

    public OrgUnitSelectorAdapter(
            OnOrgUnitClick ouClickListener,
            List<String> selectedOrgUnits
    ) {
        super(new TreeNodeCallback());
        this.ouClickListener = ouClickListener;
        this.selectedOrgUnits = selectedOrgUnits;
    }

    @NonNull
    @Override
    public OrgUnitSelectorHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOuTreeBinding binding = ItemOuTreeBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new OrgUnitSelectorHolder(binding, (organisationUnit, isChecked) -> {
            ouClickListener.onOrgUnitSelected(organisationUnit, isChecked);
            return Unit.INSTANCE;
        });
    }

    @Override
    public void onBindViewHolder(@NonNull OrgUnitSelectorHolder holder, int position) {
        TreeNode node = getItem(position);
        holder.bind(getItem(position), selectedOrgUnits.contains(node.getContent().uid()));
        holder.itemView.setOnClickListener(view -> {
                    if (getItemCount() > holder.getAdapterPosition() &&
                            holder.getAdapterPosition() >= 0)
                        ouClickListener.onOrgUnitClick(getItem(holder.getAdapterPosition()), holder.getAdapterPosition());
                }
        );
    }

    public interface OnOrgUnitClick {
        void onOrgUnitClick(TreeNode treeNode, int position);

        void onOrgUnitSelected(OrganisationUnit organisationUnit, boolean isSelected);
    }
}
