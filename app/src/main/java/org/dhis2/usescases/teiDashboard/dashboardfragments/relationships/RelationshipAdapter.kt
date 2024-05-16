package org.dhis2.usescases.teiDashboard.dashboardfragments.relationships

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import org.dhis2.R
import org.dhis2.commons.data.RelationshipViewModel
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.databinding.ItemRelationshipBinding

class RelationshipAdapter(
    private val presenter: RelationshipPresenter,
    private val colorUtils: ColorUtils,
) :
    ListAdapter<RelationshipViewModel, RelationshipViewHolder>(object :
        DiffUtil.ItemCallback<RelationshipViewModel>() {
        override fun areItemsTheSame(
            oldItem: RelationshipViewModel,
            newItem: RelationshipViewModel,
        ): Boolean {
            return oldItem.relationship.uid() == newItem.relationship.uid()
        }

        override fun areContentsTheSame(
            oldItem: RelationshipViewModel,
            newItem: RelationshipViewModel,
        ): Boolean {
            return oldItem == newItem
        }
    }) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RelationshipViewHolder {
        val binding: ItemRelationshipBinding = DataBindingUtil.inflate(
            LayoutInflater.from(parent.context),
            R.layout.item_relationship,
            parent,
            false,
        )
        return RelationshipViewHolder(binding, colorUtils)
    }

    override fun onBindViewHolder(holder: RelationshipViewHolder, position: Int) {
        holder.bind(presenter, getItem(position))
    }
}
