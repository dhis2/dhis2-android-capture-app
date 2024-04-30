package org.dhis2.maps.carousel

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

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
}
