package org.dhis2.data.forms.dataentry

import android.Manifest
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.format.DateFormat
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar
import org.dhis2.R
import org.dhis2.commons.dialogs.CustomDialog
import org.dhis2.commons.dialogs.calendarpicker.CalendarPicker
import org.dhis2.commons.dialogs.calendarpicker.OnDatePickerListener
import org.dhis2.commons.extensions.truncate
import org.dhis2.data.location.LocationProvider
import org.dhis2.databinding.ViewFormBinding
import org.dhis2.form.Injector
import org.dhis2.form.data.FormRepository
import org.dhis2.form.model.DispatcherProvider
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.RowAction
import org.dhis2.form.model.coroutine.FormDispatcher
import org.dhis2.form.ui.FormViewModel
import org.dhis2.form.ui.event.DialogDelegate
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.maps.views.MapSelectorActivity
import org.dhis2.maps.views.MapSelectorActivity.Companion.DATA_EXTRA
import org.dhis2.maps.views.MapSelectorActivity.Companion.FIELD_UID
import org.dhis2.maps.views.MapSelectorActivity.Companion.LOCATION_TYPE_EXTRA
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialPresenter
import org.dhis2.usescases.qrScanner.ScanActivity
import org.dhis2.utils.Constants
import org.dhis2.utils.customviews.QRDetailBottomDialog
import org.dhis2.utils.customviews.orgUnitCascade.OrgUnitCascadeDialog
import org.dhis2.utils.customviews.orgUnitCascade.OrgUnitCascadeDialog.CascadeOrgUnitCallbacks
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ValueType
import timber.log.Timber

