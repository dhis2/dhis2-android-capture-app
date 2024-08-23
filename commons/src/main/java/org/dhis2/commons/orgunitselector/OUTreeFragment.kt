package org.dhis2.commons.orgunitselector

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.viewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.dhis2.commons.R
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.mobile.ui.designsystem.component.OrgBottomSheet
import javax.inject.Inject

const val ARG_SINGLE_SELECTION = "OUTreeFragment.ARG_SINGLE_SELECTION"
const val ARG_SCOPE = "OUTreeFragment.ARG_SCOPE"
const val ARG_PRE_SELECTED_OU = "OUTreeFragment.ARG_PRE_SELECTED_OU"

class OUTreeFragment() : BottomSheetDialogFragment() {

    class Builder {
        private var preselectedOrgUnits = listOf<String>()
        private var singleSelection = false
        private var selectionListener: ((selectedOrgUnits: List<OrganisationUnit>) -> Unit) = {}
        private var orgUnitScope: OrgUnitSelectorScope = OrgUnitSelectorScope.UserSearchScope()

        fun withPreselectedOrgUnits(preselectedOrgUnits: List<String>) = apply {
            if (singleSelection && preselectedOrgUnits.size > 1) {
                throw IllegalArgumentException(
                    "Single selection only admits one pre-selected org. unit",
                )
            }
            this.preselectedOrgUnits = preselectedOrgUnits
        }

        fun singleSelection() = apply {
            if (preselectedOrgUnits.size > 1) {
                throw IllegalArgumentException(
                    "Single selection only admits one pre-selected org. unit",
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
                        OrgUnitSelectorScope::class.java,
                    )!!
                } else {
                    requireArguments().getParcelable(
                        ARG_SCOPE,
                    )!!
                },
            ),
        )?.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val list by presenter.treeNodes.collectAsState()
                OrgBottomSheet(
                    clearAllButtonText = stringResource(id = R.string.action_clear_all),
                    orgTreeItems = list,
                    onSearch = presenter::searchByName,
                    onDismiss = { cancelOuSelection() },
                    onItemClick = presenter::onOpenChildren,
                    onItemSelected = presenter::onOrgUnitCheckChanged,
                    onClearAll = presenter::clearAll,
                    onDone = { confirmOuSelection() },
                )
            }
        }
    }

    private fun confirmOuSelection() {
        selectionCallback(presenter.getOrgUnits())
        exitOuSelection()
    }

    private fun cancelOuSelection() {
        selectionCallback(emptyList())
        exitOuSelection()
    }

    private fun exitOuSelection() {
        dismiss()
    }
}
