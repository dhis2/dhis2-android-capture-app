package org.dhis2.data.forms.dataentry

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.R
import org.dhis2.data.forms.dataentry.fields.edittext.CustomTextView

class ItemKeyCaptureSupport private constructor(recyclerView: RecyclerView) {

    private val recyclerview: RecyclerView = recyclerView
    private var onItemKeyListener: OnItemKeyListener? = null

//setOnEditorActionListener
    /*private val mKeyListener: View.OnClickListener = View.OnClickListener { v ->
        if (onItemKeyListener != null) {
            val holder: RecyclerView.ViewHolder = recyclerview.getChildViewHolder(v)
            onItemKeyListener!!.onItemKeyPressed(recyclerview, holder.adapterPosition, v)
        }
    }*/

    private val mAttachListener: RecyclerView.OnChildAttachStateChangeListener =
        object : RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewDetachedFromWindow(view: View) {}

            override fun onChildViewAttachedToWindow(view: View) {
                val customTextView = view.findViewById<CustomTextView>(R.id.custom_edittext)
                if (onItemKeyListener != null && customTextView != null) {
                   //  customTextView.editText.setOnEditorActionListener { v, actionId, event ->  }
                    // view.setOnClickListener(mKeyListener)
                }
            }
        }

    fun setOnItemClickListener(listener: OnItemKeyListener?): ItemKeyCaptureSupport {
        onItemKeyListener = listener
        return this
    }

    private fun detach(view: RecyclerView) {
        view.removeOnChildAttachStateChangeListener(mAttachListener)
        view.setTag(R.id.item_key_support, null)
    }

    interface OnItemKeyListener {
        fun onItemKeyPressed(recyclerView: RecyclerView?, position: Int, v: View?)
    }

    companion object {
        fun addTo(view: RecyclerView): ItemKeyCaptureSupport {
            return view.getTag(R.id.item_key_support) as ItemKeyCaptureSupport
        }
    }

    init {
        recyclerview.setTag(R.id.item_key_support, this)
        recyclerview.addOnChildAttachStateChangeListener(mAttachListener)
    }
}