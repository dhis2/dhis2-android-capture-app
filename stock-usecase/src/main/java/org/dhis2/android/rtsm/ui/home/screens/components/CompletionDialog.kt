package org.dhis2.android.rtsm.ui.home.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Snackbar
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import org.dhis2.android.rtsm.ui.home.model.DataEntryUiState

@Composable
fun CompletionDialog(dataEntryUiState: DataEntryUiState) {
    ConstraintLayout(
        modifier = Modifier
            .height(56.dp)
            .fillMaxSize(),
    ) {
        val (snackbar) = createRefs()
        val painterResource = painterResource(
            id = dataEntryUiState.snackBarUiState.icon,
        )
        Snackbar(
            backgroundColor = Color(
                colorResource(
                    id = dataEntryUiState.snackBarUiState.color,
                ).toArgb(),
            ),
            content = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        painter = painterResource,
                        contentDescription = "",
                        modifier = Modifier.padding(end = (11.23).dp),
                    )
                    Text(
                        text = stringResource(
                            id = dataEntryUiState.snackBarUiState.message,
                        ),
                    )
                }
            },
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .height(56.dp)
                .background(
                    shape = MaterialTheme.shapes.medium.copy(CornerSize(8.dp)),
                    color = Color(0xFF4CAF50),
                )
                .constrainAs(snackbar) {
                    bottom.linkTo(parent.bottom, margin = 16.dp)
                    start.linkTo(parent.start, margin = 16.dp)
                    end.linkTo(parent.end, margin = 16.dp)
                },
            action = {
            },
        )
    }
}

@Preview
@Composable
fun SnackBarPreview() {
    CompletionDialog(DataEntryUiState())
}
