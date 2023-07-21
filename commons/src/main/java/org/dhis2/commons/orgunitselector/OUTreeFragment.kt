package org.dhis2.commons.orgunitselector

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import javax.inject.Inject
import org.dhis2.ui.dialogs.orgunit.OrgUnitSelectorActions
import org.dhis2.ui.dialogs.orgunit.OrgUnitSelectorDialog
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

const val ARG_SHOW_AS_DIALOG = "OUTreeFragment.ARG_SHOW_AS_DIALOG"
const val ARG_SINGLE_SELECTION = "OUTreeFragment.ARG_SINGLE_SELECTION"
const val ARG_SCOPE = "OUTreeFragment.ARG_SCOPE"
const val ARG_PRE_SELECTED_OU = "OUTreeFragment.ARG_PRE_SELECTED_OU"

class OUTreeFragment private constructor() : DialogFragment() {

    class Builder {
        private var showAsDialog = false
        private var preselectedOrgUnits = listOf<String>()
        private var singleSelection = false
        private var selectionListener: ((selectedOrgUnits: List<OrganisationUnit>) -> Unit) = {}
        private var orgUnitScope: OrgUnitSelectorScope = OrgUnitSelectorScope.UserSearchScope()
        fun showAsDialog() = apply {
            showAsDialog = true
        }

        fun withPreselectedOrgUnits(preselectedOrgUnits: List<String>) = apply {
            if (singleSelection && preselectedOrgUnits.size > 1) {
                throw IllegalArgumentException(
                    "Single selection only admits one pre-selected org. unit"
                )
            }
            this.preselectedOrgUnits = preselectedOrgUnits
        }

        fun singleSelection() = apply {
            if (preselectedOrgUnits.size > 1) {
                throw IllegalArgumentException(
                    "Single selection only admits one pre-selected org. unit"
                )
            }
            singleSelection = true
        }

        fun orgUnitScope(orgUnitScope: OrgUnitSelectorScope) = apply {
            this.orgUnitScope = orgUnitScope
        }

        fun onSelection(selectionListener: (selectedOrgUnits: List<OrganisationUnit>) -> Unit) =
            apply {
                this.selectionListener = selectionListener
            }

        fun build(): OUTreeFragment {
            return OUTreeFragment().apply {
                selectionCallback = selectionListener
                arguments = Bundle().apply {
                    putBoolean(ARG_SHOW_AS_DIALOG, showAsDialog)
                    putBoolean(ARG_SINGLE_SELECTION, singleSelection)
                    putParcelable(ARG_SCOPE, orgUnitScope)
                    putStringArrayList(ARG_PRE_SELECTED_OU, ArrayList(preselectedOrgUnits))
                }
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: OUTreeViewModelFactory

    private val presenter: OUTreeViewModel by viewModels { viewModelFactory }

    var selectionCallback: ((selectedOrgUnits: List<OrganisationUnit>) -> Unit) = {}

    override fun onAttach(context: Context) {
        super.onAttach(context)

        (context.applicationContext as OUTreeComponentProvider).provideOUTreeComponent(
            OUTreeModule(
                preselectedOrgUnits = requireArguments().getStringArrayList(ARG_PRE_SELECTED_OU)
                    ?.toList() ?: emptyList(),
                singleSelection = requireArguments().getBoolean(ARG_SINGLE_SELECTION, false),
                orgUnitSelectorScope = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    requireArguments().getParcelable(
                        ARG_SCOPE,
                        OrgUnitSelectorScope::class.java
                    )!!
                } else {
                    requireArguments().getParcelable(
                        ARG_SCOPE
                    )!!
                }
            )
        )?.inject(this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showAsDialog().let { showAsDialog ->
            showsDialog = showAsDialog
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                Mdc3Theme {
                    val list by presenter.treeNodes.collectAsState()
                    OrgUnitSelectorDialog(
                        title = null,
                        items = list,
                        actions = object : OrgUnitSelectorActions {
                            override val onSearch: (String) -> Unit
                                get() = presenter::searchByName
                            override val onOrgUnitChecked:
                                (orgUnitUid: String, isChecked: Boolean) -> Unit
                                get() = presenter::onOrgUnitCheckChanged
                            override val onOpenOrgUnit: (orgUnitUid: String) -> Unit
                                get() = presenter::onOpenChildren
                            override val onDoneClick: () -> Unit
                                get() = this@OUTreeFragment::exitOuSelection
                            override val onCancelClick: () -> Unit
                                get() = this@OUTreeFragment::dismiss
                            override val onClearClick: () -> Unit
                                get() = presenter::clearAll
                        }
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        showAsDialog().takeIf { it }?.let {
            fixDialogSize(0.9, 0.9)
        }
    }

    private fun showAsDialog() = arguments?.getBoolean(ARG_SHOW_AS_DIALOG, false) ?: false

    private fun exitOuSelection() {
        selectionCallback(presenter.getOrgUnits())
        if (showAsDialog()) {
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
