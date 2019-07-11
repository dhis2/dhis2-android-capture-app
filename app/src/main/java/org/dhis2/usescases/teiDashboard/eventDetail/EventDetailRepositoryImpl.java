package org.dhis2.usescases.teiDashboard.eventDetail;

import android.content.ContentValues;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.tuples.Pair;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramStageSection;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;

import io.reactivex.Flowable;
import io.reactivex.Observable;

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

    /*@NonNull
    @Override
    public Observable<List<ProgramStageDataElementModel>> programStageDataElement(String eventUid) {
        String SELECT_PROGRAM_STAGE_DE = String.format(
                "SELECT %s.* FROM %s " +
                        "JOIN %s ON %s.%s =%s.%s " +
                        "WHERE %s.%s = ? " +
                        "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "'",
                ProgramStageDataElementModel.TABLE, ProgramStageDataElementModel.TABLE,
                EventModel.TABLE, EventModel.TABLE, EventModel.Columns.PROGRAM_STAGE, ProgramStageDataElementModel.TABLE, ProgramStageDataElementModel.Columns.PROGRAM_STAGE,
                EventModel.TABLE, EventModel.Columns.UID
        );
        return briteDatabase.createQuery(EventModel.TABLE, SELECT_PROGRAM_STAGE_DE, eventUid == null ? "" : eventUid)
                .mapToList(ProgramStageDataElementModel::create);
    }*/

    /*@NonNull
    @Override
    public Observable<List<TrackedEntityDataValueModel>> dataValueModelList(String eventUid) {
        String SELECT_TRACKED_ENTITY_DATA_VALUE_WITH_EVENT_UID = "SELECT * FROM " + TrackedEntityDataValueModel.TABLE + " WHERE " + TrackedEntityDataValueModel.Columns.EVENT + "=";
        String uid = eventUid == null ? "" : eventUid;
        return briteDatabase.createQuery(TrackedEntityDataValueModel.TABLE, SELECT_TRACKED_ENTITY_DATA_VALUE_WITH_EVENT_UID + "'" + uid + "'")
                .mapToList(TrackedEntityDataValueModel::create);
    }*/

    @NonNull
    @Override
    public Observable<ProgramStage> programStage(String eventUid) {
        return eventModelDetail(eventUid)
                .map(event -> d2.programModule().programStages.uid(event.programStage()).get());
    }

    @Override
    public void deleteNotPostedEvent(String eventUid) {
        String DELETE_WHERE = String.format(
                "%s.%s = ",
                EventModel.TABLE, EventModel.Columns.UID
        );
        String id = eventUid == null ? "" : eventUid;
        briteDatabase.delete(EventModel.TABLE, DELETE_WHERE + "'" + id + "'");
    }

    @Override
    public void deletePostedEvent(Event eventModel) {
        Date currentDate = Calendar.getInstance().getTime();
        EventModel event = EventModel.builder()
                .id(eventModel.id())
                .uid(eventModel.uid())
                .created(eventModel.created())
                .lastUpdated(currentDate)
                .eventDate(eventModel.eventDate())
                .dueDate(eventModel.dueDate())
                .enrollment(eventModel.enrollment())
                .program(eventModel.program())
                .programStage(eventModel.programStage())
                .organisationUnit(eventModel.organisationUnit())
                .status(eventModel.status())
                .state(State.TO_DELETE)
                .build();

        if (event != null) {
            briteDatabase.update(EventModel.TABLE, event.toContentValues(), EventModel.Columns.UID + " = ?", event.uid());
            updateTEi();
        }


        updateProgramTable(currentDate, eventModel.program());
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
                    for(OrganisationUnit organisationUnit : organisationUnits){
                        for (Program program : organisationUnit.programs()) {
                            if (program.uid().equals(programId))
                                programOrganisationUnits.add(organisationUnit);
                        }
                    }
                    return programOrganisationUnits;
                });
    }

    @Override
    public Observable<Pair<String, List<CategoryOptionComboModel>>> getCategoryOptionCombos() {
        String GET_CAT_COMBO_FROM_EVENT = "SELECT CategoryCombo.* FROM CategoryCombo " +
                "WHERE CategoryCombo.uid IN (" +
                "SELECT Program.categoryCombo FROM Program " +
                "JOIN Event ON Event.program = Program.uid " +
                "WHERE Event.uid = ? LIMIT 1)";
        String SELECT_CATEGORY_COMBO = String.format("SELECT * FROM %s WHERE %s.%s = ?",
                CategoryOptionComboModel.TABLE, CategoryOptionComboModel.TABLE, CategoryOptionComboModel.Columns.CATEGORY_COMBO);
        return briteDatabase.createQuery(CategoryComboModel.TABLE, GET_CAT_COMBO_FROM_EVENT, eventUid == null ? "" : eventUid)
                .mapToOne(CategoryComboModel::create)
                .flatMap(catCombo -> {
                    if (catCombo != null && !catCombo.isDefault())
                        return briteDatabase.createQuery(CategoryOptionComboModel.TABLE, SELECT_CATEGORY_COMBO, catCombo.uid()).mapToList(CategoryOptionComboModel::create)
                                .map(list -> Pair.create(catCombo.name(), list));
                    else
                        return Observable.just(Pair.create("", new ArrayList<>()));
                });
        /*return getProgram(eventUid)
                .map(program -> {
                    CategoryCombo catCombo = program.categoryCombo();
                    if (catCombo != null && !catCombo.isDefault())
                        return Pair.create(catCombo.name(), d2.categoryModule().categoryOptionCombos.byCategoryComboUid().eq(catCombo.uid()).get());
                    else
                        return Pair.create("", new ArrayList<>());
                });*/
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
    public void saveCatOption(CategoryOptionComboModel selectedOption) {
        ContentValues event = new ContentValues();
        event.put(EventModel.Columns.ATTRIBUTE_OPTION_COMBO, selectedOption.uid());
        event.put(EventModel.Columns.STATE, State.TO_UPDATE.name()); // TODO: Check if state is TO_POST
        // TODO: and if so, keep the TO_POST state

        briteDatabase.update(EventModel.TABLE, event, EventModel.Columns.UID + " = ?", eventUid == null ? "" : eventUid);
        updateTEi();
    }

    @Override
    public Observable<Boolean> isEnrollmentActive(String eventUid) {
        Event event = d2.eventModule().events.uid(eventUid).withAllChildren().get();
        return Observable.fromCallable(() -> event == null || event.enrollment() == null || d2.enrollmentModule().enrollments.uid(event.enrollment()).get().status() == EnrollmentStatus.ACTIVE);
    }

    private void updateProgramTable(Date lastUpdated, String programUid) {
       /* ContentValues program = new ContentValues();  TODO: Crash if active
        program.put(EnrollmentModel.Columns.LAST_UPDATED, BaseIdentifiableObject.DATE_FORMAT.format(lastUpdated));
        briteDatabase.update(ProgramModel.TABLE, program, ProgramModel.Columns.UID + " = ?", programUid);*/
    }

    private void updateTEi() {

        ContentValues tei = new ContentValues();
        tei.put(TrackedEntityInstanceModel.Columns.LAST_UPDATED, DateUtils.databaseDateFormat().format(Calendar.getInstance().getTime()));
        tei.put(TrackedEntityInstanceModel.Columns.STATE, State.TO_UPDATE.name());// TODO: Check if state is TO_POST
        // TODO: and if so, keep the TO_POST state
        briteDatabase.update(TrackedEntityInstanceModel.TABLE, tei, "uid = ?", teiUid);
    }
}