package org.dhis2.uicomponents.map.views

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.mapbox.geojson.Feature
import org.dhis2.uicomponents.map.camera.centerCameraOnFeature
import org.dhis2.uicomponents.map.camera.centerCameraOnFeatures
import org.dhis2.uicomponents.map.carousel.CarouselAdapter
import org.dhis2.uicomponents.map.carousel.CarouselLayoutManager
import org.dhis2.uicomponents.map.carousel.CarouselTeiHolder
import org.dhis2.uicomponents.map.managers.MapManager
import kotlin.math.abs

class CarouselView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    var carouselEnabled: Boolean = true
    private lateinit var carouselAdapter: CarouselAdapter
    private val CAROUSEL_SMOOTH_THREADSHOLD = 10
    private val CAROUSEL_PROXIMITY_THREADSHOLD = 3
    private lateinit var mapManager: MapManager
    private lateinit var callback: (feature: Feature?, found: Boolean) -> Boolean

    init {
        layoutManager = CarouselLayoutManager(context, HORIZONTAL, false)
        itemAnimator = DefaultItemAnimator()
        LinearSnapHelper().attachToRecyclerView(this)
    }

    fun setAdapter(adapter: CarouselAdapter) {
        super.setAdapter(adapter)
        this.carouselAdapter = adapter
    }

    fun attachToMapManager(
        manager: MapManager
    ) {
        mapManager = manager
        if (::callback.isInitialized) {
            addOnScrollListener(object : OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (
                        newState == SCROLL_STATE_DRAGGING ||
                        newState == SCROLL_STATE_SETTLING &&
                        carouselEnabled
                    ) {
                        callback.invoke(null, false)
                    }
                    if (newState == SCROLL_STATE_IDLE && carouselEnabled) {
                        mapManager.requestMapLayerManager()?.selectFeature(null)
                        val features = mapManager.findFeatures(currentItem())
                        if (features != null && features.isNotEmpty() && features.size > 1) {
                            mapManager.map?.centerCameraOnFeatures(features)
                        } else {
                            val feature = mapManager.findFeature(currentItem())
                            if (feature == null) {
                                callback.invoke(feature, false)
                            } else {
                                mapManager.map?.centerCameraOnFeature(feature)
                                callback.invoke(feature, true)
                            }
                        }
                    }
                }
            })
        } else {
            throw IllegalArgumentException("Callback cannot be null, set it with .setCallback()")
        }
    }

    fun selectFirstItem() {
        if (carouselAdapter.itemCount > 0) {
            val feature = mapManager.findFeature(currentItem())
            if (feature != null) {
                mapManager.mapLayerManager.selectFeature(feature)
                callback.invoke(feature, true)
            } else {
                callback.invoke(feature, false)
            }
        }
    }

    private fun getVisiblePosition(): Int {
        var visiblePosition =
            (layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
        if (visiblePosition == -1) {
            visiblePosition = (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        }
        if (visiblePosition == -1) {
            visiblePosition = 0
        }
        return visiblePosition
    }

    fun currentItem(): String {
        return carouselAdapter.getUidProperty(getVisiblePosition())
    }

    fun scrollToFeature(feature: Feature) {
        if (carouselEnabled) {
            val initialPosition = (layoutManager as LinearLayoutManager)
                .findFirstCompletelyVisibleItemPosition()
            val endPosition = carouselAdapter.indexOfFeature(feature)

            if (initialPosition == -1 || endPosition == -1) {
                return
            }

            when {
                abs(endPosition - initialPosition) < CAROUSEL_SMOOTH_THREADSHOLD ->
                    smoothScrollToPosition(endPosition)
                endPosition > initialPosition -> {
                    smoothScrollToPosition(initialPosition + CAROUSEL_PROXIMITY_THREADSHOLD)
                    scrollToPosition(endPosition - CAROUSEL_PROXIMITY_THREADSHOLD)
                    smoothScrollToPosition(endPosition)
                }
                else -> {
                    smoothScrollToPosition(initialPosition - CAROUSEL_PROXIMITY_THREADSHOLD)
                    scrollToPosition(endPosition + CAROUSEL_PROXIMITY_THREADSHOLD)
                    smoothScrollToPosition(endPosition)
                }
            }
        }
    }

    fun setEnabledStatus(enabled: Boolean) {
        this.carouselEnabled = enabled
        (layoutManager as CarouselLayoutManager).setEnabled(enabled)
    }

    fun showNavigateTo() {
        val holder = findViewHolderForAdapterPosition(getVisiblePosition())
        (holder as CarouselTeiHolder).showNavigateButton()
    }

    fun hideNavigateTo() {
        val holder = findViewHolderForAdapterPosition(getVisiblePosition())
        (holder as CarouselTeiHolder).hideNavigateButton()
    }

    fun setCallback(callback: (feature: Feature?, found: Boolean) -> Boolean) {
        this.callback = callback
    }
}
