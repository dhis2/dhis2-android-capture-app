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
import org.dhis2.form.ui.provider.FormResultDialogProvider

fun AppCompatActivity.buildEnrollmentForm(
    enrollmentUid: String,
    programUid: String,
    enrollmentMode: EnrollmentMode,
    hasWriteAccess: Boolean,
    locationProvider: LocationProvider,
    dateEditionWarningHandler: DateEditionWarningHandler,
    enrollmentResultDialogProvider: FormResultDialogProvider,
    openErrorLocation: Boolean,
    @IdRes containerId: Int,
    loadingView: ContentLoadingProgressBar,
    saveButton: FloatingActionButton,
    onFinish: () -> Unit,
): FormView {
    return FormView.Builder()
        .locationProvider(locationProvider)
        .onItemChangeListener { action ->
            dateEditionWarningHandler.shouldShowWarning(
                fieldUid = action.id,
                showWarning = ::showDateEditionWarning,
            )
        }
        .onLoadingListener { loading ->
            runOnUiThread {
                handleLoading(hasWriteAccess, loading, loadingView, saveButton)
            }
        }
        .onFinishDataEntry(onFinish)
        .eventCompletionResultDialogProvider(enrollmentResultDialogProvider)
        .factory(supportFragmentManager)
        .setRecords(
            EnrollmentRecords(
                enrollmentUid = enrollmentUid,
                enrollmentMode = enrollmentMode,
            ),
        )
        .openErrorLocation(openErrorLocation)
        .setProgramUid(programUid)
        .build().also { formView ->

            saveButton.setOnClickListener { formView.onSaveClick() }

            val fragmentTransition = supportFragmentManager.beginTransaction()
            fragmentTransition.replace(
                containerId,
                formView,
            )
            fragmentTransition.commit()
        }
}

private fun AppCompatActivity.showDateEditionWarning(message: String) {
    val dialog = MaterialAlertDialogBuilder(this, R.style.DhisMaterialDialog)
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

private fun handleSaveButtonVisibility(hasWriteAccess: Boolean, saveButton: FloatingActionButton) {
    if (hasWriteAccess) {
        saveButton.show()
    } else {
        saveButton.hide()
    }
}
