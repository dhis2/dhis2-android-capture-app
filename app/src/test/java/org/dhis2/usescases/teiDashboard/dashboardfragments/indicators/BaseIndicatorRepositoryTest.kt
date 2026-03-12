package org.dhis2.usescases.teiDashboard.dashboardfragments.indicators

import org.dhis2.usescases.teiDashboard.dashboardfragments.indicators.BaseIndicatorRepository.Companion.getNumberValue
import org.junit.Test

class BaseIndicatorRepositoryTest {

    @Test
    fun `Should parse number value`() {
        assert(getNumberValue("54") == 54.0)
        assert(getNumberValue("54.3") == 54.3)
        assert(getNumberValue("-12.2") == -12.2)
        assert(getNumberValue("23456.6") == 23456.6)
        assert(getNumberValue("23,456.6") == 23456.6)
        assert(getNumberValue("54.1%") == 54.1)
        assert(getNumberValue("54.1 %") == 54.1)
        assert(getNumberValue("<14322>") == 14322.0)
    }
}
