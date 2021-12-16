package dhis2.org.analytics.charts.providers

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.period.PeriodType
import org.junit.Test
import org.mockito.Mockito

class PeriodStepProviderImplTest {
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val periodStepProvider = PeriodStepProviderImpl(d2)

    @Test
    fun `Should get correct period step`() {
        periodStepProvider.periodStep(PeriodType.Monthly)
        verify(d2.periodModule().periodHelper(), times(2)).blockingGetPeriodForPeriodTypeAndDate(
            any(),
            any(),
            any()
        )
    }
}
