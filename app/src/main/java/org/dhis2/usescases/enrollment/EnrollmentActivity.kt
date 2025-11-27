package org.dhis2.usescases.enrollment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.dhis2.App
import org.dhis2.R
import org.dhis2.commons.Constants.ENROLLMENT_UID
import org.dhis2.commons.Constants.PROGRAM_UID
import org.dhis2.commons.Constants.TEI_UID
import org.dhis2.commons.data.TeiAttributesInfo
import org.dhis2.commons.dialogs.bottomsheet.BottomSheetDialog
import org.dhis2.commons.dialogs.bottomsheet.BottomSheetDialogUiModel
import org.dhis2.commons.dialogs.bottomsheet.DialogButtonStyle
import org.dhis2.commons.dialogs.imagedetail.ImageDetailActivity
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.databinding.EnrollmentActivityBinding
import org.dhis2.form.data.GeometryController
import org.dhis2.form.data.GeometryParserImpl
import org.dhis2.form.model.EventMode
import org.dhis2.form.ui.FormView
import org.dhis2.maps.views.MapSelectorActivity
import org.dhis2.usescases.events.ScheduledEventActivity
import org.dhis2.usescases.eventsWithoutRegistration.eventCapture.EventCaptureActivity
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.dhis2.usescases.teiDashboard.TeiDashboardMobileActivity
import org.dhis2.utils.granularsync.OPEN_ERROR_LOCATION
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import javax.inject.Inject

