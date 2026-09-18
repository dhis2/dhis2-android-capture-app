package org.dhis2.usescases.settings.models

import kotlinx.coroutines.test.runTest
import org.dhis2.mobile.commons.error.HttpStatusMessageProvider
import org.hisp.dhis.android.core.imports.ImportStatus
import org.hisp.dhis.android.core.imports.TrackerImportConflict
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.android.core.maintenance.D2ErrorComponent
import org.hisp.dhis.android.core.maintenance.ForeignKeyViolation
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.Date

class ErrorModelMapperTest {
    private val httpErrorMessageProvider: HttpStatusMessageProvider = mock()
    private val mapper = ErrorModelMapper("Missing %s %s from %s %s", httpErrorMessageProvider)

    @Before
    fun setUp() = runTest {
        whenever(httpErrorMessageProvider.httpStatusMessage(any()))doReturn "Error label"
    }

    @Test
    fun `Should map d2Error to errorViewModel`() = runTest {
        val createDate = Date()
        val result =
            mapper.mapD2Error(
                listOf(
                    D2Error
                        .builder()
                        .httpErrorCode(1)
                        .errorCode(D2ErrorCode.API_RESPONSE_PROCESS_ERROR)
                        .created(createDate)
                        .errorDescription("Description")
                        .errorComponent(D2ErrorComponent.Database)
                        .build()
                )
            )
        result.first().apply {
            assertTrue(this.errorCode == "1 Error label")
            assertTrue(this.creationDate == createDate)
            assertTrue(this.errorComponent == D2ErrorComponent.Database.name)
            assertTrue(this.errorDescription == "Description")
        }
    }

    @Test
    fun `Should map description to errorViewModel`() {
        val createDate = Date()
        val result =
            mapper.map(
                TrackerImportConflict
                    .builder()
                    .errorCode("1")
                    .created(createDate)
                    .conflict("Conflict")
                    .displayDescription("Description")
                    .status(ImportStatus.ERROR)
                    .build(),
            )
        result.apply {
            assertTrue(this.errorCode == "1")
            assertTrue(this.creationDate == creationDate)
            assertTrue(this.errorComponent == ImportStatus.ERROR.name)
            assertTrue(this.errorDescription == "Description")
        }
    }

    @Test
    fun `Should map conflict to errorViewModel`() {
        val createDate = Date()
        val result =
            mapper.map(
                TrackerImportConflict
                    .builder()
                    .errorCode("1")
                    .created(createDate)
                    .conflict("Conflict")
                    .status(ImportStatus.ERROR)
                    .build(),
            )
        result.apply {
            assertTrue(this.errorCode == "1")
            assertTrue(this.creationDate == creationDate)
            assertTrue(this.errorComponent == ImportStatus.ERROR.name)
            assertTrue(this.errorDescription == "Conflict")
        }
    }

    @Test
    fun `Should map FK to errorViewModel`() {
        val createDate = Date()
        val result =
            mapper.map(
                ForeignKeyViolation
                    .builder()
                    .fromTable("TableA")
                    .toTable("TableB")
                    .notFoundValue("UIDB")
                    .fromObjectUid("UIDA")
                    .created(createDate)
                    .build(),
            )
        result.apply {
            assertTrue(this.errorCode == "FK")
            assertTrue(this.creationDate == creationDate)
            assertTrue(this.errorComponent == "")
            assertTrue(this.errorDescription == "Missing TableB UIDB from TableA UIDA")
        }
    }
}
