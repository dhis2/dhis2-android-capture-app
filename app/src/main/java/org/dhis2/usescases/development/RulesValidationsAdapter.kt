package org.dhis2.usescases.development

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.BR

class RulesValidationsAdapter(val onItemClick: (String) -> Unit) :
    ListAdapter<RuleValidation, RuleValidationHolder>(object :
            DiffUtil.ItemCallback<RuleValidation>() {
            override fun areItemsTheSame(
                oldItem: RuleValidation,
                newItem: RuleValidation
            ): Boolean {
                return oldItem.uid() == newItem.uid()
            }

            override fun areContentsTheSame(
                oldItem: RuleValidation,
                newItem: RuleValidation
            ): Boolean {
                return oldItem == newItem
            }
        }) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RuleValidationHolder(
            DataBindingUtil.inflate(LayoutInflater.from(parent.context), viewType, parent, false)
        )

    override fun getItemViewType(position: Int): Int {
        return getItem(position).layout()
    }

    override fun onBindViewHolder(holder: RuleValidationHolder, position: Int) {
        holder.apply {
            val item = getItem(position)
            binding.setVariable(BR.ruleValidation, item)
            item.externalLink?.let {
                itemView.setOnClickListener { onItemClick(item.externalLink) }
            }
        }
    }
}

class RuleValidationHolder(val binding: ViewDataBinding) :
    RecyclerView.ViewHolder(binding.root)
