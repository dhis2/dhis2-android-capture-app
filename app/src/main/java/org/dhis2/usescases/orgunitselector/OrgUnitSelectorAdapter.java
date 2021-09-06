package org.dhis2.usescases.orgunitselector;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;

import org.dhis2.databinding.ItemOuTreeBinding;
import org.dhis2.commons.filters.FilterManager;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;

class OrgUnitSelectorAdapter extends ListAdapter<TreeNode, OrgUnitSelectorHolder> {
    private final OnOrgUnitClick listener;
    private final List<String> selectedOrgUnits;

    public OrgUnitSelectorAdapter(OnOrgUnitClick ouClickListener, List<String> selectedOrgUnits) {
        super(new TreeNodeCallback());
        this.listener = ouClickListener;
        this.selectedOrgUnits = selectedOrgUnits;
    }

    @NonNull
    @Override
    public OrgUnitSelectorHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        org.dhis2.databinding.ItemOuTreeBinding binding = ItemOuTreeBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new OrgUnitSelectorHolder(binding, (organisationUnit, isChecked) -> {
            if (isChecked && !selectedOrgUnits.contains(organisationUnit.uid())) {
                selectedOrgUnits.add(organisationUnit.uid());
            } else if (!isChecked && selectedOrgUnits.contains(organisationUnit.uid())) {
                selectedOrgUnits.remove(organisationUnit.uid());
            }
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
                        listener.onOrgUnitClick(getItem(holder.getAdapterPosition()), holder.getAdapterPosition());
                }
        );
    }

    public void addOrgUnits(int location, List<TreeNode> nodes) {
        List<TreeNode> nodesCopy = new ArrayList<>(getCurrentList());
        nodesCopy.get(location).setOpen(!nodesCopy.get(location).isOpen());

        if (!nodesCopy.get(location).isOpen()) {
            TreeNode parent = nodesCopy.get(location);
            List<TreeNode> deleteList = new ArrayList<>();
            boolean sameLevel = true;
            for (int i = location + 1; i < nodesCopy.size(); i++) {
                if (sameLevel)
                    if (nodesCopy.get(i).getLevel() > parent.getLevel()) {
                        deleteList.add(nodesCopy.get(i));
                    } else {
                        sameLevel = false;
                    }
            }
            nodesCopy.removeAll(deleteList);
        } else {
            nodesCopy.addAll(location + 1, nodes);
        }

        submitList(nodesCopy);
    }

    public void clearAll() {
        FilterManager.getInstance().removeAll();
        for (int i = 0; i < getItemCount(); i++) {
            TreeNode treeNode = getItem(i);
            treeNode.setChecked(false);
        }
        notifyDataSetChanged();
    }

    public interface OnOrgUnitClick {
        void onOrgUnitClick(TreeNode treeNode, int position);
    }
}
