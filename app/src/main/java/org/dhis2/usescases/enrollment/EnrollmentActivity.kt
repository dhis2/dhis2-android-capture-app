package org.dhis2.usescases.enrollment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils.isEmpty
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.crashlytics.android.Crashlytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import io.reactivex.Flowable
import java.io.File
import javax.inject.Inject
import org.dhis2.App
import org.dhis2.Bindings.isKeyboardOpened
import org.dhis2.R
import org.dhis2.data.forms.dataentry.DataEntryAdapter
import org.dhis2.data.forms.dataentry.DataEntryArguments
import org.dhis2.data.forms.dataentry.fields.FieldViewModel
import org.dhis2.data.forms.dataentry.fields.RowAction
import org.dhis2.data.forms.dataentry.fields.display.DisplayViewModel
import org.dhis2.databinding.EnrollmentActivityBinding
import org.dhis2.uicomponents.map.views.MapSelectorActivity
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.eventsWithoutRegistration.eventInitial.EventInitialActivity
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.dhis2.utils.Constants
import org.dhis2.utils.Constants.CAMERA_REQUEST
import org.dhis2.utils.Constants.ENROLLMENT_UID
import org.dhis2.utils.Constants.GALLERY_REQUEST
import org.dhis2.utils.Constants.PROGRAM_UID
import org.dhis2.utils.Constants.RQ_QR_SCANNER
import org.dhis2.utils.Constants.TEI_UID
import org.dhis2.utils.EventMode
import org.dhis2.utils.FileResourcesUtil
import org.dhis2.utils.customviews.AlertBottomDialog
import org.dhis2.utils.recyclers.StickyHeaderItemDecoration
import org.hisp.dhis.android.core.arch.helpers.FileResourceDirectoryHelper
import org.hisp.dhis.android.core.arch.helpers.GeometryHelper
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.Geometry
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus

class EnrollmentActivity : ActivityGlobalAbstract(), EnrollmentView {

    enum class EnrollmentMode { NEW, CHECK }

    private var forRelationship: Boolean = false
    @Inject
    lateinit var presenter: EnrollmentPresenterImpl

    lateinit var binding: EnrollmentActivityBinding
    lateinit var mode: EnrollmentMode

