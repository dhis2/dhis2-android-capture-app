package org.dhis2.usescases.enrollment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils.isEmpty
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Flowable
import java.io.File
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import org.dhis2.App
import org.dhis2.R
import org.dhis2.data.forms.dataentry.DataEntryAdapter
import org.dhis2.data.forms.dataentry.DataEntryArguments
import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.dhis2.data.forms.dataentry.fields.RowAction
import org.dhis2.data.forms.dataentry.fields.display.DisplayViewModel
import org.dhis2.databinding.EnrollmentActivityBinding
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.map.MapSelectorActivity
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.dhis2.utils.Constants
import org.dhis2.utils.DatePickerUtils
import org.dhis2.utils.DateUtils
import org.dhis2.utils.DialogClickListener
import org.dhis2.utils.FileResourcesUtil
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.DELETE_AND_BACK
import org.dhis2.utils.analytics.SAVE_ENROLL
import org.dhis2.utils.analytics.STATUS_ENROLLMENT
import org.dhis2.utils.customviews.CustomDialog
import org.hisp.dhis.android.core.arch.helpers.FileResourceDirectoryHelper
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType

class EnrollmentActivity : ActivityGlobalAbstract(), EnrollmentView {

    enum class EnrollmentMode { NEW, CHECK }

    @Inject
    lateinit var presenter: EnrollmentPresenterImpl

    lateinit var binding: EnrollmentActivityBinding
    lateinit var mode: EnrollmentMode

    companion object {
        const val ENROLLMENT_UID_EXTRA = "ENROLLMENT_UID_EXTRA"
        const val PROGRAM_UID_EXTRA = "PROGRAM_UID_EXTRA"
        const val MODE_EXTRA = "MODE_EXTRA"
        const val RQ_ENROLLMENT_GEOMETRY = 1023
        const val RQ_INCIDENT_GEOMETRY = 1024
        const val RQ_EVENT = 1025
        const val RQ_GO_BACK = 1026

        fun getIntent(
                context: Context,
                enrollmentUid: String,
                programUid: String,
                enrollmentMode: EnrollmentMode
        ): Intent {
            val intent = Intent(context, EnrollmentActivity::class.java)
            intent.putExtra(ENROLLMENT_UID_EXTRA, enrollmentUid)
            intent.putExtra(PROGRAM_UID_EXTRA, programUid)
            intent.putExtra(MODE_EXTRA, enrollmentMode.name)
            return intent
        }
    }

    private lateinit var adapter: DataEntryAdapter

    /*region LIFECYCLE*/

    override fun onCreate(savedInstanceState: Bundle?) {
        (applicationContext as App).userComponent()!!.plus(
                EnrollmentModule(
                        this,
                        intent.getStringExtra(ENROLLMENT_UID_EXTRA),
                        intent.getStringExtra(PROGRAM_UID_EXTRA)
                )
        ).inject(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.enrollment_activity)
        binding.view = this

        mode = EnrollmentMode.valueOf(intent.getStringExtra(MODE_EXTRA))

        binding.programLockLayout.visibility =
                if (mode == EnrollmentMode.NEW) View.GONE else View.VISIBLE

        binding.coordinatesView.setIsBgTransparent(true)
        binding.teiCoordinatesView.setIsBgTransparent(true)

        adapter = DataEntryAdapter(
                LayoutInflater.from(this), supportFragmentManager,
                DataEntryArguments.forEnrollment(intent.getStringExtra(ENROLLMENT_UID_EXTRA))
        )
        binding.fieldRecycler.isNestedScrollingEnabled = true
        binding.fieldRecycler.adapter = adapter

        binding.next.setOnClickListener {
            if (presenter.dataIntegrityCheck(adapter.mandatoryOk(), adapter.hasError())) {
                binding.root.requestFocus()
                analyticsHelper().setEvent(SAVE_ENROLL, CLICK, SAVE_ENROLL)
                presenter.finish(mode)
            }
        }

        binding.fieldRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    adapter.setLastFocusItem(null)
                    val imm = context!!.getSystemService(
                            Activity.INPUT_METHOD_SERVICE
                    ) as InputMethodManager
                    imm.hideSoftInputFromWindow(recyclerView.windowToken, 0)
                    binding.root.requestFocus()
                    presenter.clearLastFocusItem()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()
        presenter.init()
    }

