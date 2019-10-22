package org.dhis2.usescases.main

import androidx.annotation.UiThread
import io.reactivex.functions.Consumer
import org.dhis2.usescases.general.AbstractActivityContracts
import org.dhis2.utils.filters.FilterManager

class MainContracts {

    interface View : AbstractActivityContracts.View {

        @UiThread
        fun renderUsername(): Consumer<String>

        fun openDrawer(gravity: Int)

        fun showHideFilter()

        fun onLockClick()

        fun changeFragment(id: Int)

        fun updateFilters(totalFilters: Int)

        fun showPeriodRequest(periodRequest: FilterManager.PeriodRequest)
    }
}
