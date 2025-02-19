package org.dhis2.usescases.datasets.dataSetTable

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import org.dhis2.mobile.aggregates.di.mappers.toDataSetInstanceParameters
import org.dhis2.mobile.aggregates.ui.DataSetInstanceScreen
import org.dhis2.usescases.general.ActivityGlobalAbstract
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class DataSetInstanceActivity : ActivityGlobalAbstract() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DHIS2Theme {
                val useTwoPane = when (calculateWindowSizeClass(this).widthSizeClass) {
                    WindowWidthSizeClass.Medium -> false
                    WindowWidthSizeClass.Compact -> false
                    WindowWidthSizeClass.Expanded -> true
                    else -> false
                }

                DataSetInstanceScreen(
                    parameters = intent.toDataSetInstanceParameters(),
                    useTwoPane = useTwoPane,
                    onBackClicked = onBackPressedDispatcher::onBackPressed,
                    onSyncClicked = {},
                )
            }
        }
    }
}
