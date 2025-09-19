package org.dhis2.commons.orgunitselector

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.dhis2.commons.R
import org.dhis2.mobile.commons.coroutine.CoroutineTracker
import org.dhis2.mobile.commons.orgunit.OrgUnitSelectorScope
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.mobile.ui.designsystem.component.OrgBottomSheet
import javax.inject.Inject

const val ARG_SINGLE_SELECTION = "OUTreeFragment.ARG_SINGLE_SELECTION"
const val ARG_SCOPE = "OUTreeFragment.ARG_SCOPE"
const val ARG_PRE_SELECTED_OU = "OUTreeFragment.ARG_PRE_SELECTED_OU"

class OUTreeFragment : BottomSheetDialogFragment() {
    class Builder {
        private var preselectedOrgUnits = listOf<String>()
        private var singleSelection = false
        private var selectionListener: ((selectedOrgUnits: List<OrganisationUnit>) -> Unit) = {}
        private var orgUnitScope: OrgUnitSelectorScope = OrgUnitSelectorScope.UserSearchScope()
        private var ouTreeModel: OUTreeModel = OUTreeModel()

        fun withPreselectedOrgUnits(preselectedOrgUnits: List<String>) =
            apply {
                require(!(singleSelection && preselectedOrgUnits.size > 1)) {
                    throw IllegalArgumentException(
                        "Single selection only admits one pre-selected org. unit",
                    )
                }
                this.preselectedOrgUnits = preselectedOrgUnits
            }

        fun singleSelection() =
            apply {
                require(preselectedOrgUnits.size <= 1) {
                    throw IllegalArgumentException(
                        "Single selection only admits one pre-selected org. unit",
                    )
                }
                singleSelection = true
            }

        fun orgUnitScope(orgUnitScope: OrgUnitSelectorScope) =
            apply {
                this.orgUnitScope = orgUnitScope
            }

        fun onSelection(selectionListener: (selectedOrgUnits: List<OrganisationUnit>) -> Unit) =
            apply {
                this.selectionListener = selectionListener
            }

        fun withModel(model: OUTreeModel) =
            apply {
                ouTreeModel = model
            }

        fun build(): OUTreeFragment {
            CoroutineTracker.increment()
            return OUTreeFragment().apply {
                selectionCallback = selectionListener
                model = ouTreeModel
                arguments =
                    Bundle().apply {
                        putBoolean(ARG_SINGLE_SELECTION, singleSelection)
                        putSerializableScope(ARG_SCOPE, orgUnitScope)
                        putStringArrayList(ARG_PRE_SELECTED_OU, ArrayList(preselectedOrgUnits))
                    }
                CoroutineTracker.decrement()
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: OUTreeViewModelFactory

    private val viewmodel: OUTreeViewModel by viewModels { viewModelFactory }

    var selectionCallback: ((selectedOrgUnits: List<OrganisationUnit>) -> Unit) = {}

    private var model: OUTreeModel = OUTreeModel()

    override fun onAttach(context: Context) {
        super.onAttach(context)

        (context.applicationContext as OUTreeComponentProvider)
            .provideOUTreeComponent(
                OUTreeModule(
                    model = model,
                    preselectedOrgUnits =
                        requireArguments()
                            .getStringArrayList(ARG_PRE_SELECTED_OU)
                            ?.toList() ?: emptyList(),
                    singleSelection = requireArguments().getBoolean(ARG_SINGLE_SELECTION, false),
                    orgUnitSelectorScope = requireArguments().getSerializableScope(ARG_SCOPE)!!,
                ),
            )?.inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
        lifecycleScope.launch {
            viewmodel.finalSelectedOrgUnits.collect {
                if (it.isNotEmpty()) {
                    selectionCallback(it)
                    exitOuSelection()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View =
        ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val list by viewmodel.treeNodes.collectAsState()

                OrgBottomSheet(
                    title = viewmodel.model().title,
                    subtitle = viewmodel.model().subtitle,
                    headerTextAlignment = viewmodel.model().headerAlignment,
                    doneButtonText = viewmodel.model().doneButtonText,
                    doneButtonIcon = viewmodel.model().doneButtonIcon,
                    clearAllButtonText = stringResource(id = R.string.action_clear_all),
                    orgTreeItems = list,
                    onSearch = viewmodel::searchByName,
                    onDismiss = { cancelOuSelection() },
                    onItemClick = viewmodel::onOpenChildren,
                    onItemSelected = viewmodel::onOrgUnitCheckChanged,
                    onClearAll = if (viewmodel.model().showClearButton) viewmodel::clearAll else null,
                    onDone = { confirmOuSelection() },
                )
            }
        }

    private fun confirmOuSelection() {
        viewmodel.confirmSelection()
    }

    private fun cancelOuSelection() {
        selectionCallback(emptyList())
        exitOuSelection()
    }

    private fun exitOuSelection() {
        dismiss()
    }
}

data class OUTreeModel(
    val title: String? = null,
    val subtitle: String? = null,
    val headerAlignment: TextAlign = TextAlign.Center,
    val doneButtonIcon: ImageVector = Icons.Filled.Check,
    val doneButtonText: String? = null,
    val showClearButton: Boolean = true,
    val hideOrgUnits: List<OrganisationUnit>? = null,
)

private val json =
    Json {
        classDiscriminator = "type"
        encodeDefaults = true
    }

private fun Bundle.putSerializableScope(
    key: String,
    value: OrgUnitSelectorScope,
) {
    putString(key, json.encodeToString(value))
}

private fun Bundle.getSerializableScope(key: String): OrgUnitSelectorScope? =
    getString(key)?.let {
        json.decodeFromString<OrgUnitSelectorScope>(it)
    }
