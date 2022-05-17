package org.dhis2.data.forms.dataentry

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import org.dhis2.data.location.LocationProvider
import org.dhis2.form.data.DataIntegrityCheckResult
import org.dhis2.form.data.FormRepository
import org.dhis2.form.model.DispatcherProvider
import org.dhis2.form.model.RowAction
import org.dhis2.usescases.enrollment.provider.EnrollmentResultDialogUiProvider

class FormViewFragmentFactory(
    val formRepository: FormRepository,
    val locationProvider: LocationProvider?,
    private val onItemChangeListener: ((action: RowAction) -> Unit)?,
    private val needToForceUpdate: Boolean = false,
    private val onLoadingListener: ((loading: Boolean) -> Unit)?,
    private val onFocused: (() -> Unit)?,
    private val onFinishDataEntry: (() -> Unit)?,
    private val onActivityForResult: (() -> Unit)?,
    private val completionListener: ((percentage: Float) -> Unit)?,
    private val onDataIntegrityCheck: ((result: DataIntegrityCheckResult) -> Unit)?,
    private val onFieldItemsRendered: ((fieldsEmpty: Boolean) -> Unit)?,
    private val resultDialogUiProvider: EnrollmentResultDialogUiProvider?,
    val dispatchers: DispatcherProvider
) : FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            FormView::class.java.name -> FormView().apply {
                setFormConfiguration(
                    formRepository = formRepository,
                    dispatchers = dispatchers
                )
                setCallbackConfiguration(
                    onItemChangeListener = onItemChangeListener,
                    onLoadingListener = onLoadingListener,
                    onFocused = onFocused,
                    onFinishDataEntry = onFinishDataEntry,
                    onActivityForResult = onActivityForResult,
                    onDataIntegrityCheck = onDataIntegrityCheck,
                    onFieldItemsRendered = onFieldItemsRendered
                )
                setConfiguration(
                    locationProvider = locationProvider,
                    needToForceUpdate = needToForceUpdate,
                    completionListener = completionListener,
                    resultDialogUiProvider = resultDialogUiProvider
                )
            }

            else -> super.instantiate(classLoader, className)
        }
    }
}
