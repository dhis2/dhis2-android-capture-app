package org.dhis2.usescases.orgunitselector

import androidx.recyclerview.widget.DiffUtil

class TreeNodeCallback(
    private val oldList: List<TreeNode>,
    private val newList: List<TreeNode>
) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].isChecked == newList[newItemPosition].isChecked &&
            oldList[oldItemPosition].isOpen == newList[newItemPosition].isOpen &&
            oldList[oldItemPosition].hasChild == newList[newItemPosition].hasChild &&
            oldList[oldItemPosition].id == newList[newItemPosition].id
    }
}
