package org.dhis2.usescases.searchTrackEntity.searchparameters

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.dhis2.usescases.searchTrackEntity.SearchTEIViewModel
import org.dhis2.usescases.searchTrackEntity.searchparameters.model.SearchParameter
import org.dhis2.usescases.searchTrackEntity.searchparameters.model.SearchParametersUiState
import org.dhis2.usescases.searchTrackEntity.searchparameters.provider.provideParameterSelectorItem
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.parameter.ParameterSelectorItem
import org.hisp.dhis.mobile.ui.designsystem.theme.Shape
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

@Composable
fun SearchParametersScreen(
    uiState: SearchParametersUiState,
    onValueChange: (uid: String, value: String?) -> Unit,
    onSearchClick: () -> Unit,
    onClear: () -> Unit,
) {
    val scaffoldState = rememberScaffoldState()
    val snackBarHostState = scaffoldState.snackbarHostState
    val coroutineScope = rememberCoroutineScope()

    uiState.minAttributesMessage?.let { message ->
        coroutineScope.launch {
            uiState.shouldShowMinAttributeWarning.collectLatest {
                if (it) {
                    snackBarHostState.showSnackbar(
                        message = message,
                        duration = SnackbarDuration.Short,
                    )
                }
            }
        }
    }

    Scaffold(
        backgroundColor = Color.Transparent,
        scaffoldState = scaffoldState,
        snackbarHost = {
            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier.padding(
                    start = 8.dp,
                    top = 8.dp,
                    end = 8.dp,
                    bottom = 48.dp,
                ),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.White, shape = Shape.LargeTop)
                .padding(it),
        ) {
            Column(
                modifier = Modifier
                    .weight(1F)
                    .verticalScroll(rememberScrollState()),
            ) {
                uiState.items.forEach { searchParameter ->
                    ParameterSelectorItem(
                        model = provideParameterSelectorItem(
                            searchParameter = searchParameter,
                            onValueChange = onValueChange,
                        ),
                    )
                }

                Button(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                    style = ButtonStyle.TEXT,
                    text = "Clear search",
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Cancel,
                            contentDescription = "Clear search",
                            tint = SurfaceColor.Primary,
                        )
                    },
                ) {
                    onClear()
                }
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 8.dp, 16.dp, 8.dp),
                style = ButtonStyle.FILLED,
                text = "Search",
            ) {
                onSearchClick()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchFormPreview() {
    SearchParametersScreen(
        uiState = SearchParametersUiState(
            items = listOf(
                SearchParameter(
                    "uid1",
                    "Label 1",
                    ValueType.TEXT,
                ),
                SearchParameter(
                    "uid2",
                    "Label 2",
                    ValueType.TEXT,
                ),
            ),
        ),
        onValueChange = { _, _ -> },
        onSearchClick = {},
        onClear = {},
    )
}

fun initSearchScreen(
    composeView: ComposeView,
    viewModel: SearchTEIViewModel,
    program: String?,
    teiType: String,
    onClear: () -> Unit,
) {
    viewModel.fetchSearchParameters(
        programUid = program,
        teiTypeUid = teiType,
    )
    composeView.setContent {
        SearchParametersScreen(
            uiState = viewModel.uiState,
            onValueChange = viewModel::updateQuery,
            onSearchClick = viewModel::onSearchClick,
            onClear = {
                onClear()
                viewModel.clearQueryData()
            },
        )
    }
}
