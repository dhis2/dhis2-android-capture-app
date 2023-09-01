package org.dhis2.usescases.teiDashboard.domain

import org.dhis2.commons.data.EventCreationType.REFERAL
import org.dhis2.commons.data.EventCreationType.SCHEDULE
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.usescases.teiDashboard.data.ProgramConfigurationRepository
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.settings.ProgramConfigurationSetting
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetNewEventCreationTypeOptionsTest {

    private val resources: ResourceManager = mock {
        on { getString(any()) } doReturn ""
    }
    private val programConfigurationSetting: ProgramConfigurationSetting = mock()

    private val programConfigurationRepository: ProgramConfigurationRepository = mock {
        on { getConfigurationByProgram("uid") } doReturn programConfigurationSetting
    }
    val program: ObjectWithUid = mock {
        on { uid() } doReturn "uid"
    }
    val programStage: ProgramStage = mock {
        on { program() } doReturn program
    }

    private lateinit var getNewEventCreationTypeOptions: GetNewEventCreationTypeOptions

    @Before
    fun setUp() {
        getNewEventCreationTypeOptions =
            GetNewEventCreationTypeOptions(resources, programConfigurationRepository)
    }

    @Test
    fun shouldReturnScheduleTypeIfThereISNoDueDate() {
        whenever(programStage.hideDueDate()) doReturn false

        val result = getNewEventCreationTypeOptions.invoke(programStage)

        assertNotNull(result.find { it.type == SCHEDULE })
    }

    @Test
    fun shouldNotReturnScheduleTypeIfThereISDueDate() {
        whenever(programStage.hideDueDate()) doReturn true

        val result = getNewEventCreationTypeOptions.invoke(programStage)

        assertNull(result.find { it.type == SCHEDULE })
    }

    @Test
    fun shouldReturnReferralTypeIfDisableReferralIsNull() {
        whenever(programConfigurationSetting.disableReferrals()) doReturn null

        val result = getNewEventCreationTypeOptions.invoke(programStage)

        assertNotNull(result.find { it.type == REFERAL })
    }

    @Test
    fun shouldReturnReferralTypeIfDisableReferralIsFalse() {
        whenever(programConfigurationSetting.disableReferrals()) doReturn false

        val result = getNewEventCreationTypeOptions.invoke(programStage)

        assertNotNull(result.find { it.type == REFERAL })
    }

    @Test
    fun shouldReturnReferralTypeIfProgramConfigurationNotExists() {
        whenever(programConfigurationRepository.getConfigurationByProgram("uid")) doReturn null

        val result = getNewEventCreationTypeOptions.invoke(programStage)

        assertNotNull(result.find { it.type == REFERAL })
    }

    @Test
    fun shouldNotReturnReferralTypeIfDisableReferralIsTrue() {
        whenever(programConfigurationSetting.disableReferrals()) doReturn true

        val result = getNewEventCreationTypeOptions.invoke(programStage)

        assertNull(result.find { it.type == REFERAL })
    }
}
