package org.dhis2.android.rtsm.coroutines

import kotlinx.coroutines.Dispatchers
import org.dhis2.commons.viewmodel.DispatcherProvider
import javax.inject.Inject

class StockDispatcherProvider @Inject constructor() : DispatcherProvider {

    override fun io() = Dispatchers.IO

    override fun computation() = Dispatchers.Default

    override fun ui() = Dispatchers.Main
}
