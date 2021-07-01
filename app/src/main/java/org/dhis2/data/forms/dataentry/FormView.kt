package org.dhis2.data.forms.dataentry

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar
import org.dhis2.Bindings.truncate
import org.dhis2.R
import org.dhis2.data.forms.dataentry.fields.age.AgeDialogDelegate
import org.dhis2.data.forms.dataentry.fields.age.negativeOrZero
import org.dhis2.data.forms.dataentry.fields.coordinate.CoordinateViewModel
import org.dhis2.data.forms.dataentry.fields.edittext.EditTextViewModel
import org.dhis2.data.forms.dataentry.fields.scan.ScanTextViewModel
import org.dhis2.data.location.LocationProvider
import org.dhis2.databinding.ViewFormBinding
import org.dhis2.form.Injector
import org.dhis2.form.data.FormRepository
import org.dhis2.form.data.FormRepositoryNonPersistenceImpl
import org.dhis2.form.model.ActionType
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.RowAction
import org.dhis2.form.ui.FormViewModel
import org.dhis2.form.ui.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent
import org.dhis2.uicomponents.map.views.MapSelectorActivity.Companion.DATA_EXTRA
import org.dhis2.uicomponents.map.views.MapSelectorActivity.Companion.FIELD_UID
import org.dhis2.uicomponents.map.views.MapSelectorActivity.Companion.LOCATION_TYPE_EXTRA
import org.dhis2.uicomponents.map.views.MapSelectorActivity.Companion.create
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialPresenter
import org.dhis2.utils.Constants
import org.dhis2.utils.DatePickerUtils
import org.dhis2.utils.DatePickerUtils.OnDatePickerClickListener
import org.dhis2.utils.customviews.CustomDialog
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.FeatureType
import timber.log.Timber

class FormView constructor(
    formRepository: FormRepository,
    private val onItemChangeListener: ((action: RowAction) -> Unit)?,
    private val locationProvider: LocationProvider?,
    private val onLoadingListener: ((loading: Boolean) -> Unit)?,
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

        adapter.onLocationRequest = { fieldUid ->
            locationProvider?.getLastKnownLocation(
                { location ->
                    val geometry = GeometryHelper.createPointGeometry(
                        location.longitude.truncate(),
                        location.latitude.truncate()
                    )
                    viewModel.onItemAction(
                        RowAction(
                            id = fieldUid,
                            value = geometry.coordinates(),
                            type = ActionType.ON_SAVE,
                            extraData = FeatureType.POINT.name
                        )
                    )
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

        adapter.onMapRequest = { fieldUid, featureType, initialData ->
            startActivityForResult(
                create(requireContext(), fieldUid, featureType, initialData),
                Constants.RQ_MAP_LOCATION_VIEW
            )
        }

        binding.recyclerView.adapter = adapter

        adapter.onIntent = { intent ->
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

        viewModel.loading.observe(
            viewLifecycleOwner,
            Observer { loading ->
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
    }

    private fun uiEventHandler(uiEvent: RecyclerViewUiEvents) {
        when (uiEvent) {
            is RecyclerViewUiEvents.OpenCustomAgeCalendar -> showCustomAgeCalendar(uiEvent)
            is RecyclerViewUiEvents.OpenYearMonthDayAgeCalendar -> showYearMonthDayAgeCalendar(
                uiEvent
            )
            is RecyclerViewUiEvents.ShowDescriptionLabelDialog -> showDescriptionLabelDialog(
                uiEvent
            )
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
                viewModel.onItemsRendered()
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
        viewModel.submitIntent(intent)
    }

    private fun showCustomAgeCalendar(intent: RecyclerViewUiEvents.OpenCustomAgeCalendar) {
        val date = Calendar.getInstance().time
        DatePickerUtils.getDatePickerDialog(
            requireContext(),
            intent.label,
            date,
            true,
            object : OnDatePickerClickListener {
                override fun onNegativeClick() {
                    val clearIntent = FormIntent.ClearDateFromAgeCalendar(intent.uid)
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
                val dateIntent = ageDialogDelegate.handleYearMonthDayInput(
                    intent.uid,
                    negativeOrZero(yearPicker.text.toString()),
                    negativeOrZero(monthPicker.text.toString()),
                    negativeOrZero(dayPicker.text.toString())
                )
                intentHandler(dateIntent)
            }
            .setNegativeButton(R.string.clear) { _, _ ->
                val clearIntent = FormIntent.ClearDateFromAgeCalendar(intent.uid)
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK &&
            requestCode == Constants.RQ_MAP_LOCATION_VIEW && data?.extras != null
        ) {
            data.apply {
                viewModel.setCoordinateFieldValue(
                    getStringExtra(FIELD_UID),
                    getStringExtra(LOCATION_TYPE_EXTRA),
                    getStringExtra(DATA_EXTRA)
                )
            }
        }
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
                adapter.onLocationRequest?.invoke(it)
            }
        }
    }

    class Builder {
        private var fragmentManager: FragmentManager? = null
        private var persistentRepository: FormRepository? = null
        private var onItemChangeListener: ((action: RowAction) -> Unit)? = null
        private var locationProvider: LocationProvider? = null
        private var needToForceUpdate: Boolean = false
        private var onLoadingListener: ((loading: Boolean) -> Unit)? = null

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
         * If set,
         * */
        fun onLoadingListener(callback: (loading: Boolean) -> Unit) =
            apply { this.onLoadingListener = callback }

        /**
         * Set a FragmentManager for instantiating the form view
         * */
        fun factory(manager: FragmentManager) =
            apply { fragmentManager = manager }

        fun build(): FormView {
            if (fragmentManager == null) {
                throw Exception("You need to call factory method and pass a FragmentManager")
            }
            fragmentManager!!.fragmentFactory =
                FormViewFragmentFactory(
                    persistentRepository ?: FormRepositoryNonPersistenceImpl(),
                    locationProvider,
                    onItemChangeListener,
                    needToForceUpdate,
                    onLoadingListener
                )

            return fragmentManager!!.fragmentFactory.instantiate(
                this.javaClass.classLoader!!,
                FormView::class.java.name
            ) as FormView
        }
    }
}
