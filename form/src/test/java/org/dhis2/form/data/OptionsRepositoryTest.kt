package org.dhis2.form.data

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.option.Option
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.RETURNS_DEEP_STUBS
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class OptionsRepositoryTest {

    private lateinit var optionsRepository: OptionsRepository
    private val d2: D2 = mock(defaultAnswer = RETURNS_DEEP_STUBS)
    private val objectResult = Option.builder()
        .uid("OptionUid")
        .code("code")
        .name("optionName")
        .displayName("OptionDisplayName")
        .build()

    @Before
    fun setUp() {
        optionsRepository = OptionsRepository(d2)
        whenever(
            d2.optionModule().options(),
        ) doReturn mock()
        whenever(
            d2.optionModule().options()
                .byOptionSetUid(),
        ) doReturn mock()
        whenever(
            d2.optionModule().options()
                .byOptionSetUid().eq("optionSetUid"),
        ) doReturn mock()
    }

    @Test
    fun shouldReturnOptionByDisplayName() {
        whenever(
            d2.optionModule().options()
                .byOptionSetUid().eq("optionSetUid")
                .byDisplayName(),
        ) doReturn mock()
        whenever(
            d2.optionModule().options()
                .byOptionSetUid().eq("optionSetUid")
                .byDisplayName().eq("displayName"),
        ) doReturn mock()
        whenever(
            d2.optionModule().options()
                .byOptionSetUid().eq("optionSetUid")
                .byDisplayName().eq("displayName").one(),
        ) doReturn mock()
        whenever(
            d2.optionModule().options()
                .byOptionSetUid().eq("optionSetUid")
                .byDisplayName().eq("displayName").one()
                .blockingGet(),
        ) doReturn objectResult

        assertEquals(
            optionsRepository.getOptionByDisplayName("optionSetUid", "displayName"),
            objectResult,
        )
    }

    @Test
    fun shouldReturnOptionByCode() {
        whenever(
            d2.optionModule().options()
                .byOptionSetUid().eq("optionSetUid")
                .byCode(),
        ) doReturn mock()
        whenever(
            d2.optionModule().options()
                .byOptionSetUid().eq("optionSetUid")
                .byCode().eq("code"),
        ) doReturn mock()
        whenever(
            d2.optionModule().options()
                .byOptionSetUid().eq("optionSetUid")
                .byCode().eq("code").one(),
        ) doReturn mock()
        whenever(
            d2.optionModule().options()
                .byOptionSetUid().eq("optionSetUid")
                .byCode().eq("code").one()
                .blockingGet(),
        ) doReturn objectResult

        assertEquals(
            optionsRepository.getOptionByCode("optionSetUid", "code"),
            objectResult,
        )
    }
}
