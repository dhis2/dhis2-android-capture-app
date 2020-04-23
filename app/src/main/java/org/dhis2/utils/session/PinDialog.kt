package org.dhis2.utils.session

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Process
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import javax.inject.Inject
import org.dhis2.Bindings.app
import org.dhis2.R
import org.dhis2.databinding.DialogPinBinding

const val PIN_DIALOG_TAG: String = "PINDIALOG"

class PinDialog(
    val mode: Mode,
    private val canBeClosed: Boolean,
    private val unlockCallback: (Boolean) -> Unit,
    private val forgotPinCallback: () -> Unit
) : DialogFragment(), PinView {

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
        dialog.window!!.apply {
            requestFeature(Window.FEATURE_NO_TITLE)
            setBackgroundDrawableResource(android.R.color.transparent)
            setWindowAnimations(R.style.pin_dialog_animation)
            isCancelable = false
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DialogPinBinding.inflate(layoutInflater, container, false)
        binding.closeButton.apply {
            visibility = if (canBeClosed) View.VISIBLE else View.GONE
            setOnClickListener { closeDialog() }
        }

        when (mode) {
            Mode.ASK -> {
                binding.title.text = getString(R.string.unblock_session)
                binding.forgotCode.apply {
                    visibility = View.VISIBLE
                    setOnClickListener { recoverPin() }
                }
            }
            Mode.SET -> {
                binding.title.text = getString(R.string.set_pin)
                binding.forgotCode.visibility = View.GONE
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
                    if (presenter.unlockSession(it)) {
                        unlockCallback.invoke(true)
                    } else {
                        Toast.makeText(context, "Wrong pin", Toast.LENGTH_SHORT).show()
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
        presenter.logOut()
        forgotPinCallback.invoke()
        dismiss()
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (manager.findFragmentByTag(tag) == null) {
            super.show(manager, tag)
        }
    }
}
