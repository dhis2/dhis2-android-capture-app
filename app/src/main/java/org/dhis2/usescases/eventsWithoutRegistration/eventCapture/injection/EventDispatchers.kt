package org.dhis2.usescases.eventsWithoutRegistration.eventCapture.injection

import kotlinx.coroutines.Dispatchers
import org.dhis2.commons.viewmodel.DispatcherProvider

class EventDispatchers : DispatcherProvider {
    override fun io() = Dispatchers.IO

    override fun computation() = Dispatchers.Default

    override fun ui() = Dispatchers.Main
}
