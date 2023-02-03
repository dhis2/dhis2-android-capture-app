package org.dhis2.utils.granularsync

import org.dhis2.data.dhislogic.DhisProgramUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.program.ProgramType

class GranularSyncRepository(
    private val d2: D2,
    private val dhisProgramUtils: DhisProgramUtils
) {
    fun getHomeItemsWithStates(vararg states: State): List<SyncStatusItem> {
        val programList = d2.programModule().programs()
            .blockingGet().filter {
                states.contains(dhisProgramUtils.getProgramState(it))
            }.map { program ->
                SyncStatusItem(
                    type = when (program.programType()) {
                        ProgramType.WITH_REGISTRATION -> SyncStatusType.EventProgram(program.uid())
                        ProgramType.WITHOUT_REGISTRATION -> SyncStatusType.TrackerProgram(program.uid())
                        null -> throw NullPointerException("Program ${program.uid()}: program type can't be null")
                    },
                    displayName = program.displayName() ?: program.uid(),
                    description = "PLACEHOLDER",
                    state = dhisProgramUtils.getProgramState(program)
                )
            }
        val dataSetList = d2.dataSetModule().dataSetInstanceSummaries()
            .blockingGet().filter {
                states.contains(it.state())
            }.map { dataSetInstanceSummary ->
                SyncStatusItem(
                    type = SyncStatusType.DataSet(dataSetInstanceSummary.dataSetUid()),
                    displayName = dataSetInstanceSummary.dataSetDisplayName(),
                    description = "PLACEHOLDER",
                    state = dataSetInstanceSummary.state()
                )
            }
        return (programList + dataSetList).sortedWith { item1, item2 ->
            item1.state.priority().compareTo(item2.state.priority())
        }
    }
}

fun State.priority(): Int {
    return when (this) {
        State.TO_POST,
        State.TO_UPDATE -> 3
        State.ERROR -> 1
        State.WARNING -> 2
        State.SYNCED,
        State.UPLOADING,
        State.RELATIONSHIP,
        State.SENT_VIA_SMS,
        State.SYNCED_VIA_SMS -> 4
    }
}
