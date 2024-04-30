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
import org.dhis2.R
import org.dhis2.bindings.app
import org.dhis2.databinding.DialogPinBinding
import javax.inject.Inject

const val PIN_DIALOG_TAG: String = "PINDIALOG"

class PinDialog(
    val mode: Mode,
    private val canBeClosed: Boolean,
    private val unlockCallback: () -> Unit,
    private val forgotPinCallback: () -> Unit,
) : DialogFragment(), PinView {

    private lateinit var binding: DialogPinBinding

    @Inject
    lateinit var presenter: PinPresenter

    enum class Mode {
        SET, ASK
    }

    private var pinAttempts = 0

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
        savedInstanceState: Bundle?,
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
                    presenter.unlockSession(
                        it,
                        attempts = pinAttempts,
                        onPinCorrect = unlockCallback,
                        onError = {
                            pinAttempts += 1
                            Toast.makeText(
                                context,
                                getString(R.string.wrong_pin),
                                Toast.LENGTH_LONG,
                            ).show()
                            binding.pinLockView.resetPinLockView()
                        },
                        onTwoManyAttempts = { recoverPin() },
                    )
            }
        }

        return binding.root
    }

    private fun blockSession() {
        Handler().postDelayed(
            { Process.killProcess(Process.myPid()) },
            1500,
        )
    }

    override fun closeDialog() {
        dismissAllowingStateLoss()
    }

    override fun dismiss() {
        app().releaseSessionComponent()
        dismissAllowingStateLoss()
    }

    override fun recoverPin() {
        presenter.logOut()
        forgotPinCallback.invoke()
        dismissAllowingStateLoss()
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (manager.findFragmentByTag(tag) == null) {
            manager.beginTransaction().add(this, tag).commitAllowingStateLoss()
        }
    }
}
