package org.dhis2.usescases.eventsWithoutRegistration.eventDetails.ui

import android.Manifest
import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.unnamed.b.atv.model.TreeNode
import java.util.Date
import javax.inject.Inject
import kotlinx.coroutines.flow.collect
import org.dhis2.R
import org.dhis2.commons.data.EventCreationType
import org.dhis2.commons.dialogs.calendarpicker.CalendarPicker
import org.dhis2.commons.dialogs.calendarpicker.OnDatePickerListener
import org.dhis2.databinding.EventDetailsFragmentBinding
import org.dhis2.maps.views.MapSelectorActivity
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.injection.EventDetailsComponentProvider
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.injection.EventDetailsModule
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventCategory
import org.dhis2.usescases.eventsWithoutRegistration.eventDetails.models.EventDetails
import org.dhis2.usescases.general.FragmentGlobalAbstract
import org.dhis2.utils.Constants.ENROLLMENT_STATUS
import org.dhis2.utils.Constants.ENROLLMENT_UID
import org.dhis2.utils.Constants.EVENT_CREATION_TYPE
import org.dhis2.utils.Constants.EVENT_PERIOD_TYPE
import org.dhis2.utils.Constants.EVENT_SCHEDULE_INTERVAL
import org.dhis2.utils.Constants.EVENT_UID
import org.dhis2.utils.Constants.ORG_UNIT
import org.dhis2.utils.Constants.PROGRAM_STAGE_UID
import org.dhis2.utils.Constants.PROGRAM_UID
import org.dhis2.utils.category.CategoryDialog
import org.dhis2.utils.category.CategoryDialog.Companion.TAG
import org.dhis2.utils.customviews.CatOptionPopUp
import org.dhis2.utils.customviews.OrgUnitDialog
import org.dhis2.utils.customviews.PeriodDialog
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.period.PeriodType

class EventDetailsFragment : FragmentGlobalAbstract() {

    @Inject
    lateinit var factory: EventDetailsViewModelFactory

