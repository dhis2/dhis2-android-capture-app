package org.dhis2.data.forms.dataentry

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.dhis2.Bindings.closeKeyboard
import org.dhis2.R
import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.dhis2.data.forms.dataentry.fields.coordinate.CoordinateViewModel
import org.dhis2.data.forms.dataentry.fields.edittext.EditTextViewModel
import org.dhis2.data.forms.dataentry.fields.scan.ScanTextViewModel
import org.dhis2.uicomponents.map.views.MapSelectorActivity
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialPresenter
import org.dhis2.utils.ActivityResultObserver
import org.dhis2.utils.Constants
import org.dhis2.utils.customviews.CustomDialog
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import timber.log.Timber

class FormView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), ActivityResultObserver {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val recyclerView: RecyclerView
    private val headerContainer: RelativeLayout
    private val dataEntryHeaderHelper: DataEntryHeaderHelper
    private lateinit var adapter: DataEntryAdapter
    var scrollCallback: ((Boolean) -> Unit)? = null
    var onLocationRequest: ((String) -> Unit)? = null
    var onMapRequest: ((FeatureType, String?) -> Unit)? = null
    var onNewGeometryValue: ((String, Geometry) -> Unit)? = null
    var geometryField: String? = null

    init {
        val params = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        layoutParams = params
        val view = inflater.inflate(R.layout.view_form, this, true)
        recyclerView = view.findViewById(R.id.recyclerView)
        headerContainer = view.findViewById(R.id.headerContainer)
        dataEntryHeaderHelper = DataEntryHeaderHelper(headerContainer, recyclerView)
        recyclerView.layoutManager =
            object : LinearLayoutManager(context, VERTICAL, false) {
                override fun onInterceptFocusSearch(focused: View, direction: Int): View {
                    return focused
                }
            }
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                dataEntryHeaderHelper.checkSectionHeader(recyclerView)
            }
        })
    }

    fun init(owner: LifecycleOwner) {
        dataEntryHeaderHelper.observeHeaderChanges(owner)
        adapter = DataEntryAdapter()
        adapter.didItemShowDialog = { title, message ->
            CustomDialog(
                context,
                title,
                message ?: context.getString(R.string.empty_description),
                context.getString(R.string.action_close),
                null,
                Constants.DESCRIPTION_DIALOG,
                null
            ).show()
        }
        adapter.onNextClicked = { position ->
            val viewHolder = recyclerView.findViewHolderForLayoutPosition(position + 1)
            if (viewHolder == null) {
                try {
                    recyclerView.smoothScrollToPosition(position + 1)
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }
        adapter.onLocationRequest = { fieldUid -> onLocationRequest?.invoke(fieldUid) }

        adapter.onMapRequest = { fieldUid, featureType, initialData ->
            geometryField = fieldUid
            onMapRequest?.invoke(
                featureType,
                initialData
            )
        }
        recyclerView.adapter = adapter

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            recyclerView.setOnScrollChangeListener { _, _, _, _, _ ->
                val hasToShowFab = checkLastItem()
                scrollCallback?.invoke(hasToShowFab)
            }
        } else {
            recyclerView.setOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(
                    recyclerView: RecyclerView,
                    dx: Int,
                    dy: Int
                ) {
                    val hasToShowFab = checkLastItem()
                    scrollCallback?.invoke(hasToShowFab)
                }
            })
        }

        recyclerView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                closeKeyboard()
            }
        }
    }

    fun render(items: List<FieldViewModel>) {
        val layoutManager: LinearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
        val myFirstPositionIndex = layoutManager.findFirstVisibleItemPosition()
        val myFirstPositionView = layoutManager.findViewByPosition(myFirstPositionIndex)

        handleKeyBoardOnFocusChange(items)

        var offset = 0
        myFirstPositionView?.let {
            offset = it.top
        }

        adapter.swap(
            items,
            Runnable {
                dataEntryHeaderHelper.onItemsUpdatedCallback()
            }
        )
        layoutManager.scrollToPositionWithOffset(myFirstPositionIndex, offset)
    }

    private fun checkLastItem(): Boolean {
        val layoutManager =
            recyclerView.layoutManager as LinearLayoutManager
        val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
        return lastVisiblePosition != -1 && (
                lastVisiblePosition == adapter.itemCount - 1 ||
                        adapter.getItemViewType(lastVisiblePosition) == R.layout.form_section
                )
    }

    private fun handleKeyBoardOnFocusChange(items: List<FieldViewModel>) {
        items.firstOrNull { it.activated() }?.let {
            if (!doesItemNeedsKeyboard(it)) {
                closeKeyboard()
            }
        }
    }

    private fun doesItemNeedsKeyboard(item: FieldViewModel) = when (item) {
        is EditTextViewModel,
        is ScanTextViewModel,
        is CoordinateViewModel -> true
        else -> false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == Constants.RQ_MAP_LOCATION_VIEW && data?.extras != null) {
            val locationType =
                FeatureType.valueOf(data.getStringExtra(MapSelectorActivity.LOCATION_TYPE_EXTRA))
            val dataExtra = data.getStringExtra(MapSelectorActivity.DATA_EXTRA)
            val geometry: Geometry
            geometry = when (locationType) {
                FeatureType.POINT -> {
                    val type = object : TypeToken<List<Double?>?>() {}.type
                    GeometryHelper.createPointGeometry(
                        Gson().fromJson(dataExtra, type)
                    )
                }
                FeatureType.POLYGON -> {
                    val type = object : TypeToken<List<List<List<Double?>?>?>?>() {}.type
                    GeometryHelper.createPolygonGeometry(
                        Gson().fromJson(dataExtra, type)
                    )
                }
                else -> {
                    val type = object : TypeToken<List<List<List<List<Double?>?>?>?>?>() {}.type
                    GeometryHelper.createMultiPolygonGeometry(
                        Gson().fromJson(dataExtra, type)
                    )
                }
            }
            geometryField?.let { onNewGeometryValue?.invoke(it, geometry) }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == EventInitialPresenter.ACCESS_LOCATION_PERMISSION_REQUEST &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            geometryField?.let { onLocationRequest?.invoke(it) }
        }
    }
}
