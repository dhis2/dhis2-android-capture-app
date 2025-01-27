package org.dhis2.mobile.aggregates.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.dhis2.mobile.aggregates.data.DataFetcher
import org.dhis2.mobile.aggregates.ui.viewModel.DataSetTableViewModel
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme

@Preview(device = "id:pixel_8a")
@Composable
fun DataSetTableScreenPreview() {
    DHIS2Theme {
        DataSetInstanceScreen(
            DataSetTableViewModel(
                object : DataFetcher {
                    override fun test(): String {
                        return "test"
                    }
                },
            ),
            false,
        ) {}
    }
}

@Preview(device = "id:pixel_c")
@Composable
fun DataSetTableTabletScreenPreview() {
    DHIS2Theme {
        DataSetInstanceScreen(
            DataSetTableViewModel(
                object : DataFetcher {
                    override fun test(): String {
                        return "test"
                    }
                },
            ),
            true,
        ) {}
    }
}