    override fun onPause() {
        super.onPause()
        presenter.onDettach()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            RQ_INCIDENT_GEOMETRY, RQ_ENROLLMENT_GEOMETRY -> {
                if (resultCode == Activity.RESULT_OK) {
                    handleGeometry(
                            FeatureType.valueOfFeatureType(
                                    data!!.getStringExtra(MapSelectorActivity.LOCATION_TYPE_EXTRA)
                            ),
                            data.getStringExtra(MapSelectorActivity.DATA_EXTRA), requestCode
                    )
                }
            }
            Constants.GALLERY_REQUEST -> {
                if (resultCode == Activity.RESULT_OK) {
                    try {
                        val imageUri = data?.data
                        presenter.saveValue(
                                uuid,
                                FileResourcesUtil.getFileFromGallery(this, imageUri).path
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
                    }
                }
            }
            Constants.CAMERA_REQUEST -> {
                if (resultCode == Activity.RESULT_OK) {
                    val file = File(
                            FileResourceDirectoryHelper.getFileResourceDirectory(this),
                            "tempFile.png"
                    )
                    if (file.exists()) {
                        presenter.saveValue(uuid, file.path)
                    } else {
                        presenter.saveValue(uuid, null)
                    }
                }
            }
            RQ_EVENT -> openDashboard(presenter.getEnrollment().uid()!!)
        }
    }

    override fun openEvent(eventUid: String) {
        if (presenter.openInitial(eventUid)) {
            val bundle = EventInitialActivity.getBundle(
                    presenter.getProgram().uid(),
                    eventUid,
                    null,
                    presenter.getEnrollment().trackedEntityInstance(),
                    null,
                    presenter.getEnrollment().organisationUnit(),
                    null,
                    presenter.getEnrollment().uid(),
                    0,
                    presenter.getEnrollment().status()
            )
            val eventInitialIntent = Intent(abstracContext, EventInitialActivity::class.java)
            eventInitialIntent.putExtras(bundle)
            startActivityForResult(eventInitialIntent, RQ_EVENT)
        } else {
            val eventCreationIntent = Intent(abstracContext, EventCaptureActivity::class.java)
            eventCreationIntent.putExtras(
                    EventCaptureActivity.getActivityBundle(eventUid, presenter.getProgram().uid())
            )
            eventCreationIntent.putExtra(
                    Constants.TRACKED_ENTITY_INSTANCE,
                    presenter.getEnrollment().trackedEntityInstance()
            )
            startActivityForResult(eventCreationIntent, RQ_EVENT)
        }
    }

    override fun openDashboard(enrollmentUid: String) {
        val bundle = Bundle()
        bundle.putString("PROGRAM_UID", presenter.getProgram().uid())
        bundle.putString("TEI_UID", presenter.getEnrollment().trackedEntityInstance())
        startActivity(TeiDashboardMobileActivity::class.java, bundle, true, false, null)
    }

    override fun rowActions(): Flowable<RowAction> {
        return adapter.asFlowable()
    }

    override fun goBack() {
        onBackPressed()
    }

    override fun showMissingMandatoryFieldsMessage() {
        showInfoDialog(
                getString(R.string.unable_to_complete),
                getString(R.string.missing_mandatory_fields)
        )
    }

    override fun showErrorFieldsMessage() {
        showInfoDialog(
                getString(R.string.unable_to_complete),
                getString(R.string.field_errors)
        )
    }

    override fun onBackPressed() {
        if (mode == EnrollmentMode.CHECK) {
            if (presenter.dataIntegrityCheck(adapter.mandatoryOk(), adapter.hasError())) {
                super.onBackPressed()
            }
        } else {
            showDeleteDialog()
        }
    }

    private fun showDeleteDialog() {
        CustomDialog(
                this,
                getString(R.string.title_delete_go_back),
                getString(R.string.delete_go_back),
                getString(R.string.cancel),
                getString(R.string.missing_mandatory_fields_go_back),
                RQ_GO_BACK,
                object : DialogClickListener {
                    override fun onPositive() {
                        // do nothing
                    }

                    override fun onNegative() {
                        analyticsHelper().setEvent(DELETE_AND_BACK, CLICK, DELETE_AND_BACK)
                        presenter.deleteAllSavedData()
                        finish()
                    }
                }
        )
                .show()
    }

