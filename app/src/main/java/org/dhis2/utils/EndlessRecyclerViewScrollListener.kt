package org.dhis2.utils

import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager

abstract class EndlessRecyclerViewScrollListener(): RecyclerView.OnScrollListener() {
    var visibleThreshold = 2
    var currentPage = 0
    var previousTotalItemCount = 0
    var loading = true
    var startingPageIndex = 0
    lateinit var mLayoutManager: RecyclerView.LayoutManager

    constructor(layout: LinearLayoutManager) : this() {
        this.mLayoutManager = layout
    }

    constructor(layout: GridLayoutManager) : this() {
        this.mLayoutManager = layout
        this.visibleThreshold = visibleThreshold * layout.spanCount
    }

    constructor(layout: StaggeredGridLayoutManager) : this() {
        this.mLayoutManager = layout
        this.visibleThreshold = visibleThreshold * layout.spanCount
    }


    constructor(layoutManager: RecyclerView.LayoutManager, vT: Int, initialPage: Int) : this() {
        this.mLayoutManager = layoutManager
        this.visibleThreshold = vT
        this.currentPage = initialPage
        if (layoutManager is GridLayoutManager)
            visibleThreshold *= layoutManager.spanCount
        if (layoutManager is StaggeredGridLayoutManager)
            visibleThreshold *= layoutManager.spanCount
    }

    constructor(layoutManager: RecyclerView.LayoutManager, vT: Int) : this() {
        this.mLayoutManager = layoutManager
        this.visibleThreshold = vT
        if (layoutManager is GridLayoutManager)
            visibleThreshold *= layoutManager.spanCount
        if (layoutManager is StaggeredGridLayoutManager)
            visibleThreshold *= layoutManager.spanCount
    }

    fun getLastVisibleItem(lastVisibleItemPositions: IntArray): Int {
        var maxSize = 0
        for (i in lastVisibleItemPositions.indices) {
            if (i == 0) {
                maxSize = lastVisibleItemPositions[i]
            } else if (lastVisibleItemPositions[i] > maxSize) {
                maxSize = lastVisibleItemPositions[i]
            }
        }
        return maxSize
    }

    override fun onScrolled(view: RecyclerView, dx: Int, dy: Int) {
        var lastVisibleItemPosition = 0
        val totalItemCount = mLayoutManager.itemCount

        when (mLayoutManager) {
            is StaggeredGridLayoutManager -> {
                val lastVisibleItemPositions = (mLayoutManager as StaggeredGridLayoutManager).findLastVisibleItemPositions(null)
                // get maximum element within the list
                lastVisibleItemPosition = getLastVisibleItem(lastVisibleItemPositions)
            }
            is GridLayoutManager -> lastVisibleItemPosition = (mLayoutManager as GridLayoutManager).findLastVisibleItemPosition()
            is LinearLayoutManager -> lastVisibleItemPosition = (mLayoutManager as LinearLayoutManager).findLastVisibleItemPosition()
        }

            // If the total item count is zero and the previous isn't, assume the
        // list is invalidated and should be reset back to initial state
        // If it’s still loading, we check to see if the dataset count has
        // changed, if so we conclude it has finished loading and update the current page
        // number and total item count.

        // If it isn’t currently loading, we check to see if we have breached
        // the visibleThreshold and need to reload more data.
        // If we do need to reload some more data, we execute onLoadMore to fetch the data.
        // threshold should reflect how many total columns there are too

        // If the total item count is zero and the previous isn't, assume the
        // list is invalidated and should be reset back to initial state
        if (totalItemCount < previousTotalItemCount) {
            this.currentPage = this.startingPageIndex
            this.previousTotalItemCount = totalItemCount
            if (totalItemCount == 0) {
                this.loading = true
            }
        }
        // If it’s still loading, we check to see if the dataset count has
        // changed, if so we conclude it has finished loading and update the current page
        // number and total item count.
        if (loading && totalItemCount > previousTotalItemCount) {
            loading = false
            previousTotalItemCount = totalItemCount
        }

        // If it isn’t currently loading, we check to see if we have breached
        // the visibleThreshold and need to reload more data.
        // If we do need to reload some more data, we execute onLoadMore to fetch the data.
        // threshold should reflect how many total columns there are too
        if (!loading && lastVisibleItemPosition + visibleThreshold > totalItemCount) {
            currentPage++
            onLoadMore(currentPage, totalItemCount, view)
            loading = true
        }

    }

    fun resetState() {
        this.currentPage = this.startingPageIndex
        this.previousTotalItemCount = 0
        this.loading = true
    }

    fun resetState(initialPage: Int?) {
        this.currentPage = initialPage!!
        this.previousTotalItemCount = 0
        this.loading = true
    }

    // Defines the process for actually loading more data based on page
    abstract fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView)
}