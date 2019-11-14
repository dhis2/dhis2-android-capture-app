package org.dhis2.usescases.datasets.dataSetTable

import org.dhis2.usescases.general.AbstractActivityContracts
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.dataset.DataSet

interface DataSetTableView : AbstractActivityContracts.View {

    val dataSetUid: String

    val orgUnitName: String

    fun setSections(sections: List<String>)

    fun accessDataWrite(): Boolean?

    fun showOptions()

    fun goToTable(numTable: Int)

    fun renderDetails(dataSet: DataSet, catcomboName: String)

    fun isDataSetOpen(dataSetIsOpen: Boolean)

    fun setDataSetState(state: State)

    fun showSyncDialog()
}
