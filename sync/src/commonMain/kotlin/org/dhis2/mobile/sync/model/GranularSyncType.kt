package org.dhis2.mobile.sync.model

import org.dhis2.mobile.sync.model.GranularSyncType.DataSet
import org.dhis2.mobile.sync.model.GranularSyncType.DataValue
import org.dhis2.mobile.sync.model.GranularSyncType.Event
import org.dhis2.mobile.sync.model.GranularSyncType.Program
import org.dhis2.mobile.sync.model.GranularSyncType.Tei

sealed class GranularSyncType(
    val name: String,
) {
    data object Program : GranularSyncType("program")

    data object Tei : GranularSyncType("tei")

    data object Event : GranularSyncType("event")

    data object DataSet : GranularSyncType("dataset")

    data object DataValue : GranularSyncType("datavalue")
}

internal fun fromName(name: String) =
    when (name) {
        "program" -> Program
        "tei" -> Tei
        "Event" -> Event
        "DataSet" -> DataSet
        "DataValue" -> DataValue
        else -> error("Type %s is not available".format(name))
    }
