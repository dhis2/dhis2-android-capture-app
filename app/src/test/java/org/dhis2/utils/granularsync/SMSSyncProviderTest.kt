package org.dhis2.utils.granularsync

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import org.dhis2.R
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.sync.ConflictType
import org.dhis2.commons.sync.SyncContext
import org.dhis2.usescases.sms.SmsSendingService
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.sms.domain.interactor.ConfigCase
import org.hisp.dhis.android.core.sms.domain.interactor.SmsSubmitCase
import org.hisp.dhis.android.core.sms.domain.repository.SmsRepository
import org.hisp.dhis.android.core.systeminfo.DHISVersionManager
import org.hisp.dhis.android.core.systeminfo.SMSVersion
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class SMSSyncProviderTest {
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val resources: ResourceManager = mock()
    private val smsSender: SmsSubmitCase = mock {
        on { convertEnrollment(any()) } doReturn mock()
        on { convertTrackerEvent(any()) } doReturn mock()
        on { convertSimpleEvent(any()) } doReturn mock()
        on { convertDataSet(any(), any(), any(), any()) } doReturn mock()
        on { send() } doReturn Observable.just(
            SmsRepository.SmsSendingState(0, 3),
            SmsRepository.SmsSendingState(1, 3),
            SmsRepository.SmsSendingState(2, 3),
            SmsRepository.SmsSendingState(3, 3),
        )
    }

    @Before
    fun setUp() {
        whenever(d2.smsModule().smsSubmitCase()) doReturn smsSender
    }

    @Test
    fun `should return sms enabled if has correct version`() {
        mockTrackerSMSVersion(SMSVersion.V2)
        mockModuleEnableStatus(true)

        val result = smsSyncProvider(ConflictType.TEI)
            .isSMSEnabled(true)

        assertTrue(result)
    }

    @Test
    fun `should return sms enabled if it is not tracker sync`() {
        mockModuleEnableStatus(true)

        val result = smsSyncProvider(ConflictType.TEI)
            .isSMSEnabled(false)

        assertTrue(result)
    }

    @Test
    fun `should return sms disabled if has incorrect version`() {
        mockTrackerSMSVersion(SMSVersion.V1)
        mockModuleEnableStatus(true)

        val result = smsSyncProvider(ConflictType.TEI)
            .isSMSEnabled(true)

        assertTrue(!result)
    }

    @Test
    fun `should return sms disabled`() {
        mockTrackerSMSVersion(SMSVersion.V2)
        mockModuleEnableStatus(false)

        val result = smsSyncProvider(ConflictType.TEI)
            .isSMSEnabled(true)

        assertTrue(!result)
    }

    @Test
    fun `should return single event task`() {
        mockIsTrackerEvent(false)
        whenever(smsSender.convertSimpleEvent(any()))doReturn Single.just(1)
        smsSyncProvider(ConflictType.EVENT).getConvertTask()
        verify(smsSender).convertSimpleEvent("uid")
    }

    @Test
    fun `should return tracker event task`() {
        mockIsTrackerEvent(true)
        whenever(smsSender.convertTrackerEvent(any()))doReturn Single.just(1)
        smsSyncProvider(ConflictType.EVENT).getConvertTask()
        verify(smsSender).convertTrackerEvent("uid")
    }

    @Test
    fun `should return enrollment task`() {
        mockEnrollmentExists(true)
        whenever(smsSender.convertEnrollment(any()))doReturn Single.just(1)
        smsSyncProvider(ConflictType.TEI).getConvertTask()
        verify(smsSender).convertEnrollment("uid")
    }

    @Test
    fun `should return enrollment error task`() {
        mockEnrollmentExists(false)
        smsSyncProvider(ConflictType.TEI).getConvertTask()
        verify(resources).getString(R.string.granular_sync_enrollments_empty)
    }

    @Test
    fun `should return data value task`() {
        whenever(smsSender.convertDataSet(any(), any(), any(), any()))doReturn Single.just(1)
        smsSyncProviderDataValue(ConflictType.DATA_VALUES).getConvertTask()
        verify(smsSender).convertDataSet(
            "uid",
            "orgUnitUid",
            "periodId",
            "attComboUid",
        )
    }

    @Test
    fun `should return error task`() {
        smsSyncProvider(ConflictType.PROGRAM).getConvertTask()
        verify(resources).getString(R.string.granular_sync_unsupported_task)
    }

    @Test
    fun `should send sms`() {
        mockSendingSMS(false)
        val statuses = mutableListOf<SmsSendingService.SendingStatus>()
        val testObserver = smsSyncProvider(ConflictType.TEI)
            .sendSms(
                {
                    statuses.add(it)
                },
                {
                    statuses.add(it)
                },
            )
            .test()
        testObserver
            .assertNoErrors()
            .assertComplete()
        statuses.apply {
            assertTrue(size == 5)
            assertTrue(first().state == SmsSendingService.State.STARTED)
            assertTrue(last().state == SmsSendingService.State.SENT)
        }
    }

    @Test
    fun `should send sms and wait for response`() {
        mockSendingSMS(true)
        mockConfirmationSMS()
        val statuses = mutableListOf<SmsSendingService.SendingStatus>()
        val testObserver = smsSyncProvider(ConflictType.TEI)
            .sendSms(
                {
                    statuses.add(it)
                },
                {
                    statuses.add(it)
                },
            )
            .test()
        testObserver
            .assertNoErrors()
            .assertComplete()
        statuses.apply {
            assertTrue(size == 7)
            assertTrue(first().state == SmsSendingService.State.STARTED)
            assertTrue(any { it.state == SmsSendingService.State.WAITING_RESULT })
            assertTrue(last().state == SmsSendingService.State.RESULT_CONFIRMED)
        }
    }

    @Test
    fun `should send sms and wait for response with timeout`() {
        mockSendingSMS(true)
        mockConfirmationSMSTimeout()
        val statuses = mutableListOf<SmsSendingService.SendingStatus>()
        val testObserver = smsSyncProvider(ConflictType.TEI)
            .sendSms(
                {
                    statuses.add(it)
                },
                {
                    statuses.add(it)
                },
            )
            .test()
        statuses.apply {
            assertTrue(size == 7)
            assertTrue(first().state == SmsSendingService.State.STARTED)
            assertTrue(any { it.state == SmsSendingService.State.WAITING_RESULT })
            assertTrue(last().state == SmsSendingService.State.WAITING_RESULT_TIMEOUT)
        }
    }

    private fun mockConfirmationSMS() {
        whenever(
            smsSender.checkConfirmationSms(any()),
        )doReturn Completable.complete()
    }

    private fun mockConfirmationSMSTimeout() {
        whenever(
            smsSender.checkConfirmationSms(any()),
        )doReturn Completable.error(
            SmsRepository.ResultResponseException(SmsRepository.ResultResponseIssue.TIMEOUT),
        )
    }

    private fun mockSendingSMS(waitingForResult: Boolean) {
        val smsConfig: ConfigCase.SmsConfig =
            mock { on { isWaitingForResult } doReturn waitingForResult }

        whenever(
            d2.smsModule().configCase(),
        ) doReturn mock()
        whenever(
            d2.smsModule().configCase().getSmsModuleConfig(),
        ) doReturn Single.just(smsConfig)
    }

    fun smsSyncProvider(conflictType: ConflictType) = SMSSyncProviderImpl(
        d2,
        when (conflictType) {
            ConflictType.ALL -> SyncContext.Global()
            ConflictType.PROGRAM -> SyncContext.TrackerProgram("uid")
            ConflictType.TEI -> SyncContext.TrackerProgramTei("uid")
            ConflictType.EVENT -> SyncContext.Event("uid")
            ConflictType.DATA_SET -> SyncContext.DataSet("uid")
            ConflictType.DATA_VALUES -> SyncContext.DataSetInstance(
                "uid",
                "periodId",
                "orgUnitUid",
                "attComboUid",
            )
        },
        resources,
    )

    fun smsSyncProviderDataValue(conflictType: ConflictType) = SMSSyncProviderImpl(
        d2,
        SyncContext.DataSetInstance(
            "uid",
            "periodId",
            "orgUnitUid",
            "attComboUid",
        ),
        resources,
    )

    private fun mockTrackerSMSVersion(version: SMSVersion) {
        val dhisVersionManager: DHISVersionManager = mock {
            on { getSmsVersion() } doReturn version
        }
        whenever(
            d2.systemInfoModule().versionManager(),
        ) doReturn dhisVersionManager
    }

    private fun mockEnrollmentExists(exists: Boolean) {
        whenever(
            d2.enrollmentModule().enrollments().uid("uid").blockingExists(),
        ) doReturn exists
    }

    private fun mockIsTrackerEvent(isTrackerEvent: Boolean) {
        val event: Event = mock {
            on { enrollment() } doReturn if (isTrackerEvent) "enrollmentUid" else null
        }

        whenever(
            d2.eventModule().events().uid("uid").blockingGet(),
        ) doReturn event
    }

    private fun mockModuleEnableStatus(isEnabled: Boolean) {
        val smsConfig: ConfigCase.SmsConfig = mock { on { isModuleEnabled } doReturn isEnabled }

        whenever(
            d2.smsModule().configCase(),
        ) doReturn mock()
        whenever(
            d2.smsModule().configCase().getSmsModuleConfig(),
        ) doReturn Single.just(smsConfig)
    }
}
