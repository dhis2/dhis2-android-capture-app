package org.dhis2.android.rtsm.ui.filter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.annotation.LayoutRes
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.ProgramStage

class DropdownFilterAdapter<T>(
    context: Context,
    @LayoutRes layout: Int,
    filterItems: MutableList<T>
): ArrayAdapter<T>(
    context,
    layout
) {
    private val items: MutableList<T> = mutableListOf()

    init {
        items.addAll(filterItems)
    }

    override fun add(`object`: T?) {
        `object`?.let {
            items.add(it)
        }
        notifyDataSetChanged()
    }

    override fun addAll(collection: MutableCollection<out T>) {
        collection.let {
            items.addAll(it)
        }
        notifyDataSetChanged()
    }

    override fun clear() {
        items.clear()
    }

    override fun getCount() = items.size

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return (super.getDropDownView(position, convertView, parent) as TextView).apply {
            when(val item = items[position]) {
                is OrganisationUnit -> text = item.displayName().toString()
                is ProgramStage -> text = item.displayName().toString()
                is Option -> text = item.displayName().toString()
                is String -> text = item.toString()
                else -> item.toString()
            }
        }
    }

    override fun getItem(position: Int) = items[position]

    override fun getPosition(item: T?) = items.indexOf(item)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return (super.getDropDownView(position, convertView, parent) as TextView).apply {
            when(val item = items[position]) {
                is OrganisationUnit -> text = item.displayName().toString()
                is ProgramStage -> text = item.displayName().toString()
                is Option -> text = item.displayName().toString()
                is String -> text = item.toString()
                else -> item.toString()
            }
        }
    }
}