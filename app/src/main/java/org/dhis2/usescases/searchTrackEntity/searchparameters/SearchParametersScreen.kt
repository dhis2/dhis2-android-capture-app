package org.dhis2.usescases.searchTrackEntity.searchparameters

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.dhis2.usescases.searchTrackEntity.searchparameters.provider.provideParameterSelectorItem
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.parameter.ParameterSelectorItem
import org.hisp.dhis.mobile.ui.designsystem.theme.Shape

@Composable
fun SearchParametersScreen(viewModel: SearchParametersViewModel) {
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
                    model = provideParameterSelectorItem(searchParameter),
                )
            }
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 8.dp, 16.dp, 8.dp),
            style = ButtonStyle.FILLED,
            text = "Search",
        ) {
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchFormPreview() {
    SearchParametersScreen(
        SearchParametersViewModel(
            repository = SearchParametersRepository(),
        ),
    )
}

fun initSearchScreen(composeView: ComposeView, viewModel: SearchParametersViewModel) {
    composeView.setContent {
        SearchParametersScreen(viewModel)
    }
}
