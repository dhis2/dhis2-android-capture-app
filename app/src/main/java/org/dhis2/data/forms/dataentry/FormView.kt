package org.dhis2.data.forms.dataentry

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.RelativeLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.Bindings.closeKeyboard
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
    var scrollCallback: ((Boolean) -> Unit)? = null

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
        recyclerView.adapter = adapter

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            recyclerView.setOnScrollChangeListener { _, _, _, _, _ ->
                val hasToShowFab = checkLastItem()
                scrollCallback?.invoke(hasToShowFab)
            }
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
}
