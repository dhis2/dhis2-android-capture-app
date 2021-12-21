package org.dhis2.data.forms.dataentry

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import org.dhis2.data.location.LocationProvider
import org.dhis2.form.data.FormRepository
import org.dhis2.form.model.DispatcherProvider
import org.dhis2.form.model.RowAction

class FormViewFragmentFactory(
    val formRepository: FormRepository,
    val locationProvider: LocationProvider?,
    private val onItemChangeListener: ((action: RowAction) -> Unit)?,
    private val needToForceUpdate: Boolean = false,
    private val onLoadingListener: ((loading: Boolean) -> Unit)?,
    private val onFocused: (() -> Unit)?,
    private val onActivityForResult: (() -> Unit)?,
    val dispatchers: DispatcherProvider
) : FragmentFactory() {
    override fun instantiate(classLoader: ClassLoader, className: String): Fragment {
        return when (className) {
            FormView::class.java.name -> FormView(
                formRepository,
                onItemChangeListener,
                locationProvider,
                onLoadingListener,
                onFocused,
                onActivityForResult,
                needToForceUpdate,
                dispatchers
            )
            else -> super.instantiate(classLoader, className)
        }
    }
}
