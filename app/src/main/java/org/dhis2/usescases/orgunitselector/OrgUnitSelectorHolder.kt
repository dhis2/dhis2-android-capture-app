package org.dhis2.usescases.orgunitselector

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.R
import org.dhis2.commons.filters.FilterManager
import org.dhis2.databinding.ItemOuTreeBinding

internal class OrgUnitSelectorHolder(private val binding: ItemOuTreeBinding) :
    RecyclerView.ViewHolder(binding.root) {

    private lateinit var node: TreeNode

    fun bind(
        node: TreeNode
    ) {
        this.node = node
        binding.checkBox.setOnCheckedChangeListener(null)

        binding.ouName.text = node.content.displayName()
        node.isChecked = FilterManager.getInstance().exist(node.content)
        val marginParams = binding.root.layoutParams as ViewGroup.MarginLayoutParams
        marginParams.leftMargin = (node.level - 1) * 40
        binding.checkBox.isChecked = node.isChecked

        if (!node.hasChild) {
            binding.icon.setImageResource(R.drawable.ic_circle_primary)
        } else if (node.isOpen) {
            binding.icon.setImageResource(R.drawable.ic_remove_circle_primary)
        } else {
            binding.icon.setImageResource(R.drawable.ic_add_circle)
        }

        binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
            FilterManager.getInstance().addIfCan(node.content, isChecked)
        }
    }
}
