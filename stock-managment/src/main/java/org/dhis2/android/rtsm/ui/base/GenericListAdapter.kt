package org.dhis2.android.rtsm.ui.base

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.TextView
import org.hisp.dhis.android.core.common.BaseIdentifiableObject

class GenericListAdapter<T: BaseIdentifiableObject>(context: Context,
                                                    private val layoutResource: Int,
                                                    options: MutableList<T>
) : ArrayAdapter<T>(context, layoutResource, options) {
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView.let { convertView } ?:
        inflater.inflate(layoutResource, parent, false)

        val textView: TextView = view as TextView
        val objModel: T? = getItem(position)
        if (objModel != null) {
            textView.text = objModel.displayName()
        }

        return view
    }

    private val modelFilter: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()

            val queryString = constraint?.toString()?.lowercase()?.trim()
            val suggestions = if (queryString == null || queryString.isEmpty())
                options
            else
                options.filter {
                    it.displayName()?.lowercase()?.contains(queryString) ?: false
                }

            results.values = suggestions
            results.count = suggestions.size

            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            if (results.count > 0) {
                notifyDataSetChanged()
            } else {
                notifyDataSetInvalidated()
            }
        }

        override fun convertResultToString(resultValue: Any): CharSequence {
            @Suppress("UNCHECKED_CAST")
            val objName = (resultValue as T).displayName()
            return objName?.subSequence(0, objName.length) ?:
            super.convertResultToString(resultValue)
        }
    }

    override fun getFilter(): Filter {
        return modelFilter
    }
}