package org.dhis2.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import org.dhis2.R
import org.dhis2.databinding.SpinnerLayoutBinding
import org.hisp.dhis.android.core.category.CategoryOption

class CategoryOptionAdapter(
        val ctx: Context,
        val resource: Int,
        val textViewResourceId: Int,
        val objects: List<CategoryOption>,
        val categoryName: String,
        val textColor: Int
        ): ArrayAdapter<String>(ctx, resource, textViewResourceId, transformToStringArray(objects)) {

    companion object {
        fun transformToStringArray(categoryOptions: List<CategoryOption>): List<String> {
            return categoryOptions.map {
                it.displayName() ?: ""
            }
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<SpinnerLayoutBinding>(inflater, R.layout.spinner_layout, parent, false)
        if (position != 0)
            binding.option = objects.get(position - 1).displayName()
        binding.optionSetName = categoryName
        binding.spinnerText.setTextColor(ContextCompat.getColor(binding.spinnerText.context, textColor))
        return binding.root
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<SpinnerLayoutBinding>(inflater, R.layout.spinner_layout, parent, false)
        if (position != 0)
            binding.option = objects.get(position - 1).displayName()

        binding.optionSetName = categoryName
        return binding.root
    }

    override fun getCount(): Int {
        return super.getCount() +1
    }

    fun getSelectedOption(position: Int): CategoryOption {
        return objects[position]
    }
}