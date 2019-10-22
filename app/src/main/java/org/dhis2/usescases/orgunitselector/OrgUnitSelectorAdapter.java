package org.dhis2.usescases.orgunitselector;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import org.dhis2.databinding.ItemOuTreeBinding;
import org.dhis2.utils.filters.FilterManager;

import java.util.ArrayList;
import java.util.List;

class OrgUnitSelectorAdapter extends RecyclerView.Adapter<OrgUnitSelectorHolder> {
    private List<TreeNode> treeNodes;
    private final OnOrgUnitClick listener;

    public OrgUnitSelectorAdapter(List<TreeNode> organisationUnits, OnOrgUnitClick ouClickListener) {
        this.treeNodes = organisationUnits;
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
        holder.bind(treeNodes.get(holder.getAdapterPosition()));
        holder.itemView.setOnClickListener(view -> {
                    if (treeNodes.size() > holder.getAdapterPosition() &&
                    holder.getAdapterPosition() >= 0)
                        listener.onOrgUnitClick(treeNodes.get(holder.getAdapterPosition()), holder.getAdapterPosition());
                }
        );
    }

    public void addOrgUnits(int location, List<TreeNode> nodes) {
        List<TreeNode> nodesCopy = new ArrayList<>(treeNodes);
        nodesCopy.get(location).setOpen(!nodesCopy.get(location).isOpen());
        notifyItemChanged(location);
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

        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new TreeNodeCallback(treeNodes, nodesCopy));
        diffResult.dispatchUpdatesTo(this);
        treeNodes.clear();
        treeNodes.addAll(nodesCopy);
    }

    @Override
    public int getItemCount() {
        return treeNodes.size();
    }

    public void clearAll() {
        FilterManager.getInstance().removeAll();
        for (TreeNode treeNode:treeNodes) {
            treeNode.setChecked(false);
        }
        notifyDataSetChanged();
    }

    public interface OnOrgUnitClick {
        void onOrgUnitClick(TreeNode treeNode, int position);
    }
}
