package org.dhis2.commons.resources

import org.dhis2.commons.R
import org.dhis2.mobile.commons.providers.CustomLabelContext
import org.dhis2.mobile.commons.providers.CustomLabelProvider
import org.hisp.dhis.android.core.D2
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class EventResourcesProviderTest {
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val resourceManager: ResourceManager = mock()
    private val customLabelProvider: CustomLabelProvider = mock()

    private val eventResourcesProvider =
        EventResourcesProvider(d2, resourceManager, customLabelProvider)

    @Test
    fun `capitalizes custom event label at the start of a sentence`() {
        mockRawEventLabel(quantity = 1, label = "visit")
        whenever(resourceManager.getString(R.string.event_label_created)) doReturn "%s created"

        val result =
            eventResourcesProvider.formatWithProgramEventLabel(
                stringResource = R.string.event_label_created,
                programUid = PROGRAM_UID,
            )

        assertEquals("Visit created", result)
    }

    @Test
    fun `keeps custom event label as configured in the middle of a sentence`() {
        mockRawEventLabel(quantity = 1, label = "eVisit")
        whenever(resourceManager.getString(R.string.new_event_label)) doReturn "New %s"

        val result =
            eventResourcesProvider.formatWithProgramEventLabel(
                stringResource = R.string.new_event_label,
                programUid = PROGRAM_UID,
            )

        assertEquals("New eVisit", result)
    }

    @Test
    fun `keeps plural custom event label as configured after a quantity`() {
        mockRawEventLabel(quantity = 3, label = "visits")
        whenever(resourceManager.getString(R.string.new_event_label)) doReturn "%1\$d %2\$s"

        val result =
            eventResourcesProvider.formatWithProgramEventLabel(
                stringResource = R.string.new_event_label,
                programUid = PROGRAM_UID,
                quantity = 3,
                formatWithQuantity = true,
            )

        assertEquals("3 visits", result)
    }

    private fun mockRawEventLabel(
        quantity: Int,
        label: String,
    ) {
        whenever(
            customLabelProvider.blockingCustomEventLabel(
                CustomLabelContext.Program(PROGRAM_UID),
                quantity,
                true,
            ),
        ) doReturn label
    }

    companion object {
        private const val PROGRAM_UID = "programUid"
    }
}