class FormView constructor(
    formRepository: FormRepository,
    private val onItemChangeListener: ((action: RowAction) -> Unit)?,
    private val locationProvider: LocationProvider?,
    private val onLoadingListener: ((loading: Boolean) -> Unit)?,
    private val onFocused: (() -> Unit)?,
    private val onActivityForResult: (() -> Unit)?,
    private val needToForceUpdate: Boolean = false,
    dispatchers: DispatcherProvider
) : Fragment() {

    private val qrScanContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val intent = FormIntent.OnSave(
                    it.data?.getStringExtra(Constants.UID)!!,
                    it.data?.getStringExtra(Constants.EXTRA_DATA),
                    ValueType.TEXT
                )
                intentHandler(intent)
            }
        }

    private val mapContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK && it.data?.extras != null
            ) {
                val uid = it.data?.getStringExtra(FIELD_UID)
                val featureType = it.data?.getStringExtra(LOCATION_TYPE_EXTRA)
                val coordinates = it.data?.getStringExtra(DATA_EXTRA)
                if (uid != null && featureType != null) {
                    val intent = FormIntent.SelectLocationFromMap(
                        uid,
                        featureType,
                        coordinates
                    )
                    intentHandler(intent)
                }
            }
        }

    private val viewModel: FormViewModel by viewModels {
        Injector.provideFormViewModelFactory(formRepository, dispatchers)
    }

    private lateinit var binding: ViewFormBinding
    private lateinit var dataEntryHeaderHelper: DataEntryHeaderHelper
    private lateinit var adapter: DataEntryAdapter
    private lateinit var alertDialogView: View
    private lateinit var dialogDelegate: DialogDelegate
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
        dialogDelegate = DialogDelegate()
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

        binding.recyclerView.adapter = adapter

        adapter.onIntent = { intent ->
            if (intent is FormIntent.OnNext) {
                scrollToPosition(intent.position!!)
            }
            intentHandler(intent)
        }

        adapter.onRecyclerViewUiEvents = { uiEvent ->
            uiEventHandler(uiEvent)
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

        viewModel.savedValue.observe(
            viewLifecycleOwner,
            { rowAction ->
                onItemChangeListener?.let { it(rowAction) }
            }
        )

        viewModel.queryData.observe(
            viewLifecycleOwner,
            { rowAction ->
                if (needToForceUpdate) {
                    onItemChangeListener?.let { it(rowAction) }
                }
            }
        )

        viewModel.items.observe(
            viewLifecycleOwner,
            { items ->
                render(items)
            }
        )

        viewModel.loading.observe(
            viewLifecycleOwner,
            { loading ->
                if (onLoadingListener != null) {
                    onLoadingListener.invoke(loading)
                } else {
                    if (loading) {
                        binding.progress.show()
                    } else {
                        binding.progress.hide()
                    }
                }
            }
        )

        viewModel.showToast.observe(
            viewLifecycleOwner,
            { message ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        )

        viewModel.focused.observe(
            viewLifecycleOwner,
            { onFocused?.invoke() }
        )

        viewModel.showInfo.observe(
            viewLifecycleOwner,
            { infoUiModel ->
                CustomDialog(
                    requireContext(),
                    requireContext().getString(infoUiModel.title),
                    requireContext().getString(infoUiModel.description),
                    requireContext().getString(R.string.action_close),
                    null,
                    Constants.DESCRIPTION_DIALOG,
                    null
                ).show()
            }
        )
    }

    private fun scrollToPosition(position: Int) {
        val viewHolder = binding.recyclerView.findViewHolderForLayoutPosition(position + 1)
        if (viewHolder == null) {
            try {
                binding.recyclerView.smoothScrollToPosition(position + 1)
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
    }

    private fun uiEventHandler(uiEvent: RecyclerViewUiEvents) {
        when (uiEvent) {
            is RecyclerViewUiEvents.OpenCustomCalendar -> showCustomCalendar(uiEvent)
            is RecyclerViewUiEvents.OpenYearMonthDayAgeCalendar -> showYearMonthDayAgeCalendar(
                uiEvent
            )
            is RecyclerViewUiEvents.OpenTimePicker -> showTimePicker(uiEvent)
            is RecyclerViewUiEvents.ShowDescriptionLabelDialog -> showDescriptionLabelDialog(
                uiEvent
            )
            is RecyclerViewUiEvents.RequestCurrentLocation -> requestCurrentLocation(uiEvent)
            is RecyclerViewUiEvents.RequestLocationByMap -> requestLocationByMap(uiEvent)
            is RecyclerViewUiEvents.DisplayQRCode -> displayQRImage(uiEvent)
            is RecyclerViewUiEvents.ScanQRCode -> requestQRScan(uiEvent)
            is RecyclerViewUiEvents.OpenOrgUnitDialog -> showOrgUnitDialog(uiEvent)
        }
    }

    private fun render(items: List<FieldUiModel>) {
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
            items
        ) {
            when (needToForceUpdate) {
                true -> adapter.notifyDataSetChanged()
                else -> dataEntryHeaderHelper.onItemsUpdatedCallback()
            }
            viewModel.onItemsRendered()
        }
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
        items.firstOrNull { it.focused }?.let { fieldUiModel ->
            fieldUiModel.valueType?.let { valueType ->
                if (!needsKeyboard(valueType)) {
                    closeKeyboard()
                }
            }
        }
    }

    private fun needsKeyboard(valueType: ValueType): Boolean {
        return valueType.isText ||
            valueType.isNumeric ||
            valueType.isInteger
    }

    private fun closeKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(binding.recyclerView.windowToken, 0)
    }

    private fun intentHandler(intent: FormIntent) {
        viewModel.submitIntent(intent)
    }

    private fun showCustomCalendar(intent: RecyclerViewUiEvents.OpenCustomCalendar) {
        val dialog = CalendarPicker(requireContext()).apply {
            setTitle(intent.label)
            setInitialDate(intent.date)
            isFutureDatesAllowed(intent.allowFutureDates)
            setListener(object : OnDatePickerListener {
                override fun onNegativeClick() {
                    intentHandler(FormIntent.ClearValue(intent.uid))
                }

                override fun onPositiveClick(datePicker: DatePicker) {
                    when (intent.isDateTime) {
                        true -> uiEventHandler(
                            dialogDelegate.handleDateTimeInput(
                                intent.uid,
                                intent.label,
                                intent.date,
                                datePicker.year,
                                datePicker.month,
                                datePicker.dayOfMonth
                            )
                        )
                        else -> intentHandler(
                            dialogDelegate.handleDateInput(
                                intent.uid,
                                datePicker.year,
                                datePicker.month,
                                datePicker.dayOfMonth
                            )
                        )
                    }
                }
            })
        }
        dialog.show()
    }

    private fun showTimePicker(intent: RecyclerViewUiEvents.OpenTimePicker) {
        val calendar = Calendar.getInstance()
        intent.date?.let { calendar.time = it }
        val is24HourFormat = DateFormat.is24HourFormat(requireContext())
        val dialog = TimePickerDialog(
            requireContext(),
            { _: TimePicker?, hourOfDay: Int, minutes: Int ->
                intentHandler(
                    dialogDelegate.handleTimeInput(
                        intent.uid,
                        if (intent.isDateTime == true) intent.date else null,
                        hourOfDay,
                        minutes
                    )
                )
            },
            calendar[Calendar.HOUR_OF_DAY],
            calendar[Calendar.MINUTE],
            is24HourFormat
        )
        dialog.setTitle(intent.label)
        dialog.show()
    }

    private fun showYearMonthDayAgeCalendar(
        intent: RecyclerViewUiEvents.OpenYearMonthDayAgeCalendar
    ) {
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
                val dateIntent = dialogDelegate.handleYearMonthDayInput(
                    intent.uid,
                    negativeOrZero(yearPicker.text.toString()),
                    negativeOrZero(monthPicker.text.toString()),
                    negativeOrZero(dayPicker.text.toString())
                )
                intentHandler(dateIntent)
            }
            .setNegativeButton(R.string.clear) { _, _ ->
                val clearIntent = FormIntent.ClearValue(intent.uid)
                intentHandler(clearIntent)
            }
            .create()
            .show()
    }

    private fun showDescriptionLabelDialog(
        intent: RecyclerViewUiEvents.ShowDescriptionLabelDialog
    ) {
        CustomDialog(
            requireContext(),
            intent.title,
            intent.message ?: requireContext().getString(R.string.empty_description),
            requireContext().getString(R.string.action_close),
            null,
            Constants.DESCRIPTION_DIALOG,
            null
        ).show()
    }

    private fun requestCurrentLocation(event: RecyclerViewUiEvents.RequestCurrentLocation) {
        locationProvider?.getLastKnownLocation(
            { location ->
                val geometry = GeometryHelper.createPointGeometry(
                    location.longitude.truncate(),
                    location.latitude.truncate()
                )
                val intent = FormIntent.SelectLocationFromCoordinates(
                    event.uid,
                    geometry.coordinates(),
                    FeatureType.POINT.name
                )

                intentHandler(intent)
            },
            {
                this@FormView.requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    EventInitialPresenter.ACCESS_LOCATION_PERMISSION_REQUEST
                )
            },
            {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.enable_location_message),
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }

    private fun requestLocationByMap(event: RecyclerViewUiEvents.RequestLocationByMap) {
        onActivityForResult?.invoke()
        mapContent.launch(
            MapSelectorActivity.create(requireContext(), event.uid, event.featureType, event.value)
        )
    }

    private fun requestQRScan(event: RecyclerViewUiEvents.ScanQRCode) {
        onActivityForResult?.invoke()
        qrScanContent.launch(
            Intent(context, ScanActivity::class.java).apply {
                putExtra(Constants.UID, event.uid)
                putExtra(Constants.OPTION_SET, event.optionSet)
                putExtra(Constants.SCAN_RENDERING_TYPE, event.renderingType)
            }
        )
    }

    private fun displayQRImage(event: RecyclerViewUiEvents.DisplayQRCode) {
        QRDetailBottomDialog(
            event.value,
            event.renderingType,
            event.editable,
            {
                intentHandler(FormIntent.OnNext(event.uid, null))
            },
            {
                requestQRScan(
                    RecyclerViewUiEvents.ScanQRCode(
                        event.uid,
                        event.optionSet,
                        event.renderingType
                    )
                )
            }
        ).show(
            childFragmentManager,
            QRDetailBottomDialog.TAG
        )
    }

    private fun showOrgUnitDialog(uiEvent: RecyclerViewUiEvents.OpenOrgUnitDialog) {
        OrgUnitCascadeDialog(
            uiEvent.label,
            uiEvent.value,
            object : CascadeOrgUnitCallbacks {
                override fun textChangedConsumer(
                    selectedOrgUnitUid: String,
                    selectedOrgUnitName: String
                ) {
                    intentHandler(
                        FormIntent.OnSave(
                            uiEvent.uid,
                            selectedOrgUnitUid,
                            ValueType.ORGANISATION_UNIT
                        )
                    )
                }

                override fun onDialogCancelled() {}

                override fun onClear() {
                    intentHandler(FormIntent.ClearValue(uiEvent.uid))
                }
            },
            OrgUnitCascadeDialog.OUSelectionType.SEARCH
        ).show(childFragmentManager, uiEvent.label)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        if (requestCode == EventInitialPresenter.ACCESS_LOCATION_PERMISSION_REQUEST &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            viewModel.getFocusedItemUid()?.let {
                requestCurrentLocation(RecyclerViewUiEvents.RequestCurrentLocation(it))
            }
        }
    }

    fun onEditionFinish() {
        binding.recyclerView.requestFocus()
    }

    fun processItems(items: List<FieldUiModel>?) {
        viewModel.processCalculatedItems(items)
    }

    private fun negativeOrZero(value: String): Int {
        return if (value.isEmpty()) 0 else -Integer.valueOf(value)
    }

    class Builder {
        private var fragmentManager: FragmentManager? = null
        private var repository: FormRepository? = null
        private var onItemChangeListener: ((action: RowAction) -> Unit)? = null
        private var locationProvider: LocationProvider? = null
        private var needToForceUpdate: Boolean = false
        private var onLoadingListener: ((loading: Boolean) -> Unit)? = null
        private var dispatchers: DispatcherProvider? = null
        private var onFocused: (() -> Unit)? = null
        private var onActivityForResult: (() -> Unit)? = null

        /**
         * If you want to persist the items and it's changes in any sources, please provide an
         * implementation of the repository with a valueStore.
         *
         * IF you don't provide any valueStore in repository constructor, it will be kept in memory.
         *
         * NOTE: This step is temporary in order to facilitate refactor, in the future will be
         * changed by some info like DataEntryStore.EntryMode and Event/Program uid. Then the
         * library will generate the implementation of the repository.
         */
        fun repository(repository: FormRepository) =
            apply { this.repository = repository }

        /**
         * If you want to handle the behaviour of the form and be notified when any item is updated,
         * implement this listener.
         */
        fun onItemChangeListener(callback: (action: RowAction) -> Unit) =
            apply { this.onItemChangeListener = callback }

        /**
         *
         */
        fun locationProvider(locationProvider: LocationProvider) =
            apply { this.locationProvider = locationProvider }

        /**
         * If it's set to true, any change on the list will make update all of it's items.
         */
        fun needToForceUpdate(needToForceUpdate: Boolean) =
            apply { this.needToForceUpdate = needToForceUpdate }

        /**
         * Sets if loading started or finished to handle loadingfeedback
         * */
        fun onLoadingListener(callback: (loading: Boolean) -> Unit) =
            apply { this.onLoadingListener = callback }

        /**
         * It's triggered when form gets focus
         */
        fun onFocused(callback: () -> Unit) =
            apply { this.onFocused = callback }

        /**
         * By default it uses Coroutine dispatcher IO, Computation, and Main but, you could also set
         * a custom one for testing for eg.
         */
        fun dispatcher(dispatcher: DispatcherProvider) = apply { this.dispatchers = dispatcher }

        /**
         * Set a FragmentManager for instantiating the form view
         * */
        fun factory(manager: FragmentManager) =
            apply { fragmentManager = manager }

        /**
         * Listener for the current activity to know if a activityForResult is called
         * */
        fun activityForResultListener(callback: () -> Unit) =
            apply { this.onActivityForResult = callback }

        fun build(): FormView {
            if (fragmentManager == null) {
                throw Exception("You need to call factory method and pass a FragmentManager")
            }
            if (repository == null) {
                throw Exception("You need to call persistence method and pass a FormRepository")
            }
            fragmentManager!!.fragmentFactory =
                FormViewFragmentFactory(
                    repository!!,
                    locationProvider,
                    onItemChangeListener,
                    needToForceUpdate,
                    onLoadingListener,
                    onFocused,
                    onActivityForResult,
                    dispatchers = dispatchers ?: FormDispatcher()
                )

            return fragmentManager!!.fragmentFactory.instantiate(
                this.javaClass.classLoader!!,
                FormView::class.java.name
            ) as FormView
        }
    }
}
