package org.dhis2.usescases.enrollment

import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.ContentLoadingProgressBar
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.dhis2.R
import org.dhis2.commons.locationprovider.LocationProvider
import org.dhis2.form.model.EnrollmentMode
import org.dhis2.form.model.EnrollmentRecords
import org.dhis2.form.ui.FormView

data class EnrollmentFormBuilderConfig(
    val enrollmentUid: String,
    val programUid: String,
    val enrollmentMode: EnrollmentMode,
    val hasWriteAccess: Boolean,
    val openErrorLocation: Boolean,
    @IdRes val containerId: Int,
    val loadingView: ContentLoadingProgressBar,
    val saveButton: FloatingActionButton,
)

fun AppCompatActivity.buildEnrollmentForm(
    config: EnrollmentFormBuilderConfig,
    locationProvider: LocationProvider,
    dateEditionWarningHandler: DateEditionWarningHandler,
    onFinish: () -> Unit,
): FormView =
    FormView
        .Builder()
        .locationProvider(locationProvider)
        .onItemChangeListener { action ->
            dateEditionWarningHandler.shouldShowWarning(
                fieldUid = action.id,
                showWarning = ::showDateEditionWarning,
            )
        }.onLoadingListener { loading ->
            runOnUiThread {
                handleLoading(
                    hasWriteAccess = config.hasWriteAccess,
                    loading = loading,
                    loadingView = config.loadingView,
                    saveButton = config.saveButton,
                )
            }
        }.onFinishDataEntry(onFinish)
        .factory(supportFragmentManager)
        .setRecords(
            EnrollmentRecords(
                enrollmentUid = config.enrollmentUid,
                enrollmentMode = config.enrollmentMode,
            ),
        ).openErrorLocation(config.openErrorLocation)
        .setProgramUid(config.programUid)
        .build()
        .also { formView ->

            config.saveButton.setOnClickListener { formView.onSaveClick() }

            val fragmentTransition = supportFragmentManager.beginTransaction()
            fragmentTransition.replace(
                config.containerId,
                formView,
            )
            fragmentTransition.commit()
        }

private fun AppCompatActivity.showDateEditionWarning(message: String) {
    val dialog =
        MaterialAlertDialogBuilder(this, R.style.DhisMaterialDialog)
            .setMessage(message)
            .setPositiveButton(R.string.button_ok, null)
    dialog.show()
}

private fun handleLoading(
    hasWriteAccess: Boolean,
    loading: Boolean,
    loadingView: ContentLoadingProgressBar,
    saveButton: FloatingActionButton,
) {
    if (loading) {
        loadingView.show()
    } else {
        loadingView.hide()
        handleSaveButtonVisibility(hasWriteAccess, saveButton)
    }
}

private fun handleSaveButtonVisibility(
    hasWriteAccess: Boolean,
    saveButton: FloatingActionButton,
) {
    if (hasWriteAccess) {
        saveButton.show()
    } else {
        saveButton.hide()
    }
}
