package org.dhis2.commons.orgunitselector

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
import org.dhis2.commons.databinding.OuTreeFragmentBinding
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

const val ARG_SHOW_AS_DIALOG = "OUTreeFragment.ARG_SHOW_AS_DIALOG"
const val ARG_PRE_SELECTED_OU = "OUTreeFragment.ARG_PRE_SELECTED_OU"

class OUTreeFragment private constructor() :
    DialogFragment(),
    OUTreeView,
    OrgUnitSelectorAdapter.OnOrgUnitClick {

    companion object {
        fun newInstance(
            showAsDialog: Boolean = false,
            preselectedOrgUnits: MutableList<String> = mutableListOf()
        ): OUTreeFragment {
            return OUTreeFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(ARG_SHOW_AS_DIALOG, showAsDialog)
                    putStringArrayList(ARG_PRE_SELECTED_OU, ArrayList(preselectedOrgUnits))
                }
            }
        }
    }

    @Inject
    lateinit var presenter: OUTreePresenter

    private val orgUnitSelectorAdapter by lazy {
        OrgUnitSelectorAdapter(
            this,
            selectedOrgUnits
        )
    }

    private val selectedOrgUnits by lazy {
        preSelectedOrgUnits().toMutableList()
    }

    var selectionCallback: OnOrgUnitSelectionFinished? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (context.applicationContext as OUTreeComponentProvider).provideOUTreeComponent(
            OUTreeModule(this, selectedOrgUnits)
        )?.inject(this)
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
    ): View {
        return OuTreeFragmentBinding.inflate(inflater, container, false).apply {
            acceptBtn.setOnClickListener {
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
                selectedOrgUnits.clear()
                presenter.rebuildCurrentList()
            }
            clearBtn.setOnClickListener {
                selectedOrgUnits.clear()
                dismiss()
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

    private fun preSelectedOrgUnits() =
        arguments?.getStringArrayList(ARG_PRE_SELECTED_OU)?.toList() ?: emptyList()

    override fun setOrgUnits(organisationUnits: List<TreeNode>) {
        orgUnitSelectorAdapter.submitList(organisationUnits)
    }

    override fun getCurrentList(): List<TreeNode> {
        return orgUnitSelectorAdapter.currentList
    }

    override fun onOrgUnitClick(node: TreeNode, position: Int) {
        presenter.ouChildListener.onNext(Pair(position, node.content))
    }

    override fun onOrgUnitSelected(organisationUnit: OrganisationUnit, isSelected: Boolean) {
        if (isSelected && !selectedOrgUnits.contains(organisationUnit.uid())) {
            selectedOrgUnits.add(organisationUnit.uid())
        } else if (!isSelected && selectedOrgUnits.contains(organisationUnit.uid())) {
            selectedOrgUnits.remove(organisationUnit.uid())
        }
        presenter.rebuildCurrentList()
    }

    private fun exitOuSelection() {
        selectionCallback?.onSelectionFinished(
            presenter.getOrgUnits(selectedOrgUnits)
        )
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
