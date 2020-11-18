package org.dhis2.data.forms.dataentry

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.Flowable
import org.dhis2.R
import org.dhis2.data.forms.dataentry.fields.FieldViewModel

class FormView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val recyclerView: RecyclerView
    private val headerContainer: RelativeLayout
    private val dataEntryHeaderHelper: DataEntryHeaderHelper
    private lateinit var adapter: DataEntryAdapter

    init {
        val params = LayoutParams(MATCH_PARENT, MATCH_PARENT)
        layoutParams = params
        val view = inflater.inflate(R.layout.view_form, this, true)
        recyclerView = view.findViewById(R.id.recyclerView)
        headerContainer = view.findViewById(R.id.headerContainer)
        dataEntryHeaderHelper = DataEntryHeaderHelper(headerContainer, recyclerView)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                dataEntryHeaderHelper.checkSectionHeader(recyclerView)
            }
        })
    }

    fun init(
        dataEntryArguments: DataEntryArguments,
        owner: LifecycleOwner
    ) {
        dataEntryHeaderHelper.observeHeaderChanges(owner)
        adapter = DataEntryAdapter(dataEntryArguments)
        recyclerView.adapter = adapter
    }

    fun render(items: List<FieldViewModel>) {
        val layoutManager: LinearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
        val myFirstPositionIndex = layoutManager.findFirstVisibleItemPosition()
        val myFirstPositionView = layoutManager.findViewByPosition(myFirstPositionIndex)

        var offset = 0
        myFirstPositionView?.let {
            offset = it.top
        }

        adapter.swap(items) {
            dataEntryHeaderHelper.onItemsUpdatedCallback()
        }
        layoutManager.scrollToPositionWithOffset(myFirstPositionIndex, offset)
    }

    // TODO methods to remove

    fun sectionFlowable(): Flowable<String> {
        return adapter.sectionFlowable()
    }

    fun setCurrentSection(selectedSection: String) {
        adapter.setCurrentSection(selectedSection)
    }

    fun setLastFocusItem(lastFocusItem: String?) {
        adapter.setLastFocusItem(lastFocusItem)
    }
}
