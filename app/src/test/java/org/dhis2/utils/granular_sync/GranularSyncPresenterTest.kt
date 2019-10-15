package org.dhis2.utils.granular_sync

import androidx.work.WorkManager
import io.reactivex.Single
import org.dhis2.data.schedulers.TrampolineProviderImpl
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.`object`.ReadOnlyOneObjectRepositoryFinalImpl
import org.hisp.dhis.android.core.arch.repositories.`object`.internal.ReadOnlyOneObjectRepositoryImpl
import org.hisp.dhis.android.core.arch.repositories.collection.ReadOnlyIdentifiableCollectionRepository
import org.hisp.dhis.android.core.common.Access
import org.hisp.dhis.android.core.common.DataAccess
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ObjectWithUid
import org.hisp.dhis.android.core.program.*
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType
import org.junit.Test
import org.mockito.BDDMockito.*
import org.mockito.Mockito
import org.mockito.Mockito.mock
import java.util.*

class GranularSyncPresenterTest {

    private val d2 = mock(D2::class.java)
    private val view = mock(GranularSyncContracts.View::class.java)
    private val schedulerProvider = TrampolineProviderImpl()
    private val workManager = mock(WorkManager::class.java)
    private val programRepoMock = mock(ReadOnlyOneObjectRepositoryFinalImpl::class.java)

    private val testProgram = getProgram()
    @Test
    fun simplePresenterTest() {
        //GIVEN
        val presenter = GranularSyncPresenterImpl(
                d2,
                schedulerProvider,
                SyncStatusDialog.ConflictType.PROGRAM,
                "test_uid",
                null,
                null,
                null,
                workManager
        )
        Mockito.`when`(d2.programModule()).thenReturn(mock(ProgramModule::class.java))
        Mockito.`when`(d2.programModule().programs).thenReturn(mock(ProgramCollectionRepository::class.java))
        Mockito.`when`(d2.programModule().programs.uid("test_uid")).thenReturn(programRepoMock as ReadOnlyOneObjectRepositoryFinalImpl<Program>?)
        Mockito.`when`(d2.programModule().programs.uid("test_uid").get()).thenReturn(Single.just(testProgram))
        //WHEN
        presenter.configure(view)
        //THEN
        then(view).should().showTitle("DISPLAY_NAME")
    }

    private fun getProgram(): Program {
        return Program.builder()
                .id(1L)
                .version(1)
                .onlyEnrollOnce(true)
                .enrollmentDateLabel("enrollment_date_label")
                .displayIncidentDate(false)
                .incidentDateLabel("incident_date_label")
                .registration(true)
                .selectEnrollmentDatesInFuture(true)
                .dataEntryMethod(false)
                .ignoreOverdueEvents(false)
                .selectIncidentDatesInFuture(true)
                .useFirstStageDuringRegistration(true)
                .displayFrontPageList(false)
                .programType(ProgramType.WITH_REGISTRATION)
                .relatedProgram(ObjectWithUid.create("program_uid"))
                .trackedEntityType(TrackedEntityType.builder().uid("tracked_entity_type").build())
                .categoryCombo(ObjectWithUid.create("category_combo_uid"))
                .access(Access.create(null, null, DataAccess.create(true, true)))
                .expiryDays(2)
                .completeEventsExpiryDays(3)
                .minAttributesRequiredToSearch(1)
                .maxTeiCountToReturn(2)
                .featureType(FeatureType.POINT)
                .accessLevel(AccessLevel.PROTECTED)
                .shortName("SHORT_NAME")
                .displayShortName("DISPLAY_SHORT_NAME")
                .description("DESCRIPTION")
                .displayDescription("DISPLAY_DESCRIPTION")
                .uid("test_uid")
                .code("CODE")
                .name("NAME")
                .displayName("DISPLAY_NAME")
                .created(Date())
                .lastUpdated(Date())
                .build()
    }
}