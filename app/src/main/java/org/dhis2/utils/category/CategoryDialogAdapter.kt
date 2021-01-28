package org.dhis2.utils.category

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import org.dhis2.R
import org.dhis2.databinding.ItemOptionBinding

class CategoryDialogAdapter(private val clickListener: (CategoryDialogItem) -> Unit) :
    PagedListAdapter<CategoryDialogItem, CategoryDialogHolder>(object :
            DiffUtil.ItemCallback<CategoryDialogItem>() {
            override fun areItemsTheSame(
                oldItem: CategoryDialogItem,
                newItem: CategoryDialogItem
            ): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(
                oldItem: CategoryDialogItem,
                newItem: CategoryDialogItem
            ): Boolean {
                return oldItem == newItem
            }
        }) {

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): CategoryDialogHolder {
        val binding =
            DataBindingUtil.inflate<ItemOptionBinding>(
                LayoutInflater.from(viewGroup.context),
                R.layout.item_option,
                viewGroup,
                false
            )
        return CategoryDialogHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryDialogHolder, position: Int) {
        holder.bind(getItem(position)!!, clickListener)
    }
}
