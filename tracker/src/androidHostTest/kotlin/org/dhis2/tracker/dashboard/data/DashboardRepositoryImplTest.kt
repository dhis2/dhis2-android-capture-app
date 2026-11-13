package org.dhis2.tracker.dashboard.data

import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.commons.coroutine.Dispatcher
import org.dhis2.mobile.commons.error.DomainError
import org.dhis2.mobile.commons.error.DomainErrorMapper
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.category.CategoryCombo
import org.hisp.dhis.android.core.category.CategoryOptionCombo
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.android.core.maintenance.D2ErrorComponent
import org.hisp.dhis.android.core.program.Program
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class DashboardRepositoryImplTest {
    private val d2: D2 = mock(defaultAnswer = Mockito.RETURNS_DEEP_STUBS)
    private val domainErrorMapper: DomainErrorMapper = mock()
    private val testDispatcher = UnconfinedTestDispatcher()
    private val repository =
        DashboardRepositoryImpl(
            d2 = d2,
            dispatcher =
                Dispatcher(
                    io = testDispatcher,
                    main = testDispatcher,
                    default = testDispatcher,
                ),
            domainErrorMapper = domainErrorMapper,
        )

    @Test
    fun `should return the enrollment category combo name when it is not default`() =
        runTest {
            mockProgram(enrollmentCategoryComboUid = "tbCaseTypeUid")
            mockCategoryCombo(
                uid = "tbCaseTypeUid",
                displayName = "TB case type",
                isDefault = false,
            )

            assertEquals("TB case type", repository.enrollmentCategoryComboLabel(PROGRAM_UID))
        }

    @Test
    fun `should return null when the enrollment category combo is the default one`() =
        runTest {
            mockProgram(enrollmentCategoryComboUid = "defaultUid")
            mockCategoryCombo(uid = "defaultUid", displayName = "default", isDefault = true)

            assertNull(repository.enrollmentCategoryComboLabel(PROGRAM_UID))
        }

    @Test
    fun `should return null when the program is not stored locally`() =
        runTest {
            whenever(
                d2
                    .programModule()
                    .programs()
                    .uid(PROGRAM_UID)
                    .blockingGet(),
            ) doReturn null

            assertNull(repository.enrollmentCategoryComboLabel(PROGRAM_UID))
        }

    @Test
    fun `should return null when the enrollment category combo is not stored locally`() =
        runTest {
            mockProgram(enrollmentCategoryComboUid = "tbCaseTypeUid")
            whenever(
                d2
                    .categoryModule()
                    .categoryCombos()
                    .uid(any())
                    .blockingGet(),
            ) doReturn null

            assertNull(repository.enrollmentCategoryComboLabel(PROGRAM_UID))
        }

    @Test
    fun `should return the attribute option combo name`() =
        runTest {
            whenever(
                d2
                    .categoryModule()
                    .categoryOptionCombos()
                    .uid(AOC_UID)
                    .blockingGet(),
            ) doReturn
                CategoryOptionCombo
                    .builder()
                    .uid(AOC_UID)
                    .displayName("Index, Global Fund")
                    .build()

            assertEquals(
                "Index, Global Fund",
                repository.enrollmentCategoryOptionComboLabel(AOC_UID),
            )
        }

    @Test
    fun `should return an empty label when the attribute option combo is not stored locally`() =
        runTest {
            whenever(
                d2
                    .categoryModule()
                    .categoryOptionCombos()
                    .uid(AOC_UID)
                    .blockingGet(),
            ) doReturn null

            assertEquals("", repository.enrollmentCategoryOptionComboLabel(AOC_UID))
        }

    @Test
    fun `should map an SDK error wrapped by a blocking call to a domain error`() =
        runTest {
            val d2Error =
                D2Error
                    .builder()
                    .errorCode(D2ErrorCode.UNEXPECTED)
                    .errorComponent(D2ErrorComponent.Database)
                    .errorDescription("description")
                    .build()
            val domainError = DomainError.DatabaseError("Database error")
            whenever(
                d2
                    .categoryModule()
                    .categoryOptionCombos()
                    .uid(AOC_UID)
                    .blockingGet(),
            ) doThrow RuntimeException(d2Error)
            whenever(domainErrorMapper.mapToDomainError(d2Error)) doReturn domainError

            val error =
                assertFailsWith<DomainError> { repository.enrollmentCategoryOptionComboLabel(AOC_UID) }

            assertEquals(domainError, error)
        }

    private fun mockProgram(enrollmentCategoryComboUid: String) {
        whenever(
            d2
                .programModule()
                .programs()
                .uid(PROGRAM_UID)
                .blockingGet(),
        ) doReturn
            Program
                .builder()
                .uid(PROGRAM_UID)
                .categoryCombo(ObjectWithUid.create("eventCategoryComboUid"))
                .enrollmentCategoryCombo(ObjectWithUid.create(enrollmentCategoryComboUid))
                .build()
    }

    private fun mockCategoryCombo(
        uid: String,
        displayName: String,
        isDefault: Boolean,
    ) {
        whenever(
            d2
                .categoryModule()
                .categoryCombos()
                .uid(uid)
                .blockingGet(),
        ) doReturn
            CategoryCombo
                .builder()
                .uid(uid)
                .displayName(displayName)
                .isDefault(isDefault)
                .build()
    }

    private companion object {
        const val PROGRAM_UID = "programUid"
        const val AOC_UID = "aocUid"
    }
}
