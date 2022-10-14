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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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
    themeColor: Color,
    isFacilitySelected: (value: Boolean) -> Unit = {},
    isDestinationSelected: (value: Boolean) -> Unit = {}
): Dp {
    val facilities = viewModel.facilities.collectAsState().value
    val destinations = viewModel.destinationsList.collectAsState().value
    val showDestination = viewModel.isDistribution.collectAsState().value

    // get local density from composable
    val localDensity = LocalDensity.current
    var heightIs by remember {
        mutableStateOf(0.dp)
    }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        modifier = Modifier
            .onSizeChanged { coordinates ->
                heightIs = with(localDensity) { coordinates.height.toDp() }
            }
            .animateContentSize(
                animationSpec = tween(
                    delayMillis = 400,
                    easing = LinearOutSlowInEasing
                )
            )
            .onGloballyPositioned { coordinates ->
                heightIs = with(localDensity) { coordinates.size.height.toDp() }
            }
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
    return heightIs
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
