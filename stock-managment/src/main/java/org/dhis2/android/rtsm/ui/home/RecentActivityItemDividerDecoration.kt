package org.dhis2.android.rtsm.ui.home

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView

class RecentActivityItemDividerDecoration(
    context: Context, resId: Int
): RecyclerView.ItemDecoration() {
    private val mDivider = AppCompatResources.getDrawable(context, resId)!!
    companion object {
        const val LEFT = 100
        const val LINE_STROKE = 2
        const val ITEM_SPACING = 20
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = LEFT
        val right = LEFT + LINE_STROKE

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            if (i != parent.childCount - 1) {
                parent.getChildAt(i).apply {
                    val params = this.layoutParams as RecyclerView.LayoutParams
                    val top = this.bottom + params.bottomMargin
                    val bottom = top + mDivider.intrinsicHeight + ITEM_SPACING

                    mDivider.setBounds(left, top, right, bottom)
                    mDivider.draw(c)
                }
            }
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        val spaceSize = ITEM_SPACING
        with(outRect) {
            if (parent.getChildAdapterPosition(view) == 0) {
                top = spaceSize
            }
            left = spaceSize
            right = spaceSize
            bottom = spaceSize
        }
    }
}