package org.dhis2.form.data

import org.dhis2.commons.reporting.CrashReportController
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.android.core.maintenance.D2ErrorComponent
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitMode
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito
import org.mockito.exceptions.base.MockitoException
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doReturnConsecutively
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class UniqueAttributeControllerTest {
    private val d2: D2 = Mockito.mock(D2::class.java, Mockito.RETURNS_DEEP_STUBS)
    private val crashReportController: CrashReportController = mock()
    private val uniqueAttributeController = UniqueAttributeController(d2, crashReportController)

    @Test
    fun `Check unique attribute locally without org unit context`() {
        val attributeValues: List<TrackedEntityAttributeValue> = listOf(
            mock {
                on { trackedEntityInstance() } doReturn "teiUid"
            }
        )
        mockTrackedEntityAttributeValues(attributeValues)
        val result = uniqueAttributeController.checkAttributeLocal(
            orgUnitScope = false,
            teiUid = "teiUid",
            attributeUid = "uid",
            attributeValue = "value"
        )
        assertTrue(!result)
    }

    @Test
    fun `Check unique attribute locally with org unit context`() {
        mockTeiOrgUnit(
            listOf(
                mock {
                    on { organisationUnit() } doReturn "enrollingOrgUnit"
                },
                mock {
                    on { organisationUnit() } doReturn "orgUnit2"
                }
            )
        )

        val attributeValues: List<TrackedEntityAttributeValue> = listOf(
            mock {
                on { trackedEntityInstance() } doReturn "teiUid2"
            }
        )
        mockTrackedEntityAttributeValues(attributeValues)
        val result = uniqueAttributeController.checkAttributeLocal(
            orgUnitScope = true,
            teiUid = "teiUid",
            attributeUid = "uid",
            attributeValue = "value"
        )
        assertTrue(result)
    }

    @Test
    fun `Check unique attribute online without org unit context`() {
        mockNoContextApiCall(
            listOf(
                mock {
                    on { uid() } doReturn "teiUid2"
                }
            )
        )
        val result = uniqueAttributeController.checkAttributeOnline(
            false,
            "programUid",
            "teiUid",
            "attributeUid",
            "attributeValue"
        )
        assertTrue(!result)
    }

    @Test
    fun `Check unique attribute online with org unit context`() {
        mockContextApiCall(emptyList())
        val result = uniqueAttributeController.checkAttributeOnline(
            true,
            "programUid",
            "teiUid",
            "attributeUid",
            "attributeValue"
        )
        assertTrue(result)
    }

    @Test
    fun `Should track error if api call throws exception`() {
        mockContextApiCall(emptyList(), true)
        uniqueAttributeController.checkAttributeOnline(
            true,
            "programUid",
            "teiUid",
            "attributeUid",
            "attributeValue"
        )
        verify(crashReportController).addBreadCrumb(any(), any())
    }

    private fun mockTeiOrgUnit(teiList: List<TrackedEntityInstance>) {
        whenever(
            d2.trackedEntityModule().trackedEntityInstances().uid(any()).blockingGet()
        ) doReturnConsecutively teiList
    }

    private fun mockTrackedEntityAttributeValues(result: List<TrackedEntityAttributeValue>) {
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityAttribute()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityAttribute().eq("uid")
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityAttribute().eq("uid")
                .byTrackedEntityInstance()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityAttribute().eq("uid")
                .byTrackedEntityInstance().neq("teiUid")
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityAttribute().eq("uid")
                .byTrackedEntityInstance().neq("teiUid")
                .byValue()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityAttribute().eq("uid")
                .byTrackedEntityInstance().neq("teiUid")
                .byValue().eq("value")
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityAttributeValues()
                .byTrackedEntityAttribute().eq("uid")
                .byTrackedEntityInstance().neq("teiUid")
                .byValue().eq("value")
                .blockingGet()
        ) doReturn result
    }

    private fun mockNoContextApiCall(teiList: List<TrackedEntityInstance>) {
        whenever(
            d2.trackedEntityModule().trackedEntityInstanceQuery().onlineOnly()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstanceQuery().onlineOnly()
                .allowOnlineCache()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstanceQuery().onlineOnly()
                .allowOnlineCache().eq(true)
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstanceQuery().onlineOnly()
                .allowOnlineCache().eq(true)
                .byOrgUnitMode()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstanceQuery().onlineOnly()
                .allowOnlineCache().eq(true)
                .byOrgUnitMode().eq(OrganisationUnitMode.ACCESSIBLE)
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstanceQuery().onlineOnly()
                .allowOnlineCache().eq(true)
                .byOrgUnitMode().eq(OrganisationUnitMode.ACCESSIBLE)
                .byProgram()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstanceQuery().onlineOnly()
                .allowOnlineCache().eq(true)
                .byOrgUnitMode().eq(OrganisationUnitMode.ACCESSIBLE)
                .byProgram().eq("programUid")
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstanceQuery().onlineOnly()
                .allowOnlineCache().eq(true)
                .byOrgUnitMode().eq(OrganisationUnitMode.ACCESSIBLE)
                .byProgram().eq("programUid")
                .byAttribute("attributeUid")
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstanceQuery().onlineOnly()
                .allowOnlineCache().eq(true)
                .byOrgUnitMode().eq(OrganisationUnitMode.ACCESSIBLE)
                .byProgram().eq("programUid")
                .byAttribute("attributeUid").eq("attributeValue")
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstanceQuery().onlineOnly()
                .allowOnlineCache().eq(true)
                .byOrgUnitMode().eq(OrganisationUnitMode.ACCESSIBLE)
                .byProgram().eq("programUid")
                .byAttribute("attributeUid").eq("attributeValue")
                .blockingGet()
        ) doReturn teiList
    }

    private fun mockContextApiCall(
        teiList: List<TrackedEntityInstance>,
        shouldThrowException: Boolean = false
    ) {
        mockTeiOrgUnit(
            listOf(
                mock {
                    on { organisationUnit() } doReturn "orgUnit"
                }
            )
        )
        whenever(
            d2.trackedEntityModule().trackedEntityInstanceQuery().onlineOnly()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstanceQuery().onlineOnly()
                .allowOnlineCache()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstanceQuery().onlineOnly()
                .allowOnlineCache().eq(true)
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstanceQuery().onlineOnly()
                .allowOnlineCache().eq(true)
                .byProgram()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstanceQuery().onlineOnly()
                .allowOnlineCache().eq(true)
                .byProgram().eq("programUid")
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstanceQuery().onlineOnly()
                .allowOnlineCache().eq(true)
                .byProgram().eq("programUid")
                .byAttribute("attributeUid")
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstanceQuery().onlineOnly()
                .allowOnlineCache().eq(true)
                .byProgram().eq("programUid")
                .byAttribute("attributeUid").eq("attributeValue")
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstanceQuery().onlineOnly()
                .allowOnlineCache().eq(true)
                .byProgram().eq("programUid")
                .byAttribute("attributeUid").eq("attributeValue")
                .byOrgUnitMode()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstanceQuery().onlineOnly()
                .allowOnlineCache().eq(true)
                .byProgram().eq("programUid")
                .byAttribute("attributeUid").eq("attributeValue")
                .byOrgUnitMode().eq(OrganisationUnitMode.DESCENDANTS)
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstanceQuery().onlineOnly()
                .allowOnlineCache().eq(true)
                .byProgram().eq("programUid")
                .byAttribute("attributeUid").eq("attributeValue")
                .byOrgUnitMode().eq(OrganisationUnitMode.DESCENDANTS)
                .byOrgUnits()
        ) doReturn mock()
        whenever(
            d2.trackedEntityModule().trackedEntityInstanceQuery().onlineOnly()
                .allowOnlineCache().eq(true)
                .byProgram().eq("programUid")
                .byAttribute("attributeUid").eq("attributeValue")
                .byOrgUnitMode().eq(OrganisationUnitMode.DESCENDANTS)
                .byOrgUnits().`in`("orgUnit")
        ) doReturn mock()
        if (shouldThrowException) {
            whenever(
                d2.trackedEntityModule().trackedEntityInstanceQuery().onlineOnly()
                    .allowOnlineCache().eq(true)
                    .byProgram().eq("programUid")
                    .byAttribute("attributeUid").eq("attributeValue")
                    .byOrgUnitMode().eq(OrganisationUnitMode.DESCENDANTS)
                    .byOrgUnits().`in`("orgUnit")
                    .blockingGet()
            ) doThrow MockitoException(
                "",
                D2Error.builder()
                    .errorComponent(D2ErrorComponent.Server)
                    .errorCode(D2ErrorCode.API_RESPONSE_PROCESS_ERROR)
                    .errorDescription("error")
                    .build()
            )
        } else {
            whenever(
                d2.trackedEntityModule().trackedEntityInstanceQuery().onlineOnly()
                    .allowOnlineCache().eq(true)
                    .byProgram().eq("programUid")
                    .byAttribute("attributeUid").eq("attributeValue")
                    .byOrgUnitMode().eq(OrganisationUnitMode.DESCENDANTS)
                    .byOrgUnits().`in`("orgUnit")
                    .blockingGet()
            ) doReturn teiList
        }
    }
}
