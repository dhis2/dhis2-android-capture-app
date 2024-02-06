package org.dhis2.usescases.searchTrackEntity.searchparameters

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.dhis2.usescases.searchTrackEntity.SearchDispatchers
import org.dhis2.usescases.searchTrackEntity.searchparameters.provider.provideParameterSelectorItem
import org.hisp.dhis.android.core.D2Manager
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.parameter.ParameterSelectorItem
import org.hisp.dhis.mobile.ui.designsystem.theme.Shape
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

@Composable
fun SearchParametersScreen(
    viewModel: SearchParametersViewModel,
    onValueChange: (uid: String, value: String?) -> Unit,
    onSearchClick: () -> Unit,
    onClear: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.White, shape = Shape.LargeTop),
    ) {
        Column(
            modifier = Modifier
                .weight(1F)
                .verticalScroll(rememberScrollState()),
        ) {
            viewModel.uiState.items.forEach { searchParameter ->
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

@Preview(showBackground = true)
@Composable
fun SearchFormPreview() {
    SearchParametersScreen(
        SearchParametersViewModel(
            repository = SearchParametersRepository(
                D2Manager.getD2(),
                SearchDispatchers(),
            ),
        ),
        onValueChange = { _, _ -> },
        onSearchClick = {},
        onClear = {},
    )
}

fun initSearchScreen(
    composeView: ComposeView,
    viewModel: SearchParametersViewModel,
    program: String?,
    teiType: String,
    onValueChange: (uid: String, value: String?) -> Unit,
    onSearchClick: () -> Unit,
    onClear: () -> Unit,
) {
    viewModel.fetchSearchParameters(
        programUid = program,
        teiTypeUid = teiType,
    )
    composeView.setContent {
        SearchParametersScreen(
            viewModel = viewModel,
            onValueChange = onValueChange,
            onSearchClick = onSearchClick,
            onClear = onClear,
        )
    }
}
