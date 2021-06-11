package org.dhis2.form.model.coroutine

import kotlinx.coroutines.Dispatchers
import org.dhis2.form.model.DispatcherProvider

class FormDispatcher : DispatcherProvider {
    override fun io() = Dispatchers.IO

    override fun computation() = Dispatchers.Default

    override fun ui() = Dispatchers.Main
}
