package org.dhis2.android.rtsm.data

sealed class SpeechRecognitionState {
    object NotInitialized : SpeechRecognitionState()
    object Started : SpeechRecognitionState()
    object Stopped : SpeechRecognitionState()
    object NotAvailable : SpeechRecognitionState()
    data class Completed(val data: String?) : SpeechRecognitionState()
    data class Errored(val code: Int, val data: String? = null) : SpeechRecognitionState()
}
