package org.dhis2.data.forms.dataentry

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.R
import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.dhis2.utils.Constants
import org.dhis2.utils.customviews.CustomDialog

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
    var scrollCallback: ((Boolean)->Unit)? = null

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
        recyclerView.adapter = adapter

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M){
            recyclerView.setOnScrollChangeListener({ v, scrollX, scrollY, oldScrollX, oldScrollY -> {
                checkLastItem()
            }})
        } else {
            recyclerView.setOnScrollListener(object :
                RecyclerView.OnScrollListener() {
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
    }

    fun render(items: List<FieldViewModel>) {
        val layoutManager: LinearLayoutManager = recyclerView.layoutManager as LinearLayoutManager
        val myFirstPositionIndex = layoutManager.findFirstVisibleItemPosition()
        val myFirstPositionView = layoutManager.findViewByPosition(myFirstPositionIndex)

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

    }
}

/*binding.formRecycler.addOnScrollListener(object :
    RecyclerView.OnScrollListener() {
    override fun onScrollStateChanged(
        recyclerView: RecyclerView,
        newState: Int
    ) {
        if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
            dataEntryAdapter.setLastFocusItem(null)
            val imm =
                getContext().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(recyclerView.windowToken, 0)
            binding.dummyFocusView.requestFocus()
        }
    }

    override fun onScrolled(
        recyclerView: RecyclerView,
        dx: Int,
        dy: Int
    ) {
        dataEntryHeaderHelper.checkSectionHeader(recyclerView)
    }
})

if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M){
    binding.formRecycler.setOnScrollChangeListener({ v, scrollX, scrollY, oldScrollX, oldScrollY -> checkLastItem() })
} else {
    binding.formRecycler.setOnScrollListener(object :
        RecyclerView.OnScrollListener() {
        override fun onScrolled(
            recyclerView: RecyclerView,
            dx: Int,
            dy: Int
        ) {
            checkLastItem()
        }
    })
}
}

private open fun checkLastItem() {
    val layoutManager =
        binding.formRecycler.getLayoutManager() as GridLayoutManager
    val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
    val shouldShowFab =
        lastVisiblePosition != -1 && (lastVisiblePosition == dataEntryAdapter.getItemCount() - 1 ||
            dataEntryAdapter.getItemViewType(lastVisiblePosition) == 17)
    animateFabButton(shouldShowFab)
}

private open fun animateFabButton(sectionIsVisible: Boolean) {
    binding.actionButton.animate()
        .translationX(if (sectionIsVisible) 0 else 1000)
        .setDuration(500)
        .start()
} */