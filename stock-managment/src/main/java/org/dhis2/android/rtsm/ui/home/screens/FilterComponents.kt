package org.dhis2.android.rtsm.ui.home.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.data.OperationState
import org.dhis2.android.rtsm.data.TransactionType
import org.dhis2.android.rtsm.data.models.TransactionItem
import org.dhis2.android.rtsm.ui.home.HomeViewModel
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

@Composable
fun FilterList(
    viewModel: HomeViewModel,
    themeColor: Color
) {
    val facilities = viewModel.facilities.collectAsState().value
    val destinations = viewModel.destinationsList.collectAsState().value
    val showDestination = viewModel.isDistribution.collectAsState().value

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn() {
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
                    getFacilities(facilities)
                )
            }

            if (showDestination) {
                if (destinations is OperationState.Success<*>) {
                    val result = destinations.result as List<Option>
                    item {
                        DropdownComponentDistributedTo(
                            viewModel,
                            themeColor,
                            result
                        )
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
