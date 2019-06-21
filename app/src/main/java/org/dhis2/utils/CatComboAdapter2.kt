package org.dhis2.utils

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import org.dhis2.R
import org.dhis2.databinding.SpinnerLayoutBinding
import org.dhis2.databinding.SpinnerTitleLayoutBinding
import org.hisp.dhis.android.core.category.CategoryOptionComboEntityDIModule
import org.hisp.dhis.android.core.category.CategoryOptionComboModel

class CatComboAdapter2(
        val ctx: Context,
        val resource: Int,
        val textViewResourceId: Int,
        val objects: List<CategoryOptionComboModel>,
        val categoryOptionName: String
): ArrayAdapter<CategoryOptionComboModel>(ctx, resource, textViewResourceId, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<SpinnerTitleLayoutBinding>(inflater, R.layout.spinner_title_layout, parent, false)
        if (position != 0)
            binding.option = objects.get(position - 1).displayName()
        binding.optionSetName = categoryOptionName
        return binding.root
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<SpinnerLayoutBinding>(inflater, R.layout.spinner_layout, parent, false)
        if (position != 0)
            binding.option = objects.get(position - 1).displayName()

        binding.optionSetName = categoryOptionName
        return binding.root
    }

    override fun getCount(): Int {
        return super.getCount() + 1
    }
}