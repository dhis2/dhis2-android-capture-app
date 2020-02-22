package org.dhis2.usescases.enrollment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils.isEmpty
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Flowable
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
import org.dhis2.utils.Constants.CAMERA_REQUEST
import org.dhis2.utils.Constants.GALLERY_REQUEST
import org.dhis2.utils.Constants.RQ_QR_SCANNER
import org.dhis2.utils.DialogClickListener
import org.dhis2.utils.EventMode
import org.dhis2.utils.FileResourcesUtil
import org.dhis2.utils.analytics.CLICK
import org.dhis2.utils.analytics.DELETE_AND_BACK
import org.dhis2.utils.analytics.SAVE_ENROLL
import org.dhis2.utils.customviews.CustomDialog
import org.dhis2.utils.recyclers.StickyHeaderItemDecoration
import org.hisp.dhis.android.core.arch.helpers.FileResourceDirectoryHelper
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import java.io.File
import javax.inject.Inject

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
                intent.getStringExtra(PROGRAM_UID_EXTRA),
                EnrollmentMode.valueOf(intent.getStringExtra(MODE_EXTRA))
            )
        ).inject(this)
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.enrollment_activity)
        binding.view = this

        mode = EnrollmentMode.valueOf(intent.getStringExtra(MODE_EXTRA))

        adapter = DataEntryAdapter(
            LayoutInflater.from(this),
            supportFragmentManager,
            DataEntryArguments.forEnrollment(intent.getStringExtra(ENROLLMENT_UID_EXTRA))
        )
        binding.fieldRecycler.addItemDecoration(
            StickyHeaderItemDecoration(binding.fieldRecycler,
                false,
                { itemPosition ->
                    itemPosition >= 0 &&
                            itemPosition < adapter.itemCount &&
                            adapter.getItemViewType(itemPosition) == 17
                },
                { sectionUid ->
                    adapter.sectionFlowable().onNext(sectionUid)
                })
        )
        binding.fieldRecycler.adapter = adapter

        binding.next.setOnClickListener {
            if (presenter.dataIntegrityCheck(adapter.mandatoryOk(), adapter.hasError())) {
                binding.root.requestFocus()
                analyticsHelper().setEvent(SAVE_ENROLL, CLICK, SAVE_ENROLL)
                presenter.finish(mode)
            }
        }

        presenter.init()
    }

    override fun onDestroy() {
        presenter.onDettach()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RQ_INCIDENT_GEOMETRY, RQ_ENROLLMENT_GEOMETRY -> {
                    handleGeometry(
                        FeatureType.valueOfFeatureType(
                            data!!.getStringExtra(MapSelectorActivity.LOCATION_TYPE_EXTRA)
                        ),
                        data.getStringExtra(MapSelectorActivity.DATA_EXTRA), requestCode
                    )
                }
                GALLERY_REQUEST -> {
                    try {
                        val imageUri = data?.data
                        presenter.saveFile(
                            uuid,
                            FileResourcesUtil.getFileFromGallery(this, imageUri).path
                        )
                        presenter.updateFields()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show()
                    }
                }
                CAMERA_REQUEST -> {
                    val file = File(
                        FileResourceDirectoryHelper.getFileResourceDirectory(this),
                        "tempFile.png"
                    )
                    presenter.saveFile(uuid, if (file.exists()) file.path else null)
                    presenter.updateFields()
                }
                RQ_QR_SCANNER -> {
                    scanTextView.updateScanResult(data!!.getStringExtra(Constants.EXTRA_DATA))
                }
                RQ_EVENT -> openDashboard(presenter.getEnrollment().uid()!!)
            }
        }
    }

    override fun sectionFlowable(): Flowable<String> {
        return adapter.sectionFlowable()
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
                EventCaptureActivity.getActivityBundle(
                    eventUid,
                    presenter.getProgram().uid(),
                    EventMode.CHECK
                )
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
                binding.root.requestFocus()
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
                RQ_ENROLLMENT_GEOMETRY -> {
                    presenter.saveEnrollmentGeometry(geometry)
                }
                RQ_INCIDENT_GEOMETRY -> {
                    presenter.saveTeiGeometry(geometry)
                }
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

        val myLayoutManager: LinearLayoutManager =
            binding.fieldRecycler.layoutManager as LinearLayoutManager
        val myFirstPositionIndex = myLayoutManager.findFirstVisibleItemPosition()
        val myFirstPositionView = myLayoutManager.findViewByPosition(myFirstPositionIndex)

        var offset = 0
        myFirstPositionView?.let {
            offset = it.top
        }

        adapter.swap(fields, { })

        myLayoutManager.scrollToPositionWithOffset(myFirstPositionIndex, offset)
    }
    /*endregion*/

    override fun setSaveButtonVisible(visible: Boolean) {
        if (visible) {
            binding.next.show()
        } else {
            binding.next.hide()
        }
    }

}
