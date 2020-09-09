package org.dhis2.usescases.orgunitselector;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;

import org.dhis2.databinding.ItemOuTreeBinding;
import org.dhis2.utils.filters.FilterManager;

import java.util.ArrayList;
import java.util.List;

class OrgUnitSelectorAdapter extends ListAdapter<TreeNode, OrgUnitSelectorHolder> {
    private final OnOrgUnitClick listener;

    public OrgUnitSelectorAdapter(OnOrgUnitClick ouClickListener) {
        super(new TreeNodeCallback());
        this.listener = ouClickListener;
    }

    @NonNull
    @Override
    public OrgUnitSelectorHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        org.dhis2.databinding.ItemOuTreeBinding binding = ItemOuTreeBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new OrgUnitSelectorHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OrgUnitSelectorHolder holder, int position) {
        holder.bind(getItem(holder.getAdapterPosition()));
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