    private val requestLocationPermissions =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            if (result.values.all { isGranted -> isGranted }) {
                viewModel.requestCurrentLocation()
            }
        }

    private val requestLocationByMap =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data?.extras != null
            ) {
                val featureType: String =
                    result.data!!.getStringExtra(MapSelectorActivity.LOCATION_TYPE_EXTRA)!!
                val coordinates = result.data?.getStringExtra(MapSelectorActivity.DATA_EXTRA)
                viewModel.onLocationByMapSelected(FeatureType.valueOf(featureType), coordinates)
            }
        }

    private val viewModel: EventDetailsViewModel by viewModels {
        factory
    }

    var onEventDetailsChange: ((eventDetails: EventDetails) -> Unit)? = null
    var onButtonCallback: (() -> Unit)? = null

    private lateinit var binding: EventDetailsFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        (requireActivity() as EventDetailsComponentProvider).provideEventDetailsComponent(
            EventDetailsModule(
                eventUid = requireArguments().getString(EVENT_UID),
                context = requireContext(),
                eventCreationType = getEventCreationType(
                    requireArguments().getString(EVENT_CREATION_TYPE)
                ),
                programStageUid = requireArguments().getString(PROGRAM_STAGE_UID),
                programUid = requireArguments().getString(PROGRAM_UID)!!,
                periodType = requireArguments()
                    .getSerializable(EVENT_PERIOD_TYPE) as PeriodType?,
                enrollmentId = requireArguments().getString(ENROLLMENT_UID),
                scheduleInterval = requireArguments().getInt(EVENT_SCHEDULE_INTERVAL),
                initialOrgUnitUid = requireArguments().getString(ORG_UNIT),
                enrollmentStatus = requireArguments()
                    .getSerializable(ENROLLMENT_STATUS) as EnrollmentStatus?
            )
        )?.inject(this)
        binding = DataBindingUtil.inflate(
            inflater, R.layout.event_details_fragment,
            container,
            false
        )
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenStarted {
            viewModel.eventDetails.collect {
                onEventDetailsChange?.invoke(it)
            }
        }

        viewModel.showCalendar = {
            showCalendarDialog()
        }

        viewModel.showPeriods = {
            showPeriodDialog()
        }

        viewModel.showOrgUnits = {
            showOrgUnitDialog()
        }

        viewModel.showNoOrgUnits = {
            showNoOrgUnitsDialog()
        }

        viewModel.showCategoryDialog = { category ->
            showCategoryDialog(category)
        }

        viewModel.showCategoryPopUp = { category ->
            showCategoryPopUp(category)
        }

        viewModel.requestLocationPermissions = {
            requestLocationPermissions.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
            )
        }

        viewModel.requestLocationByMap = { featureType, initCoordinate ->
            requestLocationByMap.launch(
                MapSelectorActivity.create(
                    requireActivity(),
                    FeatureType.valueOfFeatureType(featureType),
                    initCoordinate
                )
            )
        }

        viewModel.showEnableLocationMessage = {
            displayMessage(getString(R.string.enable_location_message))
        }

        viewModel.onButtonClickCallback = {
            onButtonCallback?.invoke() ?: viewModel.onActionButtonClick()
        }

        viewModel.showEventUpdateStatus = { message ->
            displayMessage(message)
        }
    }

    private fun showCalendarDialog() {
        val dialog = CalendarPicker(requireContext())
        dialog.setInitialDate(viewModel.eventDate.value.currentDate)
        dialog.setMinDate(viewModel.eventDate.value.minDate)
        dialog.setMaxDate(viewModel.eventDate.value.maxDate)
        dialog.setScheduleInterval(viewModel.eventDate.value.scheduleInterval)
        dialog.isFutureDatesAllowed(viewModel.eventDate.value.allowFutureDates)
        dialog.setListener(
            object : OnDatePickerListener {
                override fun onNegativeClick() {}
                override fun onPositiveClick(datePicker: DatePicker) {
                    viewModel.onDateSet(
                        datePicker.year,
                        datePicker.month,
                        datePicker.dayOfMonth
                    )
                }
            }
        )
        dialog.show()
    }

    private fun showPeriodDialog() {
        PeriodDialog()
            .setPeriod(viewModel.eventDate.value.periodType)
            .setMinDate(viewModel.eventDate.value.minDate)
            .setMaxDate(viewModel.eventDate.value.maxDate)
            .setPossitiveListener { selectedDate: Date ->
                viewModel.setUpEventReportDate(selectedDate)
            }
            .show(requireActivity().supportFragmentManager, PeriodDialog::class.java.simpleName)
    }

    private fun showOrgUnitDialog() {
        val dialog = OrgUnitDialog.getInstace()
            .setTitle(
                viewModel.eventOrgUnit.value.selectedOrgUnit?.displayName()
                    ?: getString(R.string.org_unit)
            )
            .setMultiSelection(false)
            .setOrgUnits(viewModel.eventOrgUnit.value.orgUnits)
            .setProgram(viewModel.eventOrgUnit.value.programUid)

        dialog.setPossitiveListener {
            viewModel.setUpOrgUnit(selectedOrgUnit = dialog.selectedOrgUnit)
            dialog.dismiss()
        }
        dialog.setNegativeListener { dialog.dismiss() }
        dialog.setNodeClickListener { node: TreeNode, _: Any? ->
            if (node.children.isNotEmpty()) node.isExpanded = node.isExpanded
        }
        dialog.show(requireActivity().supportFragmentManager, "ORG_UNIT_DIALOG")
    }

    private fun showNoOrgUnitsDialog() {
        showInfoDialog(getString(R.string.error), getString(R.string.no_org_units))
    }

    private fun showCategoryPopUp(category: EventCategory) {
        CatOptionPopUp(
            requireContext(),
            binding.catComboLayout,
            category.name,
            category.options,
            true,
            viewModel.eventDate.value.currentDate
        ) { categoryOption ->
            val selectedOption = Pair(category.uid, categoryOption?.uid())
            viewModel.setUpCategoryCombo(selectedOption)
        }.show()
    }

    private fun showCategoryDialog(category: EventCategory) {
        CategoryDialog(
            CategoryDialog.Type.CATEGORY_OPTIONS,
            category.uid,
            true,
            viewModel.eventDate.value.currentDate
        ) { categoryOption ->
            val selectedOption = Pair(category.uid, categoryOption)
            viewModel.setUpCategoryCombo(selectedOption)
        }.show(requireActivity().supportFragmentManager, TAG)
    }

    private fun getEventCreationType(typeString: String?): EventCreationType {
        return typeString?.let {
            EventCreationType.valueOf(it)
        } ?: EventCreationType.DEFAULT
    }
}
