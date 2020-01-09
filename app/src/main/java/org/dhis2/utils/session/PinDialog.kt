package org.dhis2.utils.session

import android.app.Dialog
import android.os.Bundle
import android.os.Process
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.CheckBox
import androidx.fragment.app.DialogFragment
import org.dhis2.Bindings.app
import org.dhis2.R
import org.dhis2.databinding.DialogPinBinding

const val PIN_DIALOG_TAG: String = "PINDIALOG"
class PinDialog(val mode: Mode) : DialogFragment(), PinView {

    private lateinit var binding: DialogPinBinding

    private lateinit var presenter: PinPresenter

    enum class Mode {
        SET, ASK
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Light_NoActionBar_Fullscreen)
        app().createSessionComponent(PinModule(this)).inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogPinBinding.inflate(layoutInflater, container, false)

        when (mode) {
            Mode.ASK -> {
                binding.title.text = getString(R.string.unblock_session)
                binding.forgotCode.visibility = View.VISIBLE
                binding.lockPin.visibility = View.VISIBLE
            }
            Mode.SET -> {
                binding.title.text = getString(R.string.set_pin)
                binding.forgotCode.visibility = View.GONE
                binding.lockPin.visibility = View.VISIBLE
            }
        }

        binding.pinLockView.attachIndicatorDots(binding.indicatorDots)
        binding.pinLockView.onPinSet {
            when (mode) {
                Mode.SET -> {
                    presenter.savePin(it)
                    blockSession()
                }
                Mode.ASK ->
                    if (presenter.unlockSession(it, !(binding.lockPin as CheckBox).isChecked)) {
                        //TODO: UNBLOCK SUCCEEDED
                    }
            }
        }

        return binding.root
    }

    fun blockSession() {
        Process.killProcess(Process.myPid())
    }

    override fun closeDialog() {
        dismiss()
    }

    override fun dismiss() {
        app().releaseSessionCOmponent()
        super.dismiss()
    }

    override fun recoverPin() {

    }
}