    private fun handleGeometry(featureType: FeatureType, dataExtra: String, requestCode: Int) {
        val geometry: Geometry? =
                when (featureType) {
                    FeatureType.POINT -> {
                        val type = object : TypeToken<List<Double>>() {}.type
                        GeometryHelper.createPointGeometry(Gson().fromJson(dataExtra, type))
                    }
                    FeatureType.POLYGON -> {
                        val type = object : TypeToken<List<List<List<Double>>>>() {}.type
                        GeometryHelper.createPolygonGeometry(Gson().fromJson(dataExtra, type))
                    }
                    FeatureType.MULTI_POLYGON -> {
                        val type = object : TypeToken<List<List<List<List<Double>>>>>() {}.type
                        GeometryHelper.createMultiPolygonGeometry(Gson().fromJson(dataExtra, type))
                    }
                    else -> null
                }

        if (geometry != null) {
            when (requestCode) {
                RQ_ENROLLMENT_GEOMETRY -> presenter.saveEnrollmentGeometry(geometry)
                RQ_INCIDENT_GEOMETRY -> presenter.saveTeiGeometry(geometry)
            }
        }
    }

    /*endregion*/

    /*region TEI*/
    override fun displayTeiInfo(it: List<TrackedEntityAttributeValue>) {
        binding.title.text =
                if (mode != EnrollmentMode.NEW) {
                    it.map { it.value() }.joinToString(separator = " ", limit = 3)
                } else {
                    String.format(getString(R.string.enroll_in), presenter.getProgram().displayName())
                }
    }
    /*endregion*/
    /*region ACCESS*/

    override fun setAccess(access: Boolean?) {
        binding.coordinatesView.setEditable(access)
        binding.teiCoordinatesView.setEditable(access)
        if (access == false) {
            binding.next.visibility = View.GONE
        }
    }
    /*endregion*/

    /*region STATUS*/

    override fun renderStatus(status: EnrollmentStatus) {
        binding.enrollmentStatus = status
    }

    override fun showStatusOptions(currentStatus: EnrollmentStatus) {
        val popup = PopupMenu(this, binding.programLockLayout)
        popup.setOnMenuItemClickListener { item ->
            val newStatus = when (item.itemId) {
                R.id.deactivate -> EnrollmentStatus.CANCELLED
                R.id.complete -> EnrollmentStatus.COMPLETED
                R.id.activate -> EnrollmentStatus.ACTIVE
                R.id.reOpen -> EnrollmentStatus.ACTIVE
                else -> throw IllegalArgumentException("Can't have other option")
            }
            analyticsHelper().setEvent(STATUS_ENROLLMENT, newStatus.name, STATUS_ENROLLMENT)
            presenter.updateEnrollmentStatus(newStatus)
        }

        val menuId = when (currentStatus) {
            EnrollmentStatus.ACTIVE -> R.menu.tei_detail_options_active
            EnrollmentStatus.CANCELLED -> R.menu.tei_detail_options_cancelled
            EnrollmentStatus.COMPLETED -> R.menu.tei_detail_options_completed
        }
        popup.menuInflater.inflate(menuId, popup.menu)
        popup.show()
    }

    /*endregion*/

    /*region ORG UNIT*/

    override fun displayOrgUnit(ou: OrganisationUnit) {
        binding.orgUnitText.setText(ou.displayName())
        binding.orgUnitText.isEnabled = false
    }

    /*endregion*/

    /*region DATES*/

    override fun setDateLabels(enrollmentDateLabel: String?, indicendDateLabel: String?) {
        binding.incidentDateLayout.hint = indicendDateLabel ?: getString(R.string.incident_date)
        binding.reportDateLayout.hint = enrollmentDateLabel ?: getString(R.string.report_date)
    }

    override fun setUpIncidentDate(incidentDate: Date?) {
        binding.incidentDateLayout.visibility = View.VISIBLE
        binding.incidentDateText.setText(DateUtils.uiDateFormat().format(incidentDate))
    }

    override fun setUpEnrollmentDate(enrollmentDate: Date?) {
        binding.reportDate.setText(DateUtils.uiDateFormat().format(enrollmentDate))
    }

    override fun blockDates(blockEnrollmentDate: Boolean, blockIncidentDate: Boolean) {
        if (mode != EnrollmentMode.NEW) {
            binding.reportDate.isEnabled = !blockEnrollmentDate
            binding.incidentDateText.isEnabled = !blockIncidentDate
        }
    }