class EnrollmentActivity :
    ActivityGlobalAbstract(),
    EnrollmentView {
    enum class EnrollmentMode { NEW, CHECK }

    private var forRelationship: Boolean = false
    private lateinit var formView: FormView

    @Inject
    lateinit var resourceManager: ResourceManager

    @Inject
    lateinit var presenter: EnrollmentPresenterImpl

    @Inject
    lateinit var dateEditionWarningHandler: DateEditionWarningHandler

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

        fun getIntent(
            context: Context,
            enrollmentUid: String,
            programUid: String,
            enrollmentMode: EnrollmentMode,
            forRelationship: Boolean? = false,
        ): Intent {
            val intent = Intent(context, EnrollmentActivity::class.java)
            intent.putExtra(ENROLLMENT_UID_EXTRA, enrollmentUid)
            intent.putExtra(PROGRAM_UID_EXTRA, programUid)
            intent.putExtra(MODE_EXTRA, enrollmentMode.name)
            intent.putExtra(FOR_RELATIONSHIP, forRelationship)
            return intent
        }
    }

    // region LIFECYCLE

    override fun onCreate(savedInstanceState: Bundle?) {
        val enrollmentUid = intent.getStringExtra(ENROLLMENT_UID_EXTRA) ?: ""
        val programUid = intent.getStringExtra(PROGRAM_UID_EXTRA) ?: ""
        val enrollmentMode =
            intent.getStringExtra(MODE_EXTRA)?.let { EnrollmentMode.valueOf(it) }
                ?: EnrollmentMode.NEW
        val openErrorLocation = intent.getBooleanExtra(OPEN_ERROR_LOCATION, false)
        (applicationContext as App)
            .userComponent()
            ?.plus(
                EnrollmentModule(
                    this,
                    enrollmentUid,
                    programUid,
                    enrollmentMode,
                    context,
                ),
            )?.inject(this)

        super.onCreate(savedInstanceState)

        if (presenter.getEnrollment() == null ||
            presenter.getEnrollment()?.trackedEntityInstance() == null
        ) {
            finish()
        }

        forRelationship = intent.getBooleanExtra(FOR_RELATIONSHIP, false)
        mode = enrollmentMode

        binding = DataBindingUtil.setContentView(this, R.layout.enrollment_activity)
        binding.view = this

        formView =
            buildEnrollmentForm(
                config =
                    EnrollmentFormBuilderConfig(
                        enrollmentUid = enrollmentUid,
                        programUid = programUid,
                        enrollmentMode =
                            org.dhis2.form.model.EnrollmentMode.valueOf(
                                enrollmentMode.name,
                            ),
                        hasWriteAccess = presenter.hasWriteAccess(),
                        openErrorLocation = openErrorLocation,
                        containerId = R.id.formViewContainer,
                        loadingView = binding.toolbarProgress,
                        saveButton = binding.save,
                    ),
                locationProvider = locationProvider,
                dateEditionWarningHandler = dateEditionWarningHandler,
            ) {
                presenter.finish(enrollmentMode)
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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?,
    ) {
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                RQ_INCIDENT_GEOMETRY, RQ_ENROLLMENT_GEOMETRY -> {
                    if (data?.hasExtra(MapSelectorActivity.DATA_EXTRA) == true) {
                        data.getStringExtra(MapSelectorActivity.DATA_EXTRA)?.let {
                            handleGeometry(
                                FeatureType.valueOfFeatureType(
                                    data.getStringExtra(MapSelectorActivity.LOCATION_TYPE_EXTRA)!!,
                                )!!,
                                it,
                                requestCode,
                            )
                        }
                    }
                }

                RQ_EVENT -> presenter.getEnrollment()?.uid()?.let { openDashboard(it) }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun openEvent(eventUid: String) {
        val suggestedEventDateIsNotFutureDate =
            presenter.suggestedReportDateIsNotFutureDate(eventUid)
        if (presenter.isEventScheduleOrSkipped(eventUid) && suggestedEventDateIsNotFutureDate) {
            val scheduleEventIntent = ScheduledEventActivity.getIntent(this, eventUid)
            openEventForResult.launch(scheduleEventIntent)
        } else if (suggestedEventDateIsNotFutureDate) {
            val eventCreationIntent = Intent(abstracContext, EventCaptureActivity::class.java)
            eventCreationIntent.putExtras(
                EventCaptureActivity.getActivityBundle(
                    eventUid,
                    presenter.getProgram()?.uid() ?: "",
                    EventMode.NEW,
                ),
            )
            startActivityForResult(eventCreationIntent, RQ_EVENT)
        } else {
            openDashboard(presenter.getEnrollment()?.uid()!!)
        }
    }

    private val openEventForResult =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
        ) {
            presenter.getEnrollment()?.uid()?.let { it1 -> openDashboard(it1) }
        }

    override fun openDashboard(enrollmentUid: String) {
        if (forRelationship) {
            val intent = Intent()
            intent.putExtra("TEI_A_UID", presenter.getEnrollment()?.trackedEntityInstance())
            setResult(Activity.RESULT_OK, intent)
            finish()
        } else {
            val bundle = Bundle()
            bundle.putString(PROGRAM_UID, presenter.getProgram()?.uid())
            bundle.putString(TEI_UID, presenter.getEnrollment()?.trackedEntityInstance())
            bundle.putString(ENROLLMENT_UID, enrollmentUid)
            startActivity(TeiDashboardMobileActivity::class.java, bundle, true, false, null)
        }
    }

    override fun goBack() {
        onBackPressed()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        attemptFinish()
    }

    private fun attemptFinish() {
        if (mode == EnrollmentMode.CHECK) {
            formView.onBackPressed()
        } else {
            showDeleteDialog()
        }
    }

    private fun showDeleteDialog() {
        BottomSheetDialog(
            bottomSheetDialogUiModel =
                BottomSheetDialogUiModel(
                    title = getString(R.string.not_saved),
                    message = getString(R.string.discard_go_back),
                    iconResource = R.drawable.ic_error_outline,
                    mainButton = DialogButtonStyle.MainButton(R.string.keep_editing),
                    secondaryButton = DialogButtonStyle.DiscardButton(),
                ),
            onSecondaryButtonClicked = {
                presenter.deleteAllSavedData()
                finish()
            },
            showTopDivider = true,
        ).show(supportFragmentManager, BottomSheetDialogUiModel::class.java.simpleName)
    }

    private fun handleGeometry(
        featureType: FeatureType,
        dataExtra: String,
        requestCode: Int,
    ) {
        val geometry =
            GeometryController(GeometryParserImpl()).generateLocationFromCoordinates(
                featureType,
                dataExtra,
            )

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

    // endregion

    // region TEI
    override fun displayTeiInfo(teiInfo: TeiAttributesInfo) {
        if (mode != EnrollmentMode.NEW) {
            binding.title.text =
                resourceManager.defaultEnrollmentLabel(
                    programUid = presenter.getProgram()?.uid(),
                    true,
                    1,
                )
        } else {
            binding.title.text =
                resourceManager.formatWithEnrollmentLabel(
                    programUid = presenter.getProgram()?.uid()!!,
                    R.string.new_enrollment,
                    1,
                )
        }
    }

    override fun displayTeiPicture(picturePath: String) {
        val intent =
            ImageDetailActivity.intent(
                context = this,
                title = null,
                imagePath = picturePath,
            )

        startActivity(intent)
    }
    // endregion
    // region ACCESS

    override fun setAccess(access: Boolean?) {
        if (access == false) {
            binding.save.visibility = View.GONE
        }
    }
    // endregion

    // region STATUS

    override fun renderStatus(status: EnrollmentStatus) {
        binding.enrollmentStatus = status
    }

    // endregion

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
        formView.onSaveClick()
    }

    override fun showDateEditionWarning(message: String?) {
        val dialog =
            MaterialAlertDialogBuilder(this, R.style.DhisMaterialDialog)
                .setMessage(message)
                .setPositiveButton(R.string.button_ok, null)
        dialog.show()
    }
}
