package org.dhis2.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.R

class CustomItemDecoration(context: Context) : RecyclerView.ItemDecoration()
{

    private val mDivider: Drawable = context.resources.getDrawable(R.drawable.cell_line_divider) // this is a shape   that i want to use

    override fun onDrawOver(c: Canvas,
                            parent: RecyclerView,
                            state: RecyclerView.State)
    {
        val left = parent.paddingLeft // change to how much padding u want to add here .
        val right = parent.width - parent.paddingRight

        val childCount = parent.childCount
        for (i in 0 until childCount)
        {
            val child = parent.getChildAt(i)

            val params = child.layoutParams as RecyclerView.LayoutParams

            val top = child.bottom + params.bottomMargin
            val bottom = top + mDivider.intrinsicHeight

            mDivider.setBounds(left, top, right, bottom)
            mDivider.draw(c)
        }
    }
}
