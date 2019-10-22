package org.dhis2.usescases.orgunitselector

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.R
import org.dhis2.databinding.ItemOuTreeBinding
import org.dhis2.utils.filters.FilterManager

internal class OrgUnitSelectorHolder(private val binding: ItemOuTreeBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(node: TreeNode) {
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

        binding.checkBox.setOnCheckedChangeListener { _, b ->
            FilterManager.getInstance().addIfCan(node.content, b)
            node.isChecked = FilterManager.getInstance().exist(node.content)
        }
    }
}
