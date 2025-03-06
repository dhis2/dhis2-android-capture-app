package org.dhis2.mobile.aggregates.data

import androidx.compose.runtime.Composable
import org.hisp.dhis.mobile.ui.designsystem.component.CheckBoxData

interface OptionRepository {
    @Composable
    fun fetchOptionsMap(dataElementUid: String, selectedOptionCodes: List<String>): Map<String, CheckBoxData>
}
