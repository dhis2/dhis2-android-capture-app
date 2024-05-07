package org.dhis2.maps.carousel

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CarouselLayoutManager(
    context: Context,
    horizontal: Int,
    reverseLayout: Boolean,
) : LinearLayoutManager(context, horizontal, reverseLayout) {

    private var enabled: Boolean = true

    override fun canScrollHorizontally(): Boolean {
        return enabled && super.canScrollHorizontally()
    }

    override fun canScrollVertically(): Boolean {
        return enabled && super.canScrollVertically()
    }

    fun setEnabled(enabled: Boolean) {
        this.enabled = enabled
    }

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        super.onLayoutChildren(recycler, state)
        alignItemsToBottom()
    }

    private fun alignItemsToBottom() {
        val parentBottom = height - paddingBottom
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            child?.let {
                val childBottom = it.bottom
                val offset = parentBottom - childBottom
                if (offset > 0) {
                    it.offsetTopAndBottom(offset)
                }
            }
        }
    }
}
