package org.dhis2.usescases.main.program

import io.reactivex.Flowable
import io.reactivex.parallel.ParallelFlowable
import org.dhis2.commons.filters.data.FilterPresenter
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.dhislogic.DhisProgramUtils
import org.dhis2.data.dhislogic.DhisTrackedEntityInstanceUtils
import org.dhis2.data.service.SyncStatusData
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.call.D2ProgressSyncStatus
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramType.WITHOUT_REGISTRATION

internal class ProgramRepositoryImpl(
    private val d2: D2,
    private val filterPresenter: FilterPresenter,
    private val dhisProgramUtils: DhisProgramUtils,
    private val dhisTeiUtils: DhisTrackedEntityInstanceUtils,
    private val resourceManager: ResourceManager,
    private val schedulerProvider: SchedulerProvider
) : ProgramRepository {

    private val programViewModelMapper = ProgramViewModelMapper(resourceManager)
    private var lastSyncStatus: SyncStatusData? = null
    private var baseProgramCache: List<ProgramViewModel> = emptyList()

    override fun homeItems(syncStatusData: SyncStatusData): Flowable<List<ProgramViewModel>> {
        return programModels(syncStatusData).onErrorReturn { arrayListOf() }
            .mergeWith(aggregatesModels(syncStatusData).onErrorReturn { arrayListOf() })
            .flatMapIterable { data -> data }
            .sorted { p1, p2 -> p1.title.compareTo(p2.title, ignoreCase = true) }
            .toList().toFlowable()
            .subscribeOn(schedulerProvider.io())
            .onErrorReturn { arrayListOf() }
            .doOnNext {
                lastSyncStatus = syncStatusData
            }
    }

    override fun aggregatesModels(
        syncStatusData: SyncStatusData
    ): Flowable<List<ProgramViewModel>> {
        return Flowable.fromCallable {
            aggregatesModels().blockingFirst()
                .applySync(syncStatusData)
        }
    }

    override fun clearCache() {
        baseProgramCache = emptyList()
    }

    private fun aggregatesModels(): Flowable<List<ProgramViewModel>> {
        return filterPresenter.filteredDataSetInstances().get()
            .toFlowable()
            .map { dataSetSummaries ->
                dataSetSummaries.map {
                    val dataSet = d2.dataSetModule().dataSets()
                        .uid(it.dataSetUid())
                        .blockingGet()
                    programViewModelMapper.map(
                        dataSet,
                        it,
                        if (filterPresenter.isAssignedToMeApplied()) {
                            0
                        } else {
                            it.dataSetInstanceCount()
                        },
                        resourceManager.defaultDataSetLabel(),
                        filterPresenter.areFiltersActive()
                    )
                }
            }
    }

    override fun programModels(syncStatusData: SyncStatusData): Flowable<List<ProgramViewModel>> {
        return Flowable.fromCallable {
            baseProgramCache.ifEmpty {
                baseProgramCache = basePrograms()
                baseProgramCache
            }.applyFilters()
                .applySync(syncStatusData)
        }
    }

    private fun basePrograms(): List<ProgramViewModel> {
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

                programViewModelMapper.map(
                    program,
                    0,
                    recordLabel,
                    state,
                    hasOverdue = false,
                    filtersAreActive = false
                )
            }.toList().toFlowable().blockingFirst()
    }

    private fun List<ProgramViewModel>.applyFilters(): List<ProgramViewModel> {
        return map { programModel ->
            val program = d2.programModule().programs().uid(programModel.uid).blockingGet()
            val (count, hasOverdue) =
                if (program.programType() == WITHOUT_REGISTRATION) {
                    getSingleEventCount(program)
                } else {
                    getTrackerTeiCountAndOverdue(program)
                }
            programModel.copy(
                count = count,
                hasOverdueEvent = hasOverdue,
                filtersAreActive = filterPresenter.areFiltersActive()
            )
        }
    }

    private fun List<ProgramViewModel>.applySync(
        syncStatusData: SyncStatusData
    ): List<ProgramViewModel> {
        return map { programModel ->
            programModel.copy(
                downloadState = when {
                    syncStatusData.hasDownloadError(programModel.uid) ->
                        ProgramDownloadState.ERROR
                    syncStatusData.isProgramDownloading(programModel.uid) ->
                        ProgramDownloadState.DOWNLOADING
                    syncStatusData.wasProgramDownloading(lastSyncStatus, programModel.uid) ->
                        when (syncStatusData.programSyncStatusMap[programModel.uid]?.syncStatus) {
                            D2ProgressSyncStatus.SUCCESS -> ProgramDownloadState.DOWNLOADED
                            D2ProgressSyncStatus.ERROR,
                            D2ProgressSyncStatus.PARTIAL_ERROR -> ProgramDownloadState.ERROR
                            null -> ProgramDownloadState.DOWNLOADED
                        }
                    else ->
                        ProgramDownloadState.NONE
                },
                downloadActive = syncStatusData.running
            )
        }
    }

    private fun getSingleEventCount(program: Program): Pair<Int, Boolean> {
        return Pair(
            filterPresenter.filteredEventProgram(program)
                .blockingGet().filter { event -> event.syncState() != State.RELATIONSHIP }.size,
            false
        )
    }

    private fun getTrackerTeiCountAndOverdue(program: Program): Pair<Int, Boolean> {
        val teiIds = filterPresenter.filteredTrackerProgram(program)
            .offlineFirst().blockingGetUids()
        val mCount = teiIds.size
        val mOverdue = dhisTeiUtils.hasOverdueInProgram(teiIds, program)

        return Pair(mCount, mOverdue)
    }
}
