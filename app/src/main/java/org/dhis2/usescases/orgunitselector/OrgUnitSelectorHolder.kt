package org.dhis2.usescases.orgunitselector

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.R
import org.dhis2.databinding.ItemOuTreeBinding
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

internal class OrgUnitSelectorHolder(
    private val binding: ItemOuTreeBinding,
    private val checkCallback: (OrganisationUnit, Boolean) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    private lateinit var node: TreeNode

    fun bind(
        node: TreeNode,
        preselected: Boolean
    ) {
        this.node = node
        binding.checkBox.setOnCheckedChangeListener(null)

        binding.ouName.text = node.displayName()
        node.isChecked = preselected
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
            checkCallback(node.content, isChecked)
        }
    }
}
