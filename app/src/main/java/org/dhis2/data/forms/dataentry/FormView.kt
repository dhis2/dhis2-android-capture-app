package org.dhis2.data.forms.dataentry

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.dhis2.R
import org.dhis2.data.forms.dataentry.fields.coordinate.CoordinateViewModel
import org.dhis2.data.forms.dataentry.fields.edittext.EditTextViewModel
import org.dhis2.data.forms.dataentry.fields.scan.ScanTextViewModel
import org.dhis2.databinding.ViewFormBinding
import org.dhis2.form.model.FieldUiModel
import org.dhis2.utils.Constants
import org.dhis2.utils.customviews.CustomDialog
import timber.log.Timber

class FormView : Fragment() {

    private lateinit var binding: ViewFormBinding
    private lateinit var dataEntryHeaderHelper: DataEntryHeaderHelper
    private lateinit var adapter: DataEntryAdapter
    var scrollCallback: ((Boolean) -> Unit)? = null
    var needToForceUpdate: Boolean? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.view_form, container, false)
        binding.lifecycleOwner = this
        dataEntryHeaderHelper = DataEntryHeaderHelper(binding.headerContainer, binding.recyclerView)
        binding.recyclerView.background = this.background
        binding.recyclerView.layoutManager =
            object : LinearLayoutManager(context, VERTICAL, false) {
                override fun onInterceptFocusSearch(focused: View, direction: Int): View {
                    return focused
                }
            }
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                dataEntryHeaderHelper.checkSectionHeader(recyclerView)
            }
        })
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dataEntryHeaderHelper.observeHeaderChanges(viewLifecycleOwner)
        adapter = DataEntryAdapter()
        adapter.didItemShowDialog = { title, message ->
            CustomDialog(
                requireContext(),
                title,
                message ?: requireContext().getString(R.string.empty_description),
                requireContext().getString(R.string.action_close),
                null,
                Constants.DESCRIPTION_DIALOG,
                null
            ).show()
        }
        adapter.onNextClicked = { position ->
            val viewHolder = binding.recyclerView.findViewHolderForLayoutPosition(position + 1)
            if (viewHolder == null) {
                try {
                    binding.recyclerView.smoothScrollToPosition(position + 1)
                } catch (e: Exception) {
                    Timber.e(e)
                }
            }
        }
        binding.recyclerView.adapter = adapter

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            binding.recyclerView.setOnScrollChangeListener { _, _, _, _, _ ->
                val hasToShowFab = checkLastItem()
                scrollCallback?.invoke(hasToShowFab)
            }
        } else {
            binding.recyclerView.setOnScrollListener(object : RecyclerView.OnScrollListener() {
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

        binding.recyclerView.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                closeKeyboard()
            }
        }
    }

    fun render(items: List<FieldUiModel>) {
        val layoutManager: LinearLayoutManager =
            binding.recyclerView.layoutManager as LinearLayoutManager
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
                when (needToForceUpdate) {
                    true -> adapter.notifyDataSetChanged()
                    else -> dataEntryHeaderHelper.onItemsUpdatedCallback()
                }
            }
        )
        layoutManager.scrollToPositionWithOffset(myFirstPositionIndex, offset)
    }

    private fun checkLastItem(): Boolean {
        val layoutManager =
            binding.recyclerView.layoutManager as LinearLayoutManager
        val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
        return lastVisiblePosition != -1 && (
            lastVisiblePosition == adapter.itemCount - 1 ||
                adapter.getItemViewType(lastVisiblePosition) == R.layout.form_section
            )
    }

    private fun handleKeyBoardOnFocusChange(items: List<FieldUiModel>) {
        items.firstOrNull { it.focused }?.let {
            if (!doesItemNeedsKeyboard(it)) {
                closeKeyboard()
            }
        }
    }

    private fun closeKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(binding.recyclerView.windowToken, 0)
    }

    private fun doesItemNeedsKeyboard(item: FieldUiModel) = when (item) {
        is EditTextViewModel,
        is ScanTextViewModel,
        is CoordinateViewModel -> true
        else -> false
    }
}
