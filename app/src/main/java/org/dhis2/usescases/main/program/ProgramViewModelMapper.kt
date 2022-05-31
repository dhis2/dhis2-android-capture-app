package org.dhis2.usescases.main.program

import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.dataset.DataSet
import org.hisp.dhis.android.core.dataset.DataSetInstanceSummary
import org.hisp.dhis.android.core.program.Program

class ProgramViewModelMapper {
    fun map(
        program: Program,
        recordCount: Int,
        recordLabel: String,
        state: State,
        hasOverdue: Boolean,
        filtersAreActive: Boolean
    ): ProgramViewModel {
        return ProgramViewModel(
            uid = program.uid(),
            title = program.displayName()!!,
            color = if (program.style() != null) program.style()!!.color() else null,
            icon = if (program.style() != null) program.style()!!.icon() else null,
            count = recordCount,
            type = if (program.trackedEntityType() != null) {
                program.trackedEntityType()!!.uid()
            } else {
                null
            },
            typeName = recordLabel,
            programType = program.programType()!!.name,
            description = program.displayDescription(),
            onlyEnrollOnce = program.onlyEnrollOnce() == true,
            accessDataWrite = program.access().data().write(),
            state = State.valueOf(state.name),
            hasOverdueEvent = hasOverdue,
            filtersAreActive = filtersAreActive
        )
    }

    fun map(
        dataSet: DataSet,
        dataSetInstanceSummary: DataSetInstanceSummary,
        recordCount: Int,
        dataSetLabel: String,
        filtersAreActive: Boolean
    ): ProgramViewModel {
        return ProgramViewModel(
            uid = dataSetInstanceSummary.dataSetUid(),
            title = dataSetInstanceSummary.dataSetDisplayName(),
            color = dataSet.style().color(),
            icon = dataSet.style().icon(),
            count = recordCount,
            type = null,
            typeName = dataSetLabel,
            programType = "",
            description = dataSet.description(),
            onlyEnrollOnce = false,
            accessDataWrite = dataSet.access().data().write(),
            state = dataSetInstanceSummary.state(),
            hasOverdueEvent = false,
            filtersAreActive = filtersAreActive
        )
    }
}
