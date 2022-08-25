package org.dhis2.android.rtsm.ui.base

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import io.reactivex.disposables.CompositeDisposable
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.commons.Constants
import org.dhis2.android.rtsm.commons.Constants.AUDIO_RECORDING_REQUEST_CODE
import org.dhis2.android.rtsm.commons.Constants.INTENT_EXTRA_MESSAGE
import org.dhis2.android.rtsm.data.SpeechRecognitionState
import org.dhis2.android.rtsm.data.TransactionType
import org.dhis2.android.rtsm.ui.scanner.ScannerActivity
import org.dhis2.android.rtsm.ui.settings.SettingsActivity
import org.dhis2.android.rtsm.utils.ActivityManager.Companion.checkPermission
import org.dhis2.android.rtsm.utils.ActivityManager.Companion.showErrorMessage
import org.dhis2.android.rtsm.utils.ActivityManager.Companion.showInfoMessage
import org.dhis2.android.rtsm.utils.ActivityManager.Companion.showToast
import org.dhis2.android.rtsm.utils.LocaleManager
import org.dhis2.android.rtsm.utils.NetworkUtils.Companion.isOnline
import timber.log.Timber

/**
 * The base activity
 *
 * Sets the menu, and action bar.
 */
abstract class BaseActivity : AppCompatActivity() {
    private lateinit var viewModel: ViewModel
    private lateinit var binding: ViewDataBinding
    var speechController: SpeechController? = null

    private val disposable = CompositeDisposable()

    var voiceInputEnabled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enforce 'portrait' mode
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT

        viewModel = createViewModel(disposable)

        if (viewModel is BaseViewModel) {
            voiceInputEnabled = isVoiceInputEnabled(viewModel)

            // Request Audio permission if required
            if (voiceInputEnabled) {
                checkPermission(this, AUDIO_RECORDING_REQUEST_CODE)
            }
        }

        if (viewModel is SpeechRecognitionAwareViewModel) {
            val speechAwareViewModel = viewModel as SpeechRecognitionAwareViewModel
            speechAwareViewModel.resetSpeechStatus()

            speechController = SpeechControllerImpl(speechAwareViewModel)

            registerSpeechRecognitionStatusObserver(
                speechAwareViewModel.getSpeechStatus(), speechController)
        }

        // Set the custom theme, if any,
        // before calling setContentView (which happens in ViewBinding)
        getCustomTheme(viewModel)?.let {
            theme.applyStyle(it, true)
        }

        binding = createViewBinding()

