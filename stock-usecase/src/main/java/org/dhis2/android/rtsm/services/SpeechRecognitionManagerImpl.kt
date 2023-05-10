package org.dhis2.android.rtsm.services

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import java.util.Locale
import org.dhis2.android.rtsm.commons.Constants
import org.dhis2.android.rtsm.data.SpeechRecognitionState
import org.dhis2.android.rtsm.utils.Utils
import timber.log.Timber

class SpeechRecognitionManagerImpl(
    private val context: Context
) : SpeechRecognitionManager, RecognitionListener {
    private var speechRecognizer: SpeechRecognizer? = null
    private var readyForSpeech = false
    private var allowNegativeNumberInput: Boolean = false

    private val _speechRecognitionStatus: MutableLiveData<SpeechRecognitionState> =
        MutableLiveData(SpeechRecognitionState.NotInitialized)

    init {
        setup()
    }

    private fun setup() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)

        // Check if speech recognition is available
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            initialize()
        } else {
            Timber.e("Speech recognition is not available")

            _speechRecognitionStatus.postValue(SpeechRecognitionState.NotAvailable)
        }
    }

    private fun initialize() {
        speechRecognizer?.setRecognitionListener(this)
    }

    override fun start() {
        readyForSpeech = false

        speechRecognizer?.startListening(getIntent())
    }

    override fun restart() {
        speechRecognizer = null

        setup()
        start()
    }

    override fun stop() {
        speechRecognizer?.stopListening()
        _speechRecognitionStatus.postValue(SpeechRecognitionState.Stopped)
    }

    override fun cleanUp() {
        speechRecognizer?.destroy()
    }

    override fun resetStatus() {
        _speechRecognitionStatus.value = SpeechRecognitionState.NotInitialized
    }

    override fun getStatus() = _speechRecognitionStatus
    override fun supportNegativeNumberInput(allow: Boolean) {
        allowNegativeNumberInput = allow
    }

    private fun getIntent(): Intent {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)

        return intent
    }

    override fun onReadyForSpeech(params: Bundle?) {
        readyForSpeech = true

        _speechRecognitionStatus.postValue(SpeechRecognitionState.Started)
    }

    override fun onError(errorCode: Int) {
        // handle buggy situation where onError(5) is always called before onReadyForSpeech()
        if (!readyForSpeech && errorCode == SpeechRecognizer.ERROR_CLIENT) {
            return
        }

        _speechRecognitionStatus.postValue(SpeechRecognitionState.Errored(errorCode))
    }

    override fun onResults(bundle: Bundle?) {
        val data = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        data?.let {
            if (data.size > 0) {
                val str = data[0]
                _speechRecognitionStatus.postValue(validateInput(str))
            }
        }
    }

    private fun validateInput(inputStr: String): SpeechRecognitionState {
        // Only CORRECTION transaction supports negative number input
        val isCorrection = allowNegativeNumberInput

        // There are cases where a space is added after the minus sign, which results
        // if the entire input being flagged as a non-signed number. Prevent cases
        // like this
        val cleanInputStr = Utils.cleanUpSignedNumber(inputStr)
        return when {
            isCorrection -> {
                if (Utils.isSignedNumeric(cleanInputStr)) {
                    SpeechRecognitionState.Completed(cleanInputStr)
                } else {
                    SpeechRecognitionState.Errored(
                        Constants.NON_NUMERIC_SPEECH_INPUT_ERROR,
                        cleanInputStr
                    )
                }
            }
            else -> {
                if (TextUtils.isDigitsOnly(cleanInputStr)) {
                    SpeechRecognitionState.Completed(cleanInputStr)
                } else {
                    SpeechRecognitionState.Errored(
                        Constants.NEGATIVE_NUMBER_NOT_ALLOWED_INPUT_ERROR,
                        cleanInputStr
                    )
                }
            }
        }
    }

    override fun onBeginningOfSpeech() {}
    override fun onEndOfSpeech() {}
    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
}
