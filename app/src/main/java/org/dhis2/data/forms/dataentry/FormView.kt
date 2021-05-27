package org.dhis2.data.forms.dataentry

import android.content.Context
import android.os.Bundle
import android.view.ContextThemeWrapper
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
import org.dhis2.data.forms.dataentry.fields.age.AgeDialogDelegate
import org.dhis2.data.forms.dataentry.fields.age.negativeOrZero
import org.dhis2.data.forms.dataentry.fields.coordinate.CoordinateViewModel
import org.dhis2.data.forms.dataentry.fields.edittext.EditTextViewModel
import org.dhis2.data.forms.dataentry.fields.scan.ScanTextViewModel
import org.dhis2.databinding.ViewFormBinding
import org.dhis2.form.Injector
import org.dhis2.form.data.FormRepository
import org.dhis2.form.data.FormRepositoryNonPersistenceImpl
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.RowAction
import org.dhis2.form.ui.FormIntent
import org.dhis2.form.ui.FormViewModel
import org.dhis2.utils.Constants
import org.dhis2.utils.DatePickerUtils
import org.dhis2.utils.DatePickerUtils.OnDatePickerClickListener
import org.dhis2.utils.customviews.CustomDialog
import timber.log.Timber
import java.util.Calendar

class FormView private constructor(
    formRepository: FormRepository,
    private val onItemChangeListener: ((action: RowAction) -> Unit)?,
    private val needToForceUpdate: Boolean = false
) : Fragment() {

    private val viewModel: FormViewModel by viewModels {
        Injector.provideFormViewModelFactory(formRepository)
    }

    private lateinit var binding: ViewFormBinding
    private lateinit var dataEntryHeaderHelper: DataEntryHeaderHelper
    private lateinit var adapter: DataEntryAdapter
    private lateinit var alertDialogView: View
    private lateinit var ageDialogDelegate: AgeDialogDelegate
    var scrollCallback: ((Boolean) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val contextWrapper = ContextThemeWrapper(context, R.style.searchFormInputText)
        binding = DataBindingUtil.inflate(inflater, R.layout.view_form, container, false)
        binding.lifecycleOwner = this
        dataEntryHeaderHelper = DataEntryHeaderHelper(binding.headerContainer, binding.recyclerView)
        ageDialogDelegate = AgeDialogDelegate()
        binding.recyclerView.layoutManager =
            object : LinearLayoutManager(contextWrapper, VERTICAL, false) {
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
        adapter = DataEntryAdapter(needToForceUpdate)
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

        adapter.onIntent = { intent ->
            intentHandler(intent)
        }

        //Date date = Calendar.getInstance().getTime();
        /*    adapter.onShowCustomCalendar = { uid, label, date ->
                DatePickerUtils.getDatePickerDialog(
                    requireContext(),
                    label,
                    date,
                    true,
                    object : OnDatePickerClickListener {
                        override fun onNegativeClick() {
                            ageDialogDelegate.handleClearInput(uid)
                        }

                        override fun onPositiveClick(datePicker: DatePicker) {
                            ageDialogDelegate.handleDateInput(
                                uid,
                                datePicker.year,
                                datePicker.month,
                                datePicker.dayOfMonth
                            )
                        }
                    }
                ).show()
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
                    .setPositiveButton(R.string.action_accept) { _, _ ->
                        ageDialogDelegate.handleYearMonthDayInput(
                            uid,
                            negativeOrZero(yearPicker.text.toString()),
                            negativeOrZero(monthPicker.text.toString()),
                            negativeOrZero(dayPicker.text.toString())
                        )
                    }
                    .setNegativeButton(R.string.clear) { _, _ ->
                        ageDialogDelegate.handleClearInput(uid)
                    }
                    .create()
                    .show()
            } */

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

        viewModel.savedValue.observe(
            viewLifecycleOwner,
            Observer { rowAction ->
                onItemChangeListener?.let { it(rowAction) }
            }
        )

        viewModel.items.observe(
            viewLifecycleOwner,
            Observer { items ->
                render(items)
            }
        )
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

    private fun intentHandler(intent: FormIntent) {
        when (intent) {
            is FormIntent.OpenCustomAgeCalendar -> {
                showCustomAgeCalendar(intent)
            }
            is FormIntent.OpenYearMonthDayAgeCalendar -> {
                showYearMonthDayAgeCalendar(intent)
            }
            else -> {
                viewModel.submitIntent(intent)
            }
        }
    }

    private fun showCustomAgeCalendar(intent: FormIntent.OpenCustomAgeCalendar) {
        val date = Calendar.getInstance().time
        DatePickerUtils.getDatePickerDialog(
            requireContext(),
            intent.label,
            date,
            true,
            object : OnDatePickerClickListener {
                override fun onNegativeClick() {
                    val clearIntent = ageDialogDelegate.handleClearInputCustomCalendar(intent.uid)
                    intentHandler(clearIntent)
                }

                override fun onPositiveClick(datePicker: DatePicker) {
                    val dateIntent = ageDialogDelegate.handleDateInput(
                        intent.uid,
                        datePicker.year,
                        datePicker.month,
                        datePicker.dayOfMonth
                    )
                    intentHandler(dateIntent)
                }
            }
        ).show()
    }

    private fun showYearMonthDayAgeCalendar(intent: FormIntent.OpenYearMonthDayAgeCalendar) {
        alertDialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_age, null)
        val yearPicker = alertDialogView.findViewById<TextInputEditText>(R.id.input_year)
        val monthPicker = alertDialogView.findViewById<TextInputEditText>(R.id.input_month)
        val dayPicker = alertDialogView.findViewById<TextInputEditText>(R.id.input_days)
        yearPicker.setText(intent.year.toString())
        monthPicker.setText(intent.month.toString())
        dayPicker.setText(intent.day.toString())

        AlertDialog.Builder(requireContext(), R.style.CustomDialog)
            .setView(alertDialogView)
            .setPositiveButton(R.string.action_accept) { _, _ ->
                val dateIntent = ageDialogDelegate.handleYearMonthDayInput(
                    intent.uid,
                    negativeOrZero(yearPicker.text.toString()),
                    negativeOrZero(monthPicker.text.toString()),
                    negativeOrZero(dayPicker.text.toString())
                )
                intentHandler(dateIntent)
            }
            .setNegativeButton(R.string.clear) { _, _ ->
                val clearIntent = ageDialogDelegate.handleClearInputYearMonthDayCalendar(intent.uid)
                intentHandler(clearIntent)
            }
            .create()
            .show()
    }

    class Builder {
        private var persistentRepository: FormRepository? = null
        private var onItemChangeListener: ((action: RowAction) -> Unit)? = null
        private var needToForceUpdate: Boolean = false

        /**
         * If you want to persist the items and it's changes in any sources, please provide an
         * implementation of the repository that fits with your system.
         *
         * IF you don't provide any repository implementation, data will be kept in memory.
         *
         * NOTE: This step is temporary in order to facilitate refactor, in the future will be
         * changed by some info like DataEntryStore.EntryMode and Event/Program uid. Then the
         * library will generate the implementation of the repository.
         */
        fun persistence(repository: FormRepository) =
            apply { this.persistentRepository = repository }

        /**
         * If you want to handle the behaviour of the form and be notified when any item is updated,
         * implement this listener.
         */
        fun onItemChangeListener(callback: (action: RowAction) -> Unit) =
            apply { this.onItemChangeListener = callback }

        /**
         * If it's set to true, any change on the list will make update all of it's items.
         */
        fun needToForceUpdate(needToForceUpdate: Boolean) =
            apply { this.needToForceUpdate = needToForceUpdate }

        fun build(): FormView {
            return FormView(
                formRepository = persistentRepository ?: FormRepositoryNonPersistenceImpl(),
                onItemChangeListener = onItemChangeListener,
                needToForceUpdate = needToForceUpdate
            )
        }
    }
}
