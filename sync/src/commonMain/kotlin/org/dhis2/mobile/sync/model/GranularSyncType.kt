package org.dhis2.mobile.sync.model

import org.dhis2.mobile.sync.model.GranularSyncType.DataSet
import org.dhis2.mobile.sync.model.GranularSyncType.DataValue
import org.dhis2.mobile.sync.model.GranularSyncType.Event
import org.dhis2.mobile.sync.model.GranularSyncType.Program
import org.dhis2.mobile.sync.model.GranularSyncType.Tei

const val GRANULAR_SYNC_PROGRAM_NAME = "program"
const val GRANULAR_SYNC_TEI_NAME = "tei"
const val GRANULAR_SYNC_EVENT_NAME = "event"
const val GRANULAR_SYNC_DATASET_NAME = "dataset"
const val GRANULAR_SYNC_DATAVALUE_NAME = "datavalue"

sealed class GranularSyncType(
    val name: String,
) {
    data object Program : GranularSyncType(GRANULAR_SYNC_PROGRAM_NAME)

    data object Tei : GranularSyncType(GRANULAR_SYNC_TEI_NAME)

    data object Event : GranularSyncType(GRANULAR_SYNC_EVENT_NAME)

    data object DataSet : GranularSyncType(GRANULAR_SYNC_DATASET_NAME)

    data object DataValue : GranularSyncType(GRANULAR_SYNC_DATAVALUE_NAME)
}

internal fun fromName(name: String) =
    when (name) {
        GRANULAR_SYNC_PROGRAM_NAME -> Program
        GRANULAR_SYNC_TEI_NAME -> Tei
        GRANULAR_SYNC_EVENT_NAME -> Event
        GRANULAR_SYNC_DATASET_NAME -> DataSet
        GRANULAR_SYNC_DATAVALUE_NAME -> DataValue
        else -> error("Type %s is not available".format(name))
    }
