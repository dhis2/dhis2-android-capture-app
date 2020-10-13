package org.dhis2.usescases.main.program

import io.reactivex.Flowable
import io.reactivex.parallel.ParallelFlowable
import org.dhis2.data.dhislogic.DhisProgramUtils
import org.dhis2.data.dhislogic.DhisTrackedEntityInstanceUtils
import org.dhis2.data.filter.FilterController
import org.dhis2.data.schedulers.SchedulerProvider
import org.dhis2.utils.resources.ResourceManager
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramType.WITHOUT_REGISTRATION

internal class HomeRepositoryImpl(
    private val d2: D2,
    private val filterController: FilterController,
    private val dhisProgramUtils: DhisProgramUtils,
    private val dhisTeiUtils: DhisTrackedEntityInstanceUtils,
    private val resourceManager: ResourceManager,
    private val schedulerProvider: SchedulerProvider
) : HomeRepository {

    private val programViewModelMapper = ProgramViewModelMapper()

    override fun aggregatesModels(): Flowable<List<ProgramViewModel>> {
        return filterController.filteredDataSetInstances().get()
            .toFlowable()
            .map { dataSetSummaries ->
                dataSetSummaries.map {
                    val dataSet = d2.dataSetModule().dataSets()
                        .uid(it.dataSetUid())
                        .blockingGet()
                    programViewModelMapper.map(
                        dataSet,
                        it,
                        if (filterController.isAssignedToMeApplied()) {
                            0
                        } else {
                            it.dataSetInstanceCount()
                        },
                        resourceManager.defaultDataSetLabel(),
                        filterController.areFiltersActive()
                    )
                }
            }
    }

    override fun programModels(): Flowable<List<ProgramViewModel>> {
        return dhisProgramUtils.getProgramsInCaptureOrgUnits()
            .flatMap { programs ->
                ParallelFlowable.from(Flowable.fromIterable(programs))
                    .runOn(schedulerProvider.io())
                    .sequential()
            }
            .map { program ->
                val recordLabel =
                    dhisProgramUtils.getProgramRecordLabel(
                        program,
                        resourceManager.defaultTeiLabel(),
                        resourceManager.defaultEventLabel()
                    )
                val state = dhisProgramUtils.getProgramState(program)
                val (count, hasOverdue) =
                    if (program.programType() == WITHOUT_REGISTRATION) {
                        getSingleEventCount(program)
                    } else {
                        getTrackerTeiCountAndOverdue(program)
                    }
                programViewModelMapper.map(
                    program,
                    count,
                    recordLabel,
                    state,
                    hasOverdue,
                    filterController.areFiltersActive()
                )
            }.toList().toFlowable()
    }

    private fun getSingleEventCount(program: Program): Pair<Int, Boolean> {
        return Pair(
            filterController.filteredEventProgram(program).blockingCount(),
            false
        )
    }

    private fun getTrackerTeiCountAndOverdue(program: Program): Pair<Int, Boolean> {
        val teis = filterController.filteredTrackerProgram(program)
            .offlineFirst().blockingGet()
        val mCount = teis.size
        val mOverdue = teis.firstOrNull {
            dhisTeiUtils.hasOverdueInProgram(it, program)
        } != null

        return Pair(mCount, mOverdue)
    }
}
