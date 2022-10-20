package org.dhis2.android.rtsm.ui.home.screens.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentManager
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.data.OperationState
import org.dhis2.android.rtsm.data.TransactionType
import org.dhis2.android.rtsm.data.models.TransactionItem
import org.dhis2.android.rtsm.ui.home.HomeActivity
import org.dhis2.android.rtsm.ui.home.HomeViewModel
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

@Composable
fun FilterList(
    viewModel: HomeViewModel,
    themeColor: Color,
    supportFragmentManager: FragmentManager,
    homeContext: HomeActivity,
    isFacilitySelected: (value: Boolean) -> Unit = {},
    isDestinationSelected: (value: Boolean) -> Unit = {}
) {
    val facilities = viewModel.facilities.collectAsState().value
    val destinations = viewModel.destinationsList.collectAsState().value
    val showDestination = viewModel.isDistribution.collectAsState().value

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            DropdownComponent(
                viewModel,
                themeColor,
                mapTransaction()
            )
        }

        item {
            DropdownComponentFacilities(
                viewModel,
                themeColor,
                supportFragmentManager,
                homeContext,
                getFacilities(facilities)
            ) { facility ->
                isFacilitySelected(facility.isNotEmpty())
            }
        }

        if (showDestination) {
            if (destinations is OperationState.Success<*>) {
                val result = destinations.result as List<Option>
                item {
                    DropdownComponentDistributedTo(
                        viewModel,
                        themeColor,
                        result
                    ) { destination ->
                        isDestinationSelected(destination.isNotEmpty())
                    }
                }
            }
        }
    }
}

private fun mapTransaction(): MutableList<TransactionItem> {
    return mutableListOf(
        TransactionItem(R.drawable.ic_distribution, TransactionType.DISTRIBUTION),
        TransactionItem(R.drawable.ic_discard, TransactionType.DISCARD),
        TransactionItem(R.drawable.ic_correction, TransactionType.CORRECTION)
    )
}

private fun getFacilities(ou: OperationState<List<OrganisationUnit>>?): List<OrganisationUnit> {
    return if (ou is OperationState.Success<*>) {
        ou.result as List<OrganisationUnit>
    } else emptyList()
}
