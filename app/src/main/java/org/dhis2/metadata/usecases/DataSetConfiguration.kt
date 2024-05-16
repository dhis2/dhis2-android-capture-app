package org.dhis2.metadata.usecases

import org.hisp.dhis.android.core.D2

class DataSetConfiguration(private val d2: D2) {

    fun getDataSet(dataSetUid: String) = d2.dataSetModule().dataSets().uid(dataSetUid).blockingGet()

    fun getDataSetStyle(programUid: String) = getDataSet(programUid)?.style()

    fun getDataSetIcon(programUid: String) = getDataSetStyle(programUid)?.icon()

    fun getDataSetColor(programUid: String) = getDataSetStyle(programUid)?.color()
}
