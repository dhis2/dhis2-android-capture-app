package org.dhis2.android.rtsm.ui.home.screens.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.data.OperationState
import org.dhis2.android.rtsm.data.TransactionType
import org.dhis2.android.rtsm.data.TransactionType.DISTRIBUTION
import org.dhis2.android.rtsm.data.models.TransactionItem
import org.dhis2.android.rtsm.ui.home.HomeViewModel
import org.dhis2.android.rtsm.ui.home.model.DataEntryUiState
import org.dhis2.android.rtsm.ui.home.model.EditionDialogResult
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

@Composable
fun FilterList(
    viewModel: HomeViewModel,
    dataEntryUiState: DataEntryUiState,
    themeColor: Color,
    supportFragmentManager: FragmentManager,
    launchDialog: (msg: Int, (result: EditionDialogResult) -> Unit) -> Unit,
    onTransitionSelected: (transition: TransactionType) -> Unit,
    onFacilitySelected: (facility: OrganisationUnit) -> Unit,
    onDestinationSelected: (destination: Option) -> Unit
) {
    val facilities = viewModel.facilities.collectAsState().value
    val destinations = viewModel.destinationsList.collectAsState().value
    val settingsUiState by viewModel.settingsUiState.collectAsState()
    val showDestination = settingsUiState.transactionType == DISTRIBUTION
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        modifier = Modifier
            .animateContentSize(
                animationSpec = tween(
                    delayMillis = 180,
                    easing = LinearOutSlowInEasing
                )
            )
    ) {
        item {
            DropdownComponentTransactions(
                settingsUiState,
                onTransitionSelected,
                dataEntryUiState.hasUnsavedData,
                themeColor,
                mapTransaction(),
                launchDialog
            )
        }

        item {
            DropdownComponentFacilities(
                settingsUiState,
                onFacilitySelected,
                dataEntryUiState.hasUnsavedData,
                themeColor,
                supportFragmentManager,
                getFacilities(facilities),
                launchDialog
            )
        }

        if (showDestination) {
            if (destinations is OperationState.Success<*>) {
                val result = destinations.result as List<Option>
                item {
                    DropdownComponentDistributedTo(
                        onDestinationSelected,
                        dataEntryUiState,
                        themeColor,
                        result,
                        launchDialog = launchDialog
                    )
                }
            }
        }
    }
}

private fun mapTransaction(): MutableList<TransactionItem> {
    return mutableListOf(
        TransactionItem(R.drawable.ic_distribution, DISTRIBUTION),
        TransactionItem(R.drawable.ic_discard, TransactionType.DISCARD),
        TransactionItem(R.drawable.ic_correction, TransactionType.CORRECTION)
    )
}

private fun getFacilities(ou: OperationState<List<OrganisationUnit>>?): List<OrganisationUnit> {
    return if (ou is OperationState.Success<*>) {
        ou.result as List<OrganisationUnit>
    } else {
        emptyList()
    }
}
