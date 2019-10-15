package org.dhis2.usescases.main.program

import androidx.annotation.UiThread
import io.reactivex.functions.Consumer
import org.dhis2.usescases.general.AbstractActivityContracts

/**
 * QUADRAM. Created by ppajuelo on 18/10/2017.
 */
class ProgramContract {

    interface View : AbstractActivityContracts.View {

        fun swapProgramModelData(): Consumer<List<ProgramViewModel>>

        fun showFilterProgress()

        @UiThread
        fun renderError(message: String)

        fun openOrgUnitTreeSelector()

        fun showHideFilter()

        fun clearFilters()
    }

    interface Presenter {
        fun init(view: View)

        fun onItemClick(programModel: ProgramViewModel)

        fun showDescription(description: String)

        fun dispose()

        fun onSyncStatusClick(program: ProgramViewModel)

        fun showHideFilterClick()

        fun clearFilterClick()
    }
}
