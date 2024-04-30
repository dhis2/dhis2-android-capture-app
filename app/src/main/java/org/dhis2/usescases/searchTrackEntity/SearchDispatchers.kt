package org.dhis2.usescases.searchTrackEntity

import kotlinx.coroutines.Dispatchers
import org.dhis2.commons.viewmodel.DispatcherProvider

class SearchDispatchers : DispatcherProvider {
    override fun io() = Dispatchers.IO

    override fun computation() = Dispatchers.Default

    override fun ui() = Dispatchers.Main
}
