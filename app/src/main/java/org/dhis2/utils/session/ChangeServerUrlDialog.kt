package org.dhis2.utils.session

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import org.dhis2.bindings.app
import org.dhis2.R
import org.dhis2.commons.extensions.closeKeyboard
import org.dhis2.commons.network.NetworkUtils
import org.dhis2.commons.resources.D2ErrorUtils
import org.dhis2.databinding.DialogChangeServerUrlBinding
import org.dhis2.usescases.general.ActivityGlobalAbstract
import javax.inject.Inject


const val CHANGE_SERVER_URL_DIALOG_TAG: String = "CHANGE_SERVER_URL_DIALOG_TAG"

class ChangeServerUrlDialog() : DialogFragment(), ChangeServerURLView {

    private lateinit var binding: DialogChangeServerUrlBinding

    @Inject
    lateinit var presenter: ChangeServerURLPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Light_NoActionBar)
        app().createChangeServerULComponent(
            ChangeServerURLModule(
                requireActivity().applicationContext,
                this
            )
        ).inject(this)
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
        binding = DialogChangeServerUrlBinding.inflate(layoutInflater, container, false)
        binding.closeButton.apply {
            setOnClickListener { closeDialog() }
        }
        binding.dialogOk.setOnClickListener {
            binding.root.closeKeyboard()
            presenter.save()
        }
        binding.presenter = presenter

        presenter.init()

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val window = dialog!!.window
        window!!.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        window.setGravity(Gravity.CENTER)
    }

    override fun closeDialog() {
        dismissAllowingStateLoss()
    }

    override fun requestConfirmation() {
        binding.serverUrl.visibility = View.GONE
        binding.dialogWarning.visibility = View.VISIBLE
        binding.dialogOk.text = getString(R.string.action_accept)
    }

    override fun showEditMode() {
        binding.serverUrl.visibility = View.VISIBLE
        binding.dialogWarning.visibility = View.GONE
        binding.dialogOk.text = getString(R.string.action_ok)
    }

    override fun renderServerUrl(url: String) {
        binding.serverUrlEdit.setText(url)
    }

    override fun enableOk() {
        binding.dialogOk.isEnabled = true
    }

    override fun disableOk() {
        binding.dialogOk.isEnabled = false
    }

    override fun dismiss() {
        app().releaseSessionComponent()
        dismissAllowingStateLoss()
    }

    override fun getAbstractContext(): ActivityGlobalAbstract {
        return activity as ActivityGlobalAbstract
    }

    override fun showLoginProgress() {
        binding.serverUrl.visibility = View.GONE
        binding.dialogWarning.visibility = View.GONE
        binding.progress.visibility = View.VISIBLE
    }

    override fun hideLoginProgress() {
        binding.progress.visibility = View.GONE
        binding.dialogWarning.visibility = View.GONE
        binding.serverUrl.visibility = View.VISIBLE
    }

    override fun renderError(throwable: Throwable) {
        val error = D2ErrorUtils(getAbstractContext(), NetworkUtils(getAbstractContext())).getErrorMessage(throwable)

        Toast.makeText(
            getAbstractContext(),
            error,
            Toast.LENGTH_LONG
        ).show()
    }

    override fun renderSuccess(message: String) {
        Toast.makeText(
            getAbstractContext(),
            message,
            Toast.LENGTH_LONG
        ).show()
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (manager.findFragmentByTag(tag) == null) {
            manager.beginTransaction().add(this, tag).commitAllowingStateLoss()
        }
    }
}
