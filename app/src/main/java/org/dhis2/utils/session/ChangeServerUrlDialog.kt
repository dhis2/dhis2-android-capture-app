package org.dhis2.utils.session

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import org.dhis2.Bindings.app
import org.dhis2.R
import org.dhis2.databinding.DialogChangeServerUrlBinding
import javax.inject.Inject

const val CHANGE_SERVER_URL_DIALOG_TAG: String = "CHANGE_SERVER_URL_DIALOG_TAG"

class ChangeServerUrlDialog() : DialogFragment(), ChangeServerURLView {

    private lateinit var binding: DialogChangeServerUrlBinding

    @Inject
    lateinit var presenter: ChangeServerURLPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Light_NoActionBar)
        app().createChangeServerULComponent(ChangeServerURLModule(this)).inject(this)
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
            visibility = View.GONE
            setOnClickListener { closeDialog() }
        }

        presenter.init()

        return binding.root
    }
    override fun onResume() {
        super.onResume()
        val window = dialog!!.window
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        window.setGravity(Gravity.CENTER)
    }

    override fun closeDialog() {
        dismissAllowingStateLoss()
    }

    override fun renderServerUrl(url: String) {
        binding.serverUrlEdit.setText(url)
    }

    override fun dismiss() {
        app().releaseSessionComponent()
        dismissAllowingStateLoss()
    }

    override fun show(manager: FragmentManager, tag: String?) {
        if (manager.findFragmentByTag(tag) == null) {
            manager.beginTransaction().add(this, tag).commitAllowingStateLoss()
        }
    }
}