    override fun onReportDateClick() {
        showCalendar(
                presenter.getEnrollment().enrollmentDate(),
                presenter.getOrgUnit().openingDate(),
                presenter.getOrgUnit().closedDate(),
                binding.reportDateLayout.hint.toString(),
                presenter.getProgram().selectEnrollmentDatesInFuture() ?: false,
                object : DatePickerUtils.OnDatePickerClickListener {
                    override fun onNegativeClick() {
                        val date = Date()
                        presenter.updateEnrollmentDate(date)
                    }

                    override fun onPositiveClick(datePicker: DatePicker) {
                        val calendar = Calendar.getInstance()
                        calendar.set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
                        presenter.updateEnrollmentDate(calendar.time)
                    }
                }
        )
    }

    override fun onIncidentDateClick() {
        showCalendar(
                presenter.getEnrollment().incidentDate(),
                presenter.getOrgUnit().openingDate(),
                presenter.getOrgUnit().closedDate(),
                binding.incidentDateLayout.hint.toString(),
                presenter.getProgram().selectIncidentDatesInFuture() ?: false,
                object : DatePickerUtils.OnDatePickerClickListener {
                    override fun onNegativeClick() {
                        val date = Date()
                        presenter.updateIncidentDate(date)
                    }

                    override fun onPositiveClick(datePicker: DatePicker) {
                        val calendar = Calendar.getInstance()
                        calendar.set(datePicker.year, datePicker.month, datePicker.dayOfMonth)
                        presenter.updateIncidentDate(calendar.time)
                    }
                }
        )
    }

    override fun showCalendar(
            date: Date?,
            minDate: Date?,
            maxDate: Date?,
            label: String,
            allowFuture: Boolean,
            listener: DatePickerUtils.OnDatePickerClickListener
    ) {
        DatePickerUtils.getDatePickerDialog(
                this, label, date, minDate, maxDate, allowFuture,
                listener
        ).show()
    }

    /*endregion*/

    /*region GEOMETRY*/
    override fun displayEnrollmentCoordinates(
            enrollmentCoordinatesData: Pair<Program, Enrollment>?
    ) {
        val featureType = enrollmentCoordinatesData?.first?.featureType()
        val geometry = enrollmentCoordinatesData?.second?.geometry()

        binding.coordinatesView.visibility =
                if (featureType != null && featureType != FeatureType.NONE) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

        binding.coordinatesView.setLabel(getString(R.string.enrollment_coordinates))

        binding.coordinatesView.featureType = featureType
        binding.coordinatesView.updateLocation(geometry)

        binding.coordinatesView.setMapListener {
            startActivityForResult(
                    MapSelectorActivity.create(this, it.featureType, it.currentCoordinates()),
                    RQ_ENROLLMENT_GEOMETRY
            )
        }
        binding.coordinatesView.setCurrentLocationListener {
            presenter.saveEnrollmentGeometry(it)
        }
    }

    override fun displayTeiCoordinates(
            teiCoordinatesData: Pair<TrackedEntityType, TrackedEntityInstance>?
    ) {
        binding.teiCoordinatesView.visibility =
                if (teiCoordinatesData!!.first.featureType() != FeatureType.NONE) {
                    View.VISIBLE
                } else {
                    View.GONE
                }

        binding.teiCoordinatesView.setLabel(
                "${getString(R.string.tei_coordinates)} ${teiCoordinatesData.first.displayName()}"
        )

        binding.teiCoordinatesView.featureType = teiCoordinatesData.first.featureType()
        binding.teiCoordinatesView.updateLocation(teiCoordinatesData.second.geometry())

        binding.teiCoordinatesView.setMapListener {
            startActivityForResult(
                    MapSelectorActivity.create(this, it.featureType, it.currentCoordinates()),
                    RQ_INCIDENT_GEOMETRY
            )
        }
        binding.teiCoordinatesView.setCurrentLocationListener {
            presenter.saveTeiGeometry(it)
        }
    }

    /*endregion*/

    /*region DATA ENTRY*/
    override fun showFields(fields: List<FieldViewModel>) {
        if (!isEmpty(presenter.getLastFocusItem())) {
            adapter.setLastFocusItem(presenter.getLastFocusItem())
        }

        fields.filter {
            it !is DisplayViewModel
        }

        val myLayoutManager: LinearLayoutManager = binding.fieldRecycler.layoutManager as LinearLayoutManager
        val myFirstPositionIndex = myLayoutManager.findFirstVisibleItemPosition()
        val myFirstPositionView = myLayoutManager.findViewByPosition(myFirstPositionIndex)

        var offset = 0
        myFirstPositionView?.let {
            offset = it.top
        }

        adapter.swap(fields)

        myLayoutManager.scrollToPositionWithOffset(myFirstPositionIndex, offset)
    }
    /*endregion*/
}
