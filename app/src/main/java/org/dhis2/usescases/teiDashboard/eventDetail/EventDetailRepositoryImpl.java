package org.dhis2.usescases.teiDashboard.eventDetail;

import android.content.ContentValues;

import androidx.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.tuples.Pair;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramStageSection;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
        return Observable.fromCallable(() -> d2.eventModule().events.uid(uid).get()).filter(event -> event.state() != State.TO_DELETE);
    }

    @NonNull
    @Override
    public Observable<List<ProgramStageSection>> programStageSection(String eventUid) {
        return eventModelDetail(eventUid)
                .map(event -> d2.programModule().programStageSections.byProgramStageUid().eq(event.programStage()).get());

    }

    @NonNull
    @Override
    public Observable<ProgramStage> programStage(String eventUid) {
        return eventModelDetail(eventUid)
                .map(event -> d2.programModule().programStages.uid(event.programStage()).get());
    }

    @Override
    public void deleteNotPostedEvent(String eventUid) {
        try {
            d2.eventModule().events.uid(eventUid).delete();
        } catch (D2Error d2Error) {
            Timber.e(d2Error);
        }
    }

    @Override
    public void deletePostedEvent(Event eventModel) {
        try {
            d2.eventModule().events.uid(eventModel.uid()).delete();
        } catch (D2Error d2Error) {
            Timber.e(d2Error);
        }
    }

    @NonNull
    @Override
    public Observable<String> orgUnitName(String eventUid) {
        return eventModelDetail(eventUid)
                .map(event -> d2.organisationUnitModule().organisationUnits.uid(event.organisationUnit()).get().displayName());
    }

    @NonNull
    @Override
    public Observable<OrganisationUnit> orgUnit(String eventUid) {
        return eventModelDetail(eventUid)
                .map(event -> d2.organisationUnitModule().organisationUnits.uid(event.organisationUnit()).get());
    }

    @Override
    public Observable<List<OrganisationUnit>> getOrgUnits() {
        return Observable.fromCallable(() -> d2.organisationUnitModule().organisationUnits.withPrograms().get())
                .map(organisationUnits -> {
                    List<OrganisationUnit> programOrganisationUnits = new ArrayList<>();
                    String programId = d2.eventModule().events.uid(eventUid).get().program();
                    for (OrganisationUnit organisationUnit : organisationUnits) {
                        for (Program program : organisationUnit.programs()) {
                            if (program.uid().equals(programId))
                                programOrganisationUnits.add(organisationUnit);
                        }
                    }
                    return programOrganisationUnits;
                });
    }

    @Override
    public Observable<Pair<String, List<CategoryOptionCombo>>> getCategoryOptionCombos() {
        return d2.eventModule().events.uid(eventUid).getAsync()
                .flatMap(event -> d2.programModule().programs.uid(event.program()).getAsync())
                .map(Program::categoryComboUid)
                .flatMap(catCombo -> d2.categoryModule().categoryCombos.uid(catCombo).withAllChildren().getAsync()
                        .map(categoryCombo -> Pair.create(
                                categoryCombo.name(),
                                d2.categoryModule().categoryOptionCombos.byCategoryComboUid().eq(categoryCombo.uid()).orderByDisplayName(RepositoryScope.OrderByDirection.ASC).get()))
                ).toObservable();
    }

    @NonNull
    @Override
    public Flowable<EventStatus> eventStatus(String eventUid) {
        return Flowable.fromCallable(() -> d2.eventModule().events.uid(eventUid).get().status());
    }

    @Override
    public Observable<Program> getProgram(String eventUid) {
        return eventModelDetail(eventUid)
                .map(event -> d2.programModule().programs.uid(event.program()).get());
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
        Event event = d2.eventModule().events.uid(eventUid).withAllChildren().get();
        return Observable.fromCallable(() -> event == null || event.enrollment() == null || d2.enrollmentModule().enrollments.uid(event.enrollment()).get().status() == EnrollmentStatus.ACTIVE);
    }

    @Override
    public Observable<Program> getExpiryDateFromEvent(String eventUid) {
        return d2.eventModule().events.uid(eventUid).getAsync()
                .flatMap(event -> d2.programModule().programs.uid(event.program()).getAsync())
                .toObservable();
    }
}