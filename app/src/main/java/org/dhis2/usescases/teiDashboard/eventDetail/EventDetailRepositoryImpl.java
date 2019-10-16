package org.dhis2.usescases.teiDashboard.eventDetail;

import android.database.Cursor;

import androidx.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.tuples.Pair;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramStageSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017.
 */

public class EventDetailRepositoryImpl implements EventDetailRepository {

    private final BriteDatabase briteDatabase;
    private final String eventUid;
    private final String teiUid;
    private final D2 d2;


    EventDetailRepositoryImpl(BriteDatabase briteDatabase, String eventUid, String teiUid, D2 d2) {
        this.briteDatabase = briteDatabase;
        this.eventUid = eventUid;
        this.teiUid = teiUid;
        this.d2 = d2;
    }

    @NonNull
    @Override
    public Observable<Event> eventModelDetail(String uid) {
        return Observable.fromCallable(() -> d2.eventModule().events.uid(uid).blockingGet()).filter(event -> event.deleted() == null || !event.deleted());
    }

    @NonNull
    @Override
    public Observable<List<ProgramStageSection>> programStageSection(String eventUid) {
        return eventModelDetail(eventUid)
                .map(event -> d2.programModule().programStageSections.byProgramStageUid().eq(event.programStage()).blockingGet());

    }

    @NonNull
    @Override
    public Observable<ProgramStage> programStage(String eventUid) {
        return eventModelDetail(eventUid)
                .map(event -> d2.programModule().programStages.uid(event.programStage()).blockingGet());
    }

    @Override
    public void deleteNotPostedEvent(String eventUid) {
        try {
            d2.eventModule().events.uid(eventUid).blockingDelete();
        } catch (D2Error d2Error) {
            Timber.e(d2Error);
        }
    }

    @Override
    public void deletePostedEvent(Event eventModel) {
        try {
            d2.eventModule().events.uid(eventModel.uid()).blockingDelete();
        } catch (D2Error d2Error) {
            Timber.e(d2Error);
        }
    }

    @NonNull
    @Override
    public Observable<String> orgUnitName(String eventUid) {
        return eventModelDetail(eventUid)
                .map(event -> d2.organisationUnitModule().organisationUnits.uid(event.organisationUnit()).blockingGet().displayName());
    }

    @NonNull
    @Override
    public Observable<OrganisationUnit> orgUnit(String eventUid) {
        return eventModelDetail(eventUid)
                .map(event -> d2.organisationUnitModule().organisationUnits.uid(event.organisationUnit()).blockingGet());
    }

    @Override
    public Observable<List<OrganisationUnit>> getOrgUnits() {
        return d2.organisationUnitModule().organisationUnits
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .byProgramUids(Collections.singletonList(getProgram(eventUid).blockingFirst().uid()))
                .withPrograms().get().toObservable();

    }

    @Override
    public Observable<Pair<String, List<CategoryOptionCombo>>> getCategoryOptionCombos() {
        return d2.eventModule().events.uid(eventUid).get()
                .flatMap(event -> d2.programModule().programs.uid(event.program()).get())
                .map(Program::categoryComboUid)
                .flatMap(catCombo -> d2.categoryModule().categoryCombos().uid(catCombo).get()
                        .map(categoryCombo -> Pair.create(
                                categoryCombo.name(),
                                d2.categoryModule().categoryOptionCombos().withCategoryOptions().byCategoryComboUid().eq(categoryCombo.uid()).orderByDisplayName(RepositoryScope.OrderByDirection.ASC).blockingGet()))
                ).toObservable();
    }

    @NonNull
    @Override
    public Flowable<EventStatus> eventStatus(String eventUid) {
        return Flowable.fromCallable(() -> d2.eventModule().events.uid(eventUid).blockingGet().status());
    }

    @Override
    public Observable<Program> getProgram(String eventUid) {
        return eventModelDetail(eventUid)
                .map(event -> d2.programModule().programs.uid(event.program()).blockingGet());
    }

    @Override
    public void saveCatOption(CategoryOptionCombo selectedOption) {
        try {
            d2.eventModule().events.uid(eventUid).setAttributeOptionComboUid(selectedOption.uid());
        } catch (D2Error d2Error) {
            Timber.e(d2Error);
        }
    }

    @Override
    public Observable<Boolean> isEnrollmentActive(String eventUid) {
        Event event = d2.eventModule().events.uid(eventUid).blockingGet();
        return Observable.fromCallable(() -> event == null || event.enrollment() == null || d2.enrollmentModule().enrollments.uid(event.enrollment()).blockingGet().status() == EnrollmentStatus.ACTIVE);
    }

    @Override
    public Observable<Program> getExpiryDateFromEvent(String eventUid) {
        return d2.eventModule().events.uid(eventUid).get()
                .flatMap(event -> d2.programModule().programs.uid(event.program()).get())
                .toObservable();
    }
}