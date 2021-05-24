package org.dhis2.usescases.orgunitselector

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import javax.inject.Inject
import org.dhis2.Bindings.app
import org.dhis2.databinding.OuTreeFragmentBinding

const val ARG_SHOW_AS_DIALOG = "OUTreeFragment.ARG_SHOW_AS_DIALOG"

class OUTreeFragment private constructor() :
    DialogFragment(),
    OUTreeView,
    OrgUnitSelectorAdapter.OnOrgUnitClick {

    companion object {
        fun newInstance(showAsDialog: Boolean = false): OUTreeFragment {
            return OUTreeFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_SHOW_AS_DIALOG, showAsDialog)
                }
            }
        }
    }

    @Inject
    lateinit var presenter: OUTreePresenter

    private val orgUnitSelectorAdapter = OrgUnitSelectorAdapter(this)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        context.app().serverComponent()!!
            .plus(OUTreeModule(this))
            .inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showAsDialog()?.let { showAsDialog ->
            showsDialog = showAsDialog
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return OuTreeFragmentBinding.inflate(inflater, container, false).apply {
            menu.setOnClickListener {
                exitOuSelection()
            }
            search.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                    // Not used
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (s.toString().isEmpty()) {
                        presenter.onStartSearch.onNext(true)
                    } else {
                        presenter.onSearchListener.onNext(s.toString())
                    }
                }

                override fun afterTextChanged(s: Editable) {
                    // Not used
                }
            })
            clearAll.setOnClickListener {
                if (orgUnitRecycler.adapter != null) {
                    (orgUnitRecycler.adapter as OrgUnitSelectorAdapter).clearAll()
                }
            }
            orgUnitRecycler.adapter = orgUnitSelectorAdapter
        }.root
    }

    override fun onResume() {
        super.onResume()
        showAsDialog().takeIf { it == true }?.let {
            fixDialogSize(0.98, 0.9)
        }
        presenter.init()
    }

    override fun onPause() {
        super.onPause()
        presenter.onDestroy()
    }

    private fun showAsDialog() =
        arguments?.getBoolean(ARG_SHOW_AS_DIALOG, false)

    override fun setOrgUnits(organisationUnits: List<TreeNode>) {
        orgUnitSelectorAdapter.submitList(organisationUnits)
    }

    override fun addOrgUnits(location: Int, organisationUnits: List<TreeNode>) {
        orgUnitSelectorAdapter.addOrgUnits(location, organisationUnits)
    }

    override fun onOrgUnitClick(node: TreeNode, position: Int) {
        presenter.ouChildListener.onNext(Pair(position, node.content))
    }

    private fun exitOuSelection() {
        if (showAsDialog() == true) {
            dismiss()
        } else {
            activity?.apply {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }
}

fun DialogFragment.fixDialogSize(widthPercent: Double, heightPercent: Double) {
    val size = Point()
    dialog?.window?.apply {
        windowManager.defaultDisplay.getSize(size)

        setLayout(widthPercent of size.x, heightPercent of size.y)
        setGravity(Gravity.CENTER)
    }
}

private infix fun Double.of(value: Int) = (this * value).toInt()
