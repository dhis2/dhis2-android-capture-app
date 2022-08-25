package org.dhis2.android.rtsm.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.data.TransactionType
import org.dhis2.android.rtsm.data.persistence.UserActivity
import org.dhis2.android.rtsm.databinding.ListItemRecentActivityBinding
import org.dhis2.android.rtsm.utils.DateUtils

class ViewHolder private constructor(val binding: ListItemRecentActivityBinding):
    RecyclerView.ViewHolder(binding.root) {
    fun bindTo(item: UserActivity) {
        val transactionMessageResource = when (item.type) {
            TransactionType.DISTRIBUTION -> {
                R.string.distribution
            }
            TransactionType.CORRECTION -> {
                R.string.correction
            }
            else -> {
                R.string.discard
            }
        }

        binding.raTransactionTypeTextView.text = binding.root.context.getText(transactionMessageResource)
        binding.raCreationDateTextView.text = item.date.format(DateUtils.getDateTimePattern())

        if(item.type == TransactionType.DISTRIBUTION) {
            binding.raDistributedToTextView.text = item.distributedTo ?: ""
            binding.raDirectionalArrowImageView.visibility = View.VISIBLE
        }

        when (item.type) {
            TransactionType.DISTRIBUTION -> {
                binding.raDistributedToTextView.text = item.distributedTo ?: ""
                binding.raDirectionalArrowImageView.visibility = View.VISIBLE
            } else -> {
                binding.raDirectionalArrowImageView.visibility = View.GONE
            }
        }
    }

    companion object {
        fun from(parent: ViewGroup): ViewHolder {
            val view = ListItemRecentActivityBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

            return ViewHolder(view)
        }
    }
}

class RecentActivityAdapter: ListAdapter<UserActivity, ViewHolder> (DIFF_CALLBACK) {
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<UserActivity>() {
            override fun areItemsTheSame(oldItem: UserActivity, newItem: UserActivity) = oldItem == newItem

            override fun areContentsTheSame(oldItem: UserActivity, newItem: UserActivity) =
                oldItem.type == newItem.type && oldItem.distributedTo == newItem.distributedTo &&
                        oldItem.date == newItem.date
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, position: Int) = ViewHolder.from(parent)

    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = getItem(position)
        viewHolder.bindTo(item)
    }
}