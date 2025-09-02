package org.dhis2.form.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import org.dhis2.commons.locationprovider.LocationProvider
import org.dhis2.form.model.RowAction

class FormViewFragmentFactory(
    val locationProvider: LocationProvider?,
    private val onItemChangeListener: ((action: RowAction) -> Unit)?,
    private val onLoadingListener: ((loading: Boolean) -> Unit)?,
    private val onFocused: (() -> Unit)?,
    private val onFinishDataEntry: (() -> Unit)?,
    private val onActivityForResult: (() -> Unit)?,
    private val completionListener: ((percentage: Float) -> Unit)?,
    private val onFieldItemsRendered: ((fieldsEmpty: Boolean) -> Unit)?,
    private val actionIconsActivate: Boolean = true,
    private val openErrorLocation: Boolean = false,
    private val programUid: String? = null,
) : FragmentFactory() {
    override fun instantiate(
        classLoader: ClassLoader,
        className: String,
    ): Fragment =
        when (className) {
            FormView::class.java.name ->
                FormView().apply {
                    setCallbackConfiguration(
                        onItemChangeListener = onItemChangeListener,
                        onLoadingListener = onLoadingListener,
                        onFocused = onFocused,
                        onFinishDataEntry = onFinishDataEntry,
                        onActivityForResult = onActivityForResult,
                        onFieldItemsRendered = onFieldItemsRendered,
                    )
                    setConfiguration(
                        locationProvider = locationProvider,
                        completionListener = completionListener,
                        actionIconsActivate = actionIconsActivate,
                        openErrorLocation = openErrorLocation,
                        programUid = programUid,
                    )
                }

            else -> super.instantiate(classLoader, className)
        }
}
