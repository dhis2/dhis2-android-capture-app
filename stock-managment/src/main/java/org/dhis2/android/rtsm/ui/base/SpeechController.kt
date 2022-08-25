package org.dhis2.android.rtsm.ui.base

import org.dhis2.android.rtsm.data.SpeechRecognitionState

interface SpeechController {
    fun startListening(callback: (state: SpeechRecognitionState) -> Unit)
    fun stopListening()
    fun onStateChange(state: SpeechRecognitionState)
    fun toggleState(callback: (state: SpeechRecognitionState) -> Unit)
}