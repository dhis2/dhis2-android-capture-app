package org.dhis2.usescases.settings.models

import java.util.Date
import org.hisp.dhis.android.core.imports.ImportStatus
import org.hisp.dhis.android.core.imports.TrackerImportConflict
import org.hisp.dhis.android.core.maintenance.D2Error
import org.hisp.dhis.android.core.maintenance.D2ErrorCode
import org.hisp.dhis.android.core.maintenance.D2ErrorComponent
import org.hisp.dhis.android.core.maintenance.ForeignKeyViolation
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ErrorModelMapperTest {
    private lateinit var mapper: ErrorModelMapper

    @Before
    fun setUp() {
        mapper = ErrorModelMapper("Missing %s %s from %s %s")
    }

    @Test
    fun `Should map d2Error to errorViewModel`() {
        val createDate = Date()
        val result = mapper.map(
            D2Error.builder()
                .httpErrorCode(1)
                .errorCode(D2ErrorCode.API_RESPONSE_PROCESS_ERROR)
                .created(createDate)
                .errorDescription("Description")
                .errorComponent(D2ErrorComponent.Database)
                .build()
        )
        result.apply {
            assertTrue(this.errorCode == "1")
            assertTrue(this.creationDate == creationDate)
            assertTrue(this.errorComponent == D2ErrorComponent.Database.name)
            assertTrue(this.errorDescription == "Description")
        }
    }

    @Test
    fun `Should map conflict to errorViewModel`() {
        val createDate = Date()
        val result = mapper.map(
            TrackerImportConflict.builder()
                .errorCode("1")
                .created(createDate)
                .conflict("Description")
                .status(ImportStatus.ERROR)
                .build()
        )
        result.apply {
            assertTrue(this.errorCode == "1")
            assertTrue(this.creationDate == creationDate)
            assertTrue(this.errorComponent == ImportStatus.ERROR.name)
            assertTrue(this.errorDescription == "Description")
        }
    }

    @Test
    fun `Should map FK to errorViewModel`() {
        val createDate = Date()
        val result = mapper.map(
            ForeignKeyViolation.builder()
                .fromTable("TableA")
                .toTable("TableB")
                .notFoundValue("UIDB")
                .fromObjectUid("UIDA")
                .created(createDate)
                .build()
        )
        result.apply {
            assertTrue(this.errorCode == "FK")
            assertTrue(this.creationDate == creationDate)
            assertTrue(this.errorComponent == "")
            assertTrue(this.errorDescription == "Missing TableB UIDB from TableA UIDA")
        }
    }
}
