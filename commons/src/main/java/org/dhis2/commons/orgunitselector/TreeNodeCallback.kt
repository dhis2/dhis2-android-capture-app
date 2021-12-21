package org.dhis2.commons.orgunitselector

import androidx.recyclerview.widget.DiffUtil

class TreeNodeCallback() : DiffUtil.ItemCallback<TreeNode>() {
    override fun areItemsTheSame(oldItem: TreeNode, newItem: TreeNode): Boolean {
        return oldItem.content.uid() == newItem.content.uid()
    }

    override fun areContentsTheSame(oldItem: TreeNode, newItem: TreeNode): Boolean {
        return oldItem == newItem
    }
}
