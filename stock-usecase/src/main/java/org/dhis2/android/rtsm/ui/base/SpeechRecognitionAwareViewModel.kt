package org.dhis2.android.rtsm.ui.base

import org.dhis2.android.rtsm.data.SpeechRecognitionState
import org.dhis2.android.rtsm.services.SpeechRecognitionManager
import org.dhis2.android.rtsm.services.scheduler.BaseSchedulerProvider
import javax.inject.Inject

open class SpeechRecognitionAwareViewModel @Inject constructor(
    schedulerProvider: BaseSchedulerProvider,
    private val speechRecognitionManager: SpeechRecognitionManager,
) : BaseViewModel(schedulerProvider) {
    fun startListening() {
        speechRecognitionManager.start()
    }

    fun stopListening() {
        speechRecognitionManager.stop()
    }

    fun getSpeechStatus() = speechRecognitionManager.getStatus()

    fun toggleSpeechRecognitionState() {
        val state = getSpeechStatus().value ?: return

        if (state == SpeechRecognitionState.Started) {
            stopListening()
        } else {
            startListening()
        }
    }

    fun resetSpeechStatus() {
        speechRecognitionManager.resetStatus()
    }
}
