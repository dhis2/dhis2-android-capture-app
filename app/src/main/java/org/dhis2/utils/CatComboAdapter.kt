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
import org.hisp.dhis.android.core.category.CategoryOptionComboModel

class CatComboAdapter(
        ctx: Context,
        val resource: Int,
        textViewResourceId: Int,
        val objects: MutableList<CategoryOptionComboModel>,
        private val categoryOptionName: String,
        val textColor: Int
): ArrayAdapter<CategoryOptionComboModel>(ctx, resource, textViewResourceId, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(parent.context)
        val binding: SpinnerLayoutBinding = DataBindingUtil.inflate(inflater, R.layout.spinner_layout, parent, false);
        if (position != 0) {
            binding.option = objects[position - 1].displayName()
        }
        binding.optionSetName = categoryOptionName
        binding.spinnerText.setTextColor(ContextCompat.getColor(binding.spinnerText.context, textColor))
        binding.executePendingBindings()
        return binding.root
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(parent.context)
        val binding: SpinnerLayoutBinding = DataBindingUtil.inflate(inflater, R.layout.spinner_layout, parent, false);
        if (position != 0) {
            binding.option = objects[position - 1].displayName()
        }
        binding.optionSetName = categoryOptionName
        binding.executePendingBindings()
        return binding.root
    }

    override fun getCount(): Int {
        return super.getCount() + 1
    }
}