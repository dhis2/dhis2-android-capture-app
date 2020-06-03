package org.dhis2.uicomponents.map.views

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.mapbox.geojson.Feature
import org.dhis2.uicomponents.map.camera.centerCameraOnFeature
import org.dhis2.uicomponents.map.carousel.CarouselAdapter
import org.dhis2.uicomponents.map.managers.MapManager

class CarouselView<T> @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    private var selectedFeature: Feature? = null
    private lateinit var carouselAdapter: CarouselAdapter<T>

    init {
        layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
        itemAnimator = DefaultItemAnimator()
        LinearSnapHelper().attachToRecyclerView(this)
    }

    fun setAdapter(adapter: CarouselAdapter<T>) {
        super.setAdapter(adapter)
        this.carouselAdapter = adapter
    }

    fun attachToMapManager(mapManager: MapManager, callback: () -> Boolean) {
        addOnScrollListener(object : OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_IDLE) {
                    val feature = selectedFeature ?: mapManager.findFeatureFor(currentItem())
                    mapManager.mapLayerManager.selectFeature(feature)
                    if (feature == null) {
                        callback.invoke()
                    } else {
                        mapManager.map.centerCameraOnFeature(feature)
                    }
                    selectedFeature == null
                }
            }
        })
    }

    fun currentItem(): String {
        var visiblePosition =
            (layoutManager as LinearLayoutManager).findFirstCompletelyVisibleItemPosition()
        if (visiblePosition == -1) {
            visiblePosition = (layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
        }
        if (visiblePosition == -1) {
            visiblePosition == 0
        }
        return carouselAdapter.getUidProperty(visiblePosition)
    }

    fun scrollToFeature(feature: Feature) {
        selectedFeature = feature
        smoothScrollToPosition(
            carouselAdapter.indexOfFeature(feature)
        )
    }
}
