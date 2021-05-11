package org.dhis2.data.forms.dataentry

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import org.dhis2.R
import org.dhis2.data.forms.dataentry.fields.age.DialogType
import org.dhis2.data.forms.dataentry.fields.coordinate.CoordinateViewModel
import org.dhis2.data.forms.dataentry.fields.edittext.EditTextViewModel
import org.dhis2.data.forms.dataentry.fields.scan.ScanTextViewModel
import org.dhis2.databinding.ViewFormBinding
import org.dhis2.form.Injector
import org.dhis2.form.data.FormRepository
import org.dhis2.form.model.ActionType
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.RowAction
import org.dhis2.form.ui.FormViewModel
import org.dhis2.utils.Constants
import org.dhis2.utils.DatePickerUtils
import org.dhis2.utils.DatePickerUtils.OnDatePickerClickListener
import org.dhis2.utils.DateUtils
import org.dhis2.utils.customviews.CustomDialog
import timber.log.Timber
import java.util.Calendar

class FormView(
    formRepository: FormRepository,
    private val onListChangedCallback: ((value: String) -> Unit)?
) : Fragment() {

    private val viewModel: FormViewModel by viewModels {
        Injector.provideFormViewModelFactory(formRepository)
    }

    private lateinit var binding: ViewFormBinding
    private lateinit var dataEntryHeaderHelper: DataEntryHeaderHelper
    private lateinit var adapter: DataEntryAdapter
    private lateinit var alertDialogView: View
    var scrollCallback: ((Boolean) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.view_form, container, false)
        binding.lifecycleOwner = this
        dataEntryHeaderHelper = DataEntryHeaderHelper(binding.headerContainer, binding.recyclerView)
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

        adapter.onItemAction = { action ->
            viewModel.onItemAction(action)
        }

        binding.recyclerView.adapter = adapter
        adapter.onShowCustomCalendar = { uid, label, date ->
            DatePickerUtils.getDatePickerDialog(
                requireContext(),
                label,
                date,
                true,
                object : OnDatePickerClickListener {
                    override fun onNegativeClick() {
                        handleNegativeDateInput(uid)
                    }

                    override fun onPositiveClick(datePicker: DatePicker) {
                        handlePositiveDateInput(
                            uid,
                            datePicker.year,
                            datePicker.month,
                            datePicker.dayOfMonth,
                            DialogType.DATE_CALENDAR
                        )
                    }
                }).show()
        }

        adapter.onShowYearMonthDayPicker = { uid, year, month, day ->
            alertDialogView =
                LayoutInflater.from(requireContext()).inflate(R.layout.dialog_age, null)
            val yearPicker = alertDialogView.findViewById<TextInputEditText>(R.id.input_year)
            val monthPicker = alertDialogView.findViewById<TextInputEditText>(R.id.input_month)
            val dayPicker = alertDialogView.findViewById<TextInputEditText>(R.id.input_days)
            yearPicker.setText(year.toString())
            monthPicker.setText(month.toString())
            dayPicker.setText(day.toString())

            AlertDialog.Builder(requireContext(), R.style.CustomDialog)
                .setView(alertDialogView)
                .setPositiveButton(R.string.action_accept) { dialog, which ->
                    val year =
                        alertDialogView.findViewById<TextInputEditText>(R.id.input_year).text.toString()
                    val month =
                        alertDialogView.findViewById<TextInputEditText>(R.id.input_month).text.toString()
                    val day =
                        alertDialogView.findViewById<TextInputEditText>(R.id.input_days).text.toString()
                    handlePositiveDateInput(
                        uid,
                        getDateValueOrZero(year),
                        getDateValueOrZero(month),
                        getDateValueOrZero(day),
                        DialogType.YEAR_MONTH_DAY_PICKER
                    )
                }
                .setNegativeButton(R.string.clear) { dialog, which -> handleNegativeDateInput(uid) }
                .create()
                .show()

        }

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

        viewModel.savedValue.observe(viewLifecycleOwner, Observer { value ->
            onListChangedCallback?.let { action ->
                action(value)
            }
        })

        viewModel.items.observe(viewLifecycleOwner, Observer { items ->
            render(items)
        })
    }

    fun handlePositiveDateInput(
        uid: String,
        year: Int,
        month: Int,
        day: Int,
        type: DialogType
    ) {
        val currentCalendar = Calendar.getInstance()
        val ageDate = with(currentCalendar) {
            if (type == DialogType.DATE_CALENDAR){
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, day)
            } else {
                add(Calendar.YEAR, year)
                add(Calendar.MONTH, month)
                add(Calendar.DAY_OF_MONTH, day)
            }
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            return@with time
        }

        val action = RowAction(
            uid,
            if (ageDate == null) null else DateUtils.oldUiDateFormat()
                .format(ageDate),
            false,
            null,
            null,
            null,
            null,
            ActionType.ON_SAVE
        )
        viewModel.onItemAction(action)
    }

    fun handleNegativeDateInput(uid: String) {
        val action = RowAction(
            uid,
            null,
            false,
            null,
            null,
            null,
            null,
            ActionType.ON_SAVE
        )
        viewModel.onItemAction(action)
    }

    fun getDateValueOrZero(value: String): Int {
        return if (value.isEmpty()) 0 else -Integer.valueOf(value)
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
                dataEntryHeaderHelper.onItemsUpdatedCallback()
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