    companion object {
        const val ENROLLMENT_UID_EXTRA = "ENROLLMENT_UID_EXTRA"
        const val PROGRAM_UID_EXTRA = "PROGRAM_UID_EXTRA"
        const val MODE_EXTRA = "MODE_EXTRA"
        const val FOR_RELATIONSHIP = "FOR_RELATIONSHIP"
        const val RQ_ENROLLMENT_GEOMETRY = 1023
        const val RQ_INCIDENT_GEOMETRY = 1024
        const val RQ_EVENT = 1025
        const val RQ_GO_BACK = 1026

        fun getIntent(
            context: Context,
            enrollmentUid: String,
            programUid: String,
            enrollmentMode: EnrollmentMode,
            forRelationship: Boolean? = false
        ): Intent {
            val intent = Intent(context, EnrollmentActivity::class.java)
            intent.putExtra(ENROLLMENT_UID_EXTRA, enrollmentUid)
            intent.putExtra(PROGRAM_UID_EXTRA, programUid)
            intent.putExtra(MODE_EXTRA, enrollmentMode.name)
            intent.putExtra(FOR_RELATIONSHIP, forRelationship)
            if (forRelationship == true) {
                intent.addFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT)
            }
            return intent
        }
    }

    private lateinit var adapter: DataEntryAdapter

    /*region LIFECYCLE*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (applicationContext as App).userComponent()!!.plus(
            EnrollmentModule(
                this,
                intent.getStringExtra(ENROLLMENT_UID_EXTRA),
                intent.getStringExtra(PROGRAM_UID_EXTRA),
                EnrollmentMode.valueOf(intent.getStringExtra(MODE_EXTRA))
            )
        ).inject(this)
        forRelationship = intent.getBooleanExtra(FOR_RELATIONSHIP, false)
        binding = DataBindingUtil.setContentView(this, R.layout.enrollment_activity)
        binding.view = this

        mode = EnrollmentMode.valueOf(intent.getStringExtra(MODE_EXTRA))

        adapter = DataEntryAdapter(
            LayoutInflater.from(this),
            supportFragmentManager,
            DataEntryArguments.forEnrollment(intent.getStringExtra(ENROLLMENT_UID_EXTRA))
        )
        binding.fieldRecycler.addItemDecoration(
            StickyHeaderItemDecoration(
                binding.fieldRecycler,
                false
            ) { itemPosition ->
                itemPosition >= 0 &&
                    itemPosition < adapter.itemCount &&
                    adapter.getItemViewType(itemPosition) == adapter.sectionViewType()
            }
        )
        binding.fieldRecycler.adapter = adapter

        binding.save.setOnClickListener {
            performSaveClick()
        }

        presenter.init()
    }

    override fun onResume() {
        presenter.subscribeToBackButton()
        super.onResume()
    }

    override fun onDestroy() {
        presenter.onDettach()
        super.onDestroy()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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
                        Toast.makeText(
                            this, getString(R.string.something_wrong), Toast.LENGTH_LONG
                        ).show()
                    }
                }
                CAMERA_REQUEST -> {
                    val file = File(
                        FileResourceDirectoryHelper.getFileResourceDirectory(this),
                        "tempFile.png"
                    )
                    try {
                        presenter.saveFile(uuid, if (file.exists()) file.path else null)
                        presenter.updateFields()
                    } catch (e: Exception) {
                        Crashlytics.logException(e)
                        Toast.makeText(
                            this, getString(R.string.something_wrong), Toast.LENGTH_LONG
                        ).show()
                    }
                }
                RQ_QR_SCANNER -> {
                    scanTextView.updateScanResult(data!!.getStringExtra(Constants.EXTRA_DATA))
                }
                RQ_EVENT -> openDashboard(presenter.getEnrollment().uid()!!)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun sectionFlowable(): Flowable<String> {
        return adapter.sectionFlowable()
    }

    override fun setSelectedSection(selectedSection: String) {
        adapter.setCurrentSection(selectedSection)
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
        if (forRelationship) {
            val intent = Intent()
            intent.putExtra("TEI_A_UID", presenter.getEnrollment().trackedEntityInstance())
            setResult(Activity.RESULT_OK, intent)
            finish()
        } else {
            val bundle = Bundle()
            bundle.putString(PROGRAM_UID, presenter.getProgram().uid())
            bundle.putString(TEI_UID, presenter.getEnrollment().trackedEntityInstance())
            bundle.putString(ENROLLMENT_UID, enrollmentUid)
            startActivity(TeiDashboardMobileActivity::class.java, bundle, true, false, null)
        }
    }

    override fun rowActions(): Flowable<RowAction> {
        return adapter.asFlowable()
    }

    override fun goBack() {
        onBackPressed()
    }

    override fun showMissingMandatoryFieldsMessage(
        emptyMandatoryFields: MutableMap<String, String>
    ) {
        AlertBottomDialog.instance
            .setTitle(getString(R.string.unable_to_complete))
            .setMessage(getString(R.string.missing_mandatory_fields))
            .setEmptyMandatoryFields(emptyMandatoryFields.keys.toList())
            .show(supportFragmentManager, AlertBottomDialog::class.java.simpleName)
    }

    override fun showErrorFieldsMessage(errorFields: List<String>) {
        AlertBottomDialog.instance
            .setTitle(getString(R.string.unable_to_complete))
            .setMessage(getString(R.string.field_errors))
            .setEmptyMandatoryFields(errorFields)
            .show(supportFragmentManager, AlertBottomDialog::class.java.simpleName)
    }

    override fun onBackPressed() {
        if (!isKeyboardOpened()) {
            if (mode == EnrollmentMode.CHECK) {
                presenter.backIsClicked()
            } else {
                showDeleteDialog()
            }
        } else {
            currentFocus?.apply { clearFocus() }
            hideKeyboard()
        }
    }

    private fun showDeleteDialog() {
        AlertBottomDialog.instance
            .setTitle(getString(R.string.title_delete_go_back))
            .setMessage(getString(R.string.delete_go_back))
            .setPositiveButton(getString(R.string.missing_mandatory_fields_go_back)) {
                presenter.deleteAllSavedData()
                finish()
            }
            .setNegativeButton()
            .show(supportFragmentManager, AlertBottomDialog::class.java.simpleName)
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

    override fun setResultAndFinish() {
        setResult(RESULT_OK)
        finish()
    }

    /*endregion*/

    /*region TEI*/
    override fun displayTeiInfo(attrList: List<String>, profileImage: String) {
        if (mode != EnrollmentMode.NEW) {
            binding.title.visibility = View.GONE
            binding.teiDataHeader.root.visibility = View.VISIBLE

            val attrListNotEmpty = attrList.filter { it.isNotEmpty() }
            binding.teiDataHeader.mainAttributes.apply {
                when (attrListNotEmpty.size) {
                    0 -> visibility = View.GONE
                    1 -> text = attrListNotEmpty[0]
                    else -> text = String.format("%s %s", attrListNotEmpty[0], attrListNotEmpty[1])
                }
                setTextColor(Color.WHITE)
            }
            binding.teiDataHeader.secundaryAttribute.apply {
                when (attrListNotEmpty.size) {
                    0, 1, 2 -> visibility = View.GONE
                    else -> text = attrListNotEmpty[2]
                }
                setTextColor(Color.WHITE)
            }

            if (profileImage.isEmpty()) {
                binding.teiDataHeader.teiImage.visibility = View.GONE
                binding.teiDataHeader.imageSeparator.visibility = View.GONE
            } else {
                Glide.with(this).load(File(profileImage))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .transform(CircleCrop())
                    .into(binding.teiDataHeader.teiImage)
            }
        } else {
            binding.title.visibility = View.VISIBLE
            binding.teiDataHeader.root.visibility = View.GONE
            binding.title.text =
                String.format(getString(R.string.enroll_in), presenter.getProgram().displayName())
        }
    }
    /*endregion*/
    /*region ACCESS*/

    override fun setAccess(access: Boolean?) {
        if (access == false) {
            binding.save.visibility = View.GONE
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

        adapter.swap(fields) { }

        myLayoutManager.scrollToPositionWithOffset(myFirstPositionIndex, offset)
    }

    /*endregion*/
    override fun requestFocus() {
        binding.root.requestFocus()
    }

    override fun setSaveButtonVisible(visible: Boolean) {
        if (visible) {
            binding.save.show()
        } else {
            binding.save.hide()
        }
    }

    override fun performSaveClick() {
        if (presenter.dataIntegrityCheck()) {
            presenter.finish(mode)
        }
    }
}
