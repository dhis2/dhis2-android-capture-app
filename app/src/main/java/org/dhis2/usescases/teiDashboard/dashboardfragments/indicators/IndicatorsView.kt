package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import org.dhis2.data.tuples.Trio
import org.dhis2.usescases.general.AbstractActivityContracts
import org.hisp.dhis.android.core.program.ProgramIndicator

interface IndicatorsView : AbstractActivityContracts.View {

    fun swapIndicators(indicators: List<Trio<ProgramIndicator, String, String>>)
}
