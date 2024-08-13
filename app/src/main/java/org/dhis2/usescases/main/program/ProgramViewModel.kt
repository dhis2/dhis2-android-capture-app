package org.dhis2.usescases.main.program

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.dhis2.commons.matomo.Actions.Companion.SYNC_BTN
import org.dhis2.commons.matomo.Categories.Companion.HOME
import org.dhis2.commons.matomo.Labels.Companion.CLICK_ON
import org.dhis2.commons.matomo.MatomoAnalyticsController
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.dhis2.data.service.SyncStatusController
import timber.log.Timber

class ProgramViewModel internal constructor(
    private val view: ProgramView,
    private val programRepository: ProgramRepository,
    private val dispatchers: DispatcherProvider,
    private val matomoAnalyticsController: MatomoAnalyticsController,
    private val syncStatusController: SyncStatusController,
) : ViewModel() {

    private val _programs = MutableLiveData<List<ProgramUiModel>>()
    val programs: LiveData<List<ProgramUiModel>> = _programs

    var disposable: CompositeDisposable = CompositeDisposable()

    fun init() {
        programRepository.clearCache()
        fetchPrograms()
    }

    private fun fetchPrograms() {
        viewModelScope.launch(dispatchers.io()) {
            val result = async {
                programRepository.homeItems(
                    syncStatusController.observeDownloadProcess().value!!,
                ).blockingLast()
            }
            try {
                _programs.postValue(result.await())
            } catch (e: Exception) {
                Timber.d(e)
            }
        }
    }

    fun onSyncStatusClick(program: ProgramUiModel) {
        val programTitle = "$CLICK_ON${program.title}"
        matomoAnalyticsController.trackEvent(HOME, SYNC_BTN, programTitle)
        view.showSyncDialog(program)
    }

    fun updateProgramQueries() {
        init()
    }

    fun onItemClick(programModel: ProgramUiModel) {
        view.navigateTo(programModel)
    }

    fun dispose() {
        disposable.clear()
    }

    fun downloadState() = syncStatusController.observeDownloadProcess()

    fun setIsDownloading() {
        viewModelScope.launch(dispatchers.io()) {
            fetchPrograms()
        }
    }
}
