package org.dhis2.core.ui.tree

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.core.types.Tree

abstract class TreeAdapterBinder(val contentJavaClass: Class<*>) {
    abstract val layoutId: Int
    abstract fun provideViewHolder(itemView: View): RecyclerView.ViewHolder
    abstract fun bindView(holder: RecyclerView.ViewHolder, node: Tree<*>)
}