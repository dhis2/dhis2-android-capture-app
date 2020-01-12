package org.dhis2.utils.session

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Process
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.CheckBox
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import org.dhis2.Bindings.app
import org.dhis2.R
import org.dhis2.databinding.DialogPinBinding
import javax.inject.Inject

const val PIN_DIALOG_TAG: String = "PINDIALOG"

class PinDialog(
    val mode: Mode,
    private val unlockCallback: (Boolean) -> Unit,
    private val forgotPinCallback: () -> Unit
) : DialogFragment(), PinView {

    private var forgetPin: Boolean = false
    private lateinit var binding: DialogPinBinding

    @Inject
    lateinit var presenter: PinPresenter

    enum class Mode {
        SET, ASK
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Light_NoActionBar)
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
        binding.closeButton.setOnClickListener { closeDialog() }

        when (mode) {
            Mode.ASK -> {
                binding.title.text = getString(R.string.unblock_session)
                binding.forgotCode.apply {
                    visibility = View.VISIBLE
                    setOnClickListener { recoverPin() }
                }
                binding.lockPin.visibility = View.VISIBLE
                binding.lockPin.apply {
                    visibility = View.VISIBLE
                    setOnCheckedChangeListener { _, isChecked -> forgetPin = isChecked }
                    isChecked = forgetPin
                }
            }
            Mode.SET -> {
                binding.title.text = getString(R.string.set_pin)
                binding.forgotCode.visibility = View.GONE
                binding.lockPin.visibility = View.GONE
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
                    if (presenter.unlockSession(it, !forgetPin)) {
                        unlockCallback.invoke(true)
                    }else{
                        Toast.makeText(context,"Wrong pin",Toast.LENGTH_SHORT).show()
                    }
            }
        }

        return binding.root
    }

    private fun blockSession() {
        Handler().postDelayed(
            { Process.killProcess(Process.myPid()) }, 1500
        )

    }

    override fun closeDialog() {
        dismiss()
    }

    override fun dismiss() {
        app().releaseSessionComponent()
        super.dismiss()
    }

    override fun recoverPin() {
        forgotPinCallback.invoke()
        dismiss()
    }
}