        getToolBar()?.let {
            setupToolbar(it)
        }
    }

    override fun onStart() {
        super.onStart()
        showPendingMessages()
    }

    override fun onResume() {
        super.onResume()

        if (viewModel is BaseViewModel) {
            val currentVoiceInputState: Boolean = isVoiceInputEnabled(viewModel)

            if (voiceInputEnabled != currentVoiceInputState) {
                voiceInputEnabled = currentVoiceInputState

                onVoiceInputStateChanged()
            }
        }
    }

    /**
     * Should be overridden by subclasses that require custom logic
     */
    open fun onVoiceInputStateChanged() {}

    private fun isVoiceInputEnabled(viewModel: ViewModel) =
        (viewModel as BaseViewModel).isVoiceInputEnabled(resources.getString(R.string.use_mic_pref_key))

    override fun onDestroy() {
        disposable.clear()
        super.onDestroy()
    }

    private fun showPendingMessages() {
        val message = intent.getStringExtra(INTENT_EXTRA_MESSAGE)
        message?.let {
            showInfoMessage(
                getViewBinding().root, it
            )

            // Clear the intent payload to prevent persistent notifications
            intent.removeExtra(INTENT_EXTRA_MESSAGE)
        }
    }

    abstract fun createViewBinding(): ViewDataBinding

    /**
     * Initialize the ViewModel for this Activity
     */
    abstract fun createViewModel(disposable: CompositeDisposable): ViewModel

    /**
     * Subclasses should override this to use a custom theme
     */
    open fun getCustomTheme(viewModel: ViewModel): Int? = null

    fun getViewModel(): ViewModel = viewModel

    fun getViewBinding(): ViewDataBinding = binding

    private fun setupToolbar(toolbar: Toolbar) {
        setSupportActionBar(toolbar)

        if (supportActionBar != null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowTitleEnabled(true)
        } else Timber.w("Support action bar is null")
    }

    /**
     * Get the Activity's toolbar.
     * No toolbar is created by default. Subclasses should override this as necessary
     */
    open fun getToolBar(): Toolbar? = null

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (showMoreOptions()) {
            menuInflater.inflate(R.menu.more_options, menu)
            return true
        }
        return true
    }

    /**
     * Indicates if the more options menu should be shown
     */
    open fun showMoreOptions(): Boolean = false

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_settings -> {
                startActivity(SettingsActivity.getSettingsActivityIntent(this))
                return true
            }
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

    open fun scanBarcode(launcher: ActivityResultLauncher<ScanOptions>) {
        val scanOptions = ScanOptions()
            .setBeepEnabled(true)
            .setCaptureActivity(ScannerActivity::class.java)
        launcher.launch(scanOptions)
    }

    open fun crossFade(view: View, show: Boolean, duration: Long) {
        if (show) {
            view.alpha = 0f
            view.visibility = View.VISIBLE
            view.animate()
                .alpha(1f)
                .setDuration(duration)
                .setListener(null)
        } else {
            view.animate()
                .alpha(0f)
                .setDuration(duration)
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        view.visibility = View.GONE
                    }
                })
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AUDIO_RECORDING_REQUEST_CODE && grantResults.isNotEmpty()) {
            var messageRes: Int = R.string.permission_denied

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                messageRes = R.string.permission_granted
            else if (grantResults[0] == PackageManager.PERMISSION_DENIED)
                // Permission denial may occur for different reasons.
                // For more information, see
                // https://developer.android.com/training/permissions/requesting#handle-denial
                messageRes = R.string.permission_denied

            showToast(this, messageRes)
        }
    }

    open fun registerSpeechRecognitionStatusObserver(
        speechStatus: LiveData<SpeechRecognitionState>,
        speechController: SpeechController?
    ) {
        speechStatus.observe(this) { state: SpeechRecognitionState ->
            Timber.d("SpeechRecognitionState: %s", state)
            if (state is SpeechRecognitionState.Errored) {
                handleSpeechError(state.code, state.data)
            }
            speechController?.onStateChange(state)
        }
    }

    open fun handleSpeechError(code: Int, data: String?) {
        val resId: Int = when (code) {
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> R.string.insufficient_speech_permissions_error
            SpeechRecognizer.ERROR_AUDIO -> R.string.speech_audio_error
            SpeechRecognizer.ERROR_CLIENT -> R.string.speech_client_error
            SpeechRecognizer.ERROR_NETWORK -> R.string.speech_network_error
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> R.string.speech_network_timeout_error
            SpeechRecognizer.ERROR_NO_MATCH -> R.string.no_speech_match_error
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> R.string.speech_recognition_service_busy_error
            SpeechRecognizer.ERROR_SERVER -> R.string.speech_server_error
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> R.string.speech_timeout_error
            Constants.NON_NUMERIC_SPEECH_INPUT_ERROR -> R.string.non_numeric_speech_input_error
            Constants.NEGATIVE_NUMBER_NOT_ALLOWED_INPUT_ERROR -> R.string.negative_number_speech_input_error
            else -> R.string.unknown_speech_error
        }

        val message =
            if (code == Constants.NON_NUMERIC_SPEECH_INPUT_ERROR ||
                code == Constants.NEGATIVE_NUMBER_NOT_ALLOWED_INPUT_ERROR)
                getString(resId, data ?: "")
            else
                getString(resId)

        Timber.d("Speech status error: code = %d, message = %s", code, message)
        showErrorMessage(binding.root, message)
    }

    fun displayError(view: View, messageRes: Int) {
        showErrorMessage(view, getString(messageRes))
    }

    fun setTitle(transactionType: TransactionType) {
        when(transactionType) {
            TransactionType.CORRECTION -> setTitle(R.string.correction)
            TransactionType.DISTRIBUTION -> setTitle(R.string.distribution)
            TransactionType.DISCARD -> setTitle(R.string.discard)
        }
    }

    fun isConnectedToNetwork(): Boolean {
        val networkIsAvailable = isOnline(this)
        if (!networkIsAvailable) {
            displayError(binding.root, R.string.no_network_available)
        }
        return networkIsAvailable
    }

    open fun onScanCompleted(result: ScanIntentResult, textInput: EditText,
                             stockItemList: RecyclerView) {
        val data = result.contents
        textInput.setText(data)

        // TODO: Automatically activating the microphone after a successful scan
//        stockItemList.requestFocus();
    }
}