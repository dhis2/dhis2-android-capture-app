package org.dhis2.android.rtsm.ui.base

import android.content.res.ColorStateList
import android.os.CountDownTimer
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.commons.Constants.CLEAR_ICON
import org.dhis2.android.rtsm.data.SpeechRecognitionState
import org.hisp.dhis.rules.models.RuleEffect
import timber.log.Timber

class TextInputDelegate {
    private var focusPosition: Int? = null

    fun <A, B, C> textChanged(item: A, qty: B?, position: Int, itemWatcher: ItemWatcher<A, B, C>) {
        item?.let {
            itemWatcher.quantityChanged(
                it,
                position,
                qty,
                object : ItemWatcher.OnQuantityValidated {
                    override fun validationCompleted(ruleEffects: List<RuleEffect>) {
                        itemWatcher.updateFields(it, qty, position, ruleEffects)
                    }
                })
        }
    }

    fun focusChanged(speechController: SpeechController?, textInputLayout: TextInputLayout, hasFocus: Boolean,
                     voiceInputEnabled: Boolean, position: Int) {
        // Prevent duplicate voice trigger
        val autoStart = position != focusPosition

        if (hasFocus && voiceInputEnabled) {
            textInputLayout.endIconMode = TextInputLayout.END_ICON_CUSTOM
            textInputLayout.setEndIconDrawable(R.drawable.ic_microphone)
            textInputLayout.setEndIconTintList(textInputLayout.context.getColorStateList(R.color.mic_selector))
            textInputLayout.setEndIconOnClickListener {
                speechController?.toggleState {
                    onComplete(it, textInputLayout)
                }
            }

            if (autoStart) {
                speechController?.startListening {
                    onComplete(it, textInputLayout)
                }
            } else {
                Timber.w("Duplicate focus detected. Ignoring...")
            }
        } else {
            textInputLayout.endIconMode = TextInputLayout.END_ICON_NONE
            textInputLayout.setEndIconDrawable(CLEAR_ICON)
            textInputLayout.setEndIconOnClickListener(null)
        }

        if (hasFocus)
            focusPosition = position
    }

    private fun onComplete(state: SpeechRecognitionState, textInputLayout: TextInputLayout) {
        if (state is SpeechRecognitionState.Completed) {
            textInputLayout.editText?.setText(state.data)
        }

        updateMicState(textInputLayout, state)
    }

    private fun updateMicState(textInputLayout: TextInputLayout, state: SpeechRecognitionState?) {
        state?.let {
            when (state) {
                SpeechRecognitionState.Started ->
                    textInputLayout.setEndIconTintList(
                        ColorStateList.valueOf(
                            ContextCompat.getColor(textInputLayout.context, R.color.mic_active)
                        )
                    )
                else ->
                    textInputLayout.setEndIconTintList(
                        textInputLayout.context.getColorStateList(R.color.mic_selector))
            }
        }
    }

    fun clearFieldAfterDelay(editText: EditText?, delay: Long) {
        editText?.let {
            object: CountDownTimer(delay, delay) {
                override fun onTick(millisUntilFinished: Long) {}

                override fun onFinish() {
                    it.setText("")
                }
            }.start()
        }
    }

    /**
     * Re-render the active field to reflect voice input settings change
     */
    fun voiceInputStateChanged(adapter: RecyclerView.Adapter<*>) {
        focusPosition?.let {
            if (focusPosition != RecyclerView.NO_POSITION)
                adapter.notifyItemChanged(it)
        }
    }
}