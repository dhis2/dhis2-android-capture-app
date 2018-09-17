package org.dhis2.usescases.teiDashboard.eventDetail;

import android.support.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.program.ProgramModel;
import org.hisp.dhis.android.core.program.ProgramStageDataElementModel;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.program.ProgramStageSectionModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;

/**
 * Created by ppajuelo on 02/11/2017.
 */

public class EventDetailRepositoryImpl implements EventDetailRepository {

    private static final String ORG_UNIT_NAME = "SELECT OrganisationUnit.displayName FROM OrganisationUnit " +
            "JOIN Event ON Event.organisationUnit = OrganisationUnit.uid " +
            "WHERE Event.uid = ?";

    private final BriteDatabase briteDatabase;
    private final String eventUid;


    EventDetailRepositoryImpl(BriteDatabase briteDatabase, String eventUid) {
        this.briteDatabase = briteDatabase;
        this.eventUid = eventUid;
    }

    @NonNull
    @Override
    public Observable<EventModel> eventModelDetail(String uid) {
        String SELECT_EVENT_WITH_UID = "SELECT * FROM " + EventModel.TABLE + " WHERE " + EventModel.Columns.UID + "='" + uid + "' " +
                "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "'";
        return briteDatabase.createQuery(EventModel.TABLE, SELECT_EVENT_WITH_UID)
                .mapToOne(EventModel::create);
    }

    @NonNull
    @Override
    public Observable<List<ProgramStageSectionModel>> programStageSection(String eventUid) {
        String SELECT_PROGRAM_STAGE_SECTIONS = String.format(
                "SELECT %s.* FROM %s " +
                        "JOIN %s ON %s.%s = %s.%s " +
                        "WHERE %s.%s = ? " +
                        "AND " + EventModel.TABLE + "." + EventModel.Columns.STATE + " != '" + State.TO_DELETE + "' " +
                        "ORDER BY %s.%s",
                ProgramStageSectionModel.TABLE, ProgramStageSectionModel.TABLE,
                EventModel.TABLE, EventModel.TABLE, EventModel.Columns.PROGRAM_STAGE, ProgramStageSectionModel.TABLE, ProgramStageSectionModel.Columns.PROGRAM_STAGE,
                EventModel.TABLE, EventModel.Columns.UID,
                ProgramStageSectionModel.TABLE, ProgramStageSectionModel.Columns.SORT_ORDER
        );
        return briteDatabase.createQuery(EventModel.TABLE, SELECT_PROGRAM_STAGE_SECTIONS, eventUid)
                .mapToList(ProgramStageSectionModel::create);
    }

    @NonNull
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
        return briteDatabase.createQuery(EventModel.TABLE, SELECT_PROGRAM_STAGE_DE, eventUid)
                .mapToList(ProgramStageDataElementModel::create);
    }

    @NonNull
    @Override
    public Observable<List<TrackedEntityDataValueModel>> dataValueModelList(String eventUid) {
        String SELECT_TRACKED_ENTITY_DATA_VALUE_WITH_EVENT_UID = "SELECT * FROM " + TrackedEntityDataValueModel.TABLE + " WHERE " + TrackedEntityDataValueModel.Columns.EVENT + "=";
        return briteDatabase.createQuery(TrackedEntityDataValueModel.TABLE, SELECT_TRACKED_ENTITY_DATA_VALUE_WITH_EVENT_UID + "'" + eventUid + "'")
                .mapToList(TrackedEntityDataValueModel::create);
    }

    @NonNull
    @Override
    public Observable<ProgramStageModel> programStage(String eventUid) {
        String query = "SELECT ProgramStage.* FROM ProgramStage " +
                "JOIN Event ON Event.programStage = ProgramStage.uid " +
                "WHERE Event.uid = ? LIMIT 1";
        return briteDatabase.createQuery(ProgramStageModel.TABLE, query, eventUid)
                .mapToOne(ProgramStageModel::create);
    }

    @Override
    public void deleteNotPostedEvent(String eventUid) {
        String DELETE_WHERE = String.format(
                "%s.%s = ",
                EventModel.TABLE, EventModel.Columns.UID
        );
        briteDatabase.delete(EventModel.TABLE, DELETE_WHERE + "'" + eventUid + "'");
    }

    @Override
    public void deletePostedEvent(EventModel eventModel) {
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

        briteDatabase.update(EventModel.TABLE, event.toContentValues(), EventModel.Columns.UID + " = ?", event.uid());

        updateProgramTable(currentDate, eventModel.program());
    }

    @NonNull
    @Override
    public Observable<String> orgUnitName(String eventUid) {
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, ORG_UNIT_NAME, eventUid)
                .mapToOne(cursor -> cursor.getString(0));
    }

    @Override
    public Observable<List<OrganisationUnitModel>> getOrgUnits() {
        String EVENT_ORG_UNITS = "SELECT OrganisationUnit.* FROM OrganisationUnit " +
                "JOIN OrganisationUnitProgramLink ON OrganisationUnitProgramLink.organisationUnit = OrganisationUnit.uid " +
                "JOIN Event ON Event.program = OrganisationUnitProgramLink.program " +
                "WHERE Event.uid = ?";
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, EVENT_ORG_UNITS, eventUid).mapToList(OrganisationUnitModel::create);
    }

    @Override
    public Observable<List<CategoryOptionComboModel>> getCategoryOptionCombos() {
        String GET_CAT_COMBO_FROM_EVENT = "SELECT Program.categoryCombo FROM Program " +
                "JOIN Event ON Event.program = Program.uid " +
                "WHERE Event.uid = ?";
        String SELECT_CATEGORY_COMBO = String.format("SELECT * FROM %s WHERE %s.%s = ?",
                CategoryOptionComboModel.TABLE, CategoryOptionComboModel.TABLE, CategoryOptionComboModel.Columns.CATEGORY_COMBO);
        return briteDatabase.createQuery(ProgramModel.TABLE, GET_CAT_COMBO_FROM_EVENT, eventUid)
                .mapToOne(cursor -> cursor.getString(0))
                .flatMap(catCombo -> {
                    if (catCombo != null)
                        return briteDatabase.createQuery(CategoryOptionComboModel.TABLE, SELECT_CATEGORY_COMBO, catCombo).mapToList(CategoryOptionComboModel::create);
                    else
                        return Observable.just(new ArrayList<>());
                });
    }

    @NonNull
    @Override
    public Flowable<EventStatus> eventStatus(String eventUid) {
        return briteDatabase.createQuery(EventModel.TABLE, "SELECT Event.status FROM Event WHERE Event.uid = ?", eventUid)
                .mapToOne(cursor -> EventStatus.valueOf(cursor.getString(0))).toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Observable<ProgramModel> getProgram(String eventUid) {
        return briteDatabase.createQuery(ProgramModel.TABLE, "SELECT Program.* FROM Program JOIN Event ON Event.program = Program.uid WHERE Event.uid = ?", eventUid)
                .mapToOne(ProgramModel::create);
    }

    private void updateProgramTable(Date lastUpdated, String programUid) {
       /* ContentValues program = new ContentValues();  TODO: Crash if active
        program.put(EnrollmentModel.Columns.LAST_UPDATED, BaseIdentifiableObject.DATE_FORMAT.format(lastUpdated));
        briteDatabase.update(ProgramModel.TABLE, program, ProgramModel.Columns.UID + " = ?", programUid);*/
    }
}