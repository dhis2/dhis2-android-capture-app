package org.dhis2.usescases.main.program

import io.reactivex.Flowable
import io.reactivex.parallel.ParallelFlowable
import org.dhis2.commons.bindings.isStockProgram
import org.dhis2.commons.filters.data.FilterPresenter
import org.dhis2.commons.resources.MetadataIconProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.schedulers.SchedulerProvider
import org.dhis2.data.dhislogic.DhisProgramUtils
import org.dhis2.data.service.SyncStatusData
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.program.ProgramType.WITHOUT_REGISTRATION
import org.hisp.dhis.android.core.program.ProgramType.WITH_REGISTRATION
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

internal class ProgramRepositoryImpl(
    private val d2: D2,
    private val filterPresenter: FilterPresenter,
    private val dhisProgramUtils: DhisProgramUtils,
    private val resourceManager: ResourceManager,
    private val metadataIconProvider: MetadataIconProvider,
    private val schedulerProvider: SchedulerProvider,
) : ProgramRepository {
    private val programViewModelMapper = ProgramViewModelMapper()
    private var lastSyncStatus: SyncStatusData? = null
    private var baseProgramCache: List<ProgramUiModel> = emptyList()

    override fun homeItems(syncStatusData: SyncStatusData): Flowable<List<ProgramUiModel>> =
        programModels(syncStatusData)
            .onErrorReturn { arrayListOf() }
            .mergeWith(aggregatesModels(syncStatusData).onErrorReturn { arrayListOf() })
            .flatMapIterable { data -> data }
            .sorted { p1, p2 -> p1.title.compareTo(p2.title, ignoreCase = true) }
            .toList()
            .toFlowable()
            .subscribeOn(schedulerProvider.io())
            .onErrorReturn { arrayListOf() }
            .doOnNext {
                lastSyncStatus = syncStatusData
            }

    override fun aggregatesModels(syncStatusData: SyncStatusData): Flowable<List<ProgramUiModel>> =
        Flowable.fromCallable {
            aggregatesModels()
                .blockingFirst()
                .applySync(syncStatusData)
        }

    override fun clearCache() {
        baseProgramCache = emptyList()
    }

    private fun aggregatesModels(): Flowable<List<ProgramUiModel>> =
        filterPresenter
            .filteredDataSetInstances()
            .get()
            .toFlowable()
            .map { dataSetSummaries ->
                dataSetSummaries.mapNotNull {
                    d2
                        .dataSetModule()
                        .dataSets()
                        .uid(it.dataSetUid())
                        .blockingGet()
                        ?.let { dataSet ->
                            programViewModelMapper.map(
                                dataSet = dataSet,
                                dataSetInstanceSummary = it,
                                recordCount =
                                    if (filterPresenter.isAssignedToMeApplied()) {
                                        0
                                    } else {
                                        it.dataSetInstanceCount()
                                    },
                                dataSetLabel = resourceManager.defaultDataSetLabel(),
                                filtersAreActive = filterPresenter.areFiltersActive(),
                                metadataIconData = metadataIconProvider(dataSet.style(), SurfaceColor.Primary),
                            )
                        }
                }
            }

    override fun programModels(syncStatusData: SyncStatusData): Flowable<List<ProgramUiModel>> =
        Flowable.fromCallable {
            baseProgramCache
                .ifEmpty {
                    baseProgramCache = basePrograms()
                    baseProgramCache
                }.applyFilters()
                .applySync(syncStatusData)
        }

    private fun basePrograms(): List<ProgramUiModel> =
        dhisProgramUtils
            .getProgramsInCaptureOrgUnits()
            .flatMap { programs ->
                ParallelFlowable
                    .from(Flowable.fromIterable(programs))
                    .runOn(schedulerProvider.io())
                    .sequential()
            }.map { program ->
                val recordLabel =
                    dhisProgramUtils.getProgramRecordLabel(
                        program,
                        resourceManager.defaultTeiLabel(),
                        resourceManager.defaultEventLabel(),
                    )
                val state = dhisProgramUtils.getProgramState(program)

                programViewModelMapper
                    .map(
                        program,
                        0,
                        recordLabel,
                        state,
                        filtersAreActive = false,
                        metadataIconData = metadataIconProvider(program.style(), SurfaceColor.Primary),
                    ).copy(
                        isStockUseCase = d2.isStockProgram(program.uid()),
                    )
            }.toList()
            .toFlowable()
            .blockingFirst()

    private fun List<ProgramUiModel>.applyFilters(): List<ProgramUiModel> =
        map { programModel ->
            val program =
                d2
                    .programModule()
                    .programs()
                    .uid(programModel.uid)
                    .blockingGet()
            val count =
                if (program?.programType() == WITHOUT_REGISTRATION) {
                    getSingleEventCount(program)
                } else if (program?.programType() == WITH_REGISTRATION) {
                    getTrackerTeiCount(program)
                } else {
                    0
                }
            programModel.copy(
                count = count,
                filtersAreActive = filterPresenter.areFiltersActive(),
            )
        }

    private fun List<ProgramUiModel>.applySync(syncStatusData: SyncStatusData): List<ProgramUiModel> =
        map { programModel ->
            programModel.copy(
                downloadState =
                    when {
                        syncStatusData.hasDownloadError(programModel.uid) ->
                            ProgramDownloadState.ERROR

                        syncStatusData.isProgramDownloading(programModel.uid) ->
                            ProgramDownloadState.DOWNLOADING

                        syncStatusData.isProgramDownloaded(programModel.uid) ->
                            ProgramDownloadState.DOWNLOADED

                        else ->
                            ProgramDownloadState.NONE
                    },
                downloadActive = syncStatusData.running ?: false,
            )
        }

    private fun getSingleEventCount(program: Program): Int =
        filterPresenter
            .filteredEventProgram(program)
            .blockingGet()
            .filter { event -> event.syncState() != State.RELATIONSHIP }
            .size

    private fun getTrackerTeiCount(program: Program): Int {
        val teiIds =
            filterPresenter
                .filteredTrackerProgram(program)
                .offlineFirst()
                .blockingGetUids()
        val teiCount = teiIds.size

        return teiCount
    }
}
