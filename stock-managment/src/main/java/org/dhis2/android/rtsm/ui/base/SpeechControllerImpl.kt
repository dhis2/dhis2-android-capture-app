package org.dhis2.android.rtsm.ui.base

import org.dhis2.android.rtsm.data.SpeechRecognitionState

class SpeechControllerImpl(private val viewModel: SpeechRecognitionAwareViewModel):
    SpeechController {
    private var callback: Function1<SpeechRecognitionState, Unit>? = null

    override fun onStateChange(state: SpeechRecognitionState) {
        callback?.invoke(state)
    }

    override fun startListening(callback: (state: SpeechRecognitionState) -> Unit) {
        this.callback = callback

        viewModel.startListening()
    }

    override fun stopListening() {
        viewModel.stopListening()
    }

    override fun toggleState(callback: (state: SpeechRecognitionState) -> Unit) {
        this.callback = callback
        viewModel.toggleSpeechRecognitionState()
    }
}