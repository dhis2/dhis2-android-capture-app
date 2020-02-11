package org.dhis2.utils

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager

class Dhis2LinearLayoutManager(context: Context) : LinearLayoutManager(context) {
    private var isScrollEnabled: Boolean = true

    fun setScrollEnabled(enableScrolling: Boolean) {
        isScrollEnabled = enableScrolling
    }

    override fun canScrollHorizontally(): Boolean {
        return isScrollEnabled && super.canScrollHorizontally()
    }

    override fun canScrollVertically(): Boolean {
        return isScrollEnabled && super.canScrollVertically()
    }
}