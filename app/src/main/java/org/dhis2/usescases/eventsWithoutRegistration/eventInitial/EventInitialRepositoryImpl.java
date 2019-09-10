package org.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.utils.CodeGenerator;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.ProgramStageModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceModel;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by Cristian on 22/03/2018.
 */

public class EventInitialRepositoryImpl implements EventInitialRepository {

    private final BriteDatabase briteDatabase;
    private final CodeGenerator codeGenerator;
    private final String eventUid;
    private final D2 d2;

    EventInitialRepositoryImpl(CodeGenerator codeGenerator, BriteDatabase briteDatabase, String eventUid, D2 d2) {
        this.briteDatabase = briteDatabase;
        this.codeGenerator = codeGenerator;
        this.eventUid = eventUid;
        this.d2 = d2;
    }


    @NonNull
    @Override
    public Observable<Event> event(String eventId) {
        return Observable.fromCallable(() -> d2.eventModule().events.uid(eventId).get()).filter(event -> event.state() != State.TO_DELETE);
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnit>> filteredOrgUnits(String date, String programId, String parentId) {
        if (date == null)
            return parentId == null ? orgUnits(programId) : orgUnits(programId, parentId);
        else
            return (parentId == null ? orgUnits(programId) : orgUnits(programId, parentId))
                    .map(organisationUnits -> {
                        Iterator<OrganisationUnit> iterator = organisationUnits.iterator();
                        while (iterator.hasNext()) {
                            OrganisationUnit organisationUnit = iterator.next();
                            if (organisationUnit.openingDate() != null && organisationUnit.openingDate().after(DateUtils.uiDateFormat().parse(date))
                                    || organisationUnit.closedDate() != null && organisationUnit.closedDate().before(DateUtils.uiDateFormat().parse(date)))
                                iterator.remove();
                        }
                        return organisationUnits;
                    });
    }

    @NonNull
    public Observable<List<OrganisationUnit>> orgUnits(String programUid) {

        List<String> ouUids = new ArrayList<>();

        try (Cursor orgUnitCursor = d2.databaseAdapter().query("SELECT organisationUnit FROM OrganisationUnitProgramLink WHERE program = ?", programUid)) {
            orgUnitCursor.moveToFirst();
            for(int i = 0; i<orgUnitCursor.getCount();i++){
                ouUids.add(orgUnitCursor.getString(0));
                orgUnitCursor.moveToNext();
            }
        }

        return d2.organisationUnitModule().organisationUnits
                .byUid().in(ouUids)
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .withPrograms()
                .getAsync().toObservable();

     /*   return Observable.fromCallable(() -> {
            int level = 1;
            while (d2.organisationUnitModule().organisationUnits.byLevel().eq(level)
                    .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE).withPrograms().count() < 1)
                level++;

            return d2.organisationUnitModule().organisationUnits.byLevel().eq(level)
                    .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE).withPrograms().get();

        });*/
    }


    public Observable<List<OrganisationUnit>> orgUnits(String programId, String parentUid) {
        return Observable.fromCallable(() ->
                d2.organisationUnitModule().organisationUnits
                        .byParentUid().eq(parentUid)
                        .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                        .withPrograms().get())

                .map(organisationUnits -> {
                    List<OrganisationUnit> programOrganisationUnits = new ArrayList<>();
                    for (OrganisationUnit organisationUnit : organisationUnits) {
                        if (UidsHelper.getUids(organisationUnit.programs()).contains(programId))
                            programOrganisationUnits.add(organisationUnit);
                    }
                    return programOrganisationUnits.isEmpty() ? organisationUnits : programOrganisationUnits;
                });
    }

    @NonNull
    @Override
    public Observable<CategoryCombo> catCombo(String programUid) {
        return Observable.defer(() -> Observable.just(d2.categoryModule().categoryCombos.uid(d2.programModule().programs.uid(programUid).get().categoryCombo().uid()).withAllChildren().get()))
                .map(categoryCombo -> {
                    List<Category> fullCategories = new ArrayList<>();
                    List<CategoryOptionCombo> fullOptionCombos = new ArrayList<>();
                    for (Category category : categoryCombo.categories()) {
                        fullCategories.add(d2.categoryModule().categories.uid(category.uid()).withAllChildren().get());
                    }
                    for (CategoryOptionCombo categoryOptionCombo : categoryCombo.categoryOptionCombos())
                        fullOptionCombos.add(d2.categoryModule().categoryOptionCombos.uid(categoryOptionCombo.uid()).withAllChildren().get());
                    return categoryCombo.toBuilder().categories(fullCategories).categoryOptionCombos(fullOptionCombos).build();
                });
    }

    @Override
    public Flowable<Map<String, CategoryOption>> getOptionsFromCatOptionCombo(String eventId) {
        return Flowable.just(d2.eventModule().events.uid(eventUid).get())
                .flatMap(event -> catCombo(event.program()).toFlowable(BackpressureStrategy.LATEST)
                        .flatMap(categoryCombo -> {
                            Map<String, CategoryOption> map = new HashMap<>();
                            if (!categoryCombo.isDefault() && event.attributeOptionCombo() != null) {
                                List<CategoryOption> selectedCatOptions = d2.categoryModule().categoryOptionCombos.uid(event.attributeOptionCombo()).withAllChildren().get().categoryOptions();
                                for (Category category : categoryCombo.categories()) {
                                    for (CategoryOption categoryOption : selectedCatOptions)
                                        if (category.categoryOptions().contains(categoryOption))
                                            map.put(category.uid(), categoryOption);
                                }
                            }

                            return Flowable.just(map);
                        }));
    }

    @Override
    public Date getStageLastDate(String programStageUid, String enrollmentUid) {
        List<Event> activeEvents = d2.eventModule().events.byEnrollmentUid().eq(enrollmentUid).byProgramStageUid().eq(programStageUid)
                .orderByEventDate(RepositoryScope.OrderByDirection.DESC).get();
        List<Event> scheduleEvents = d2.eventModule().events.byEnrollmentUid().eq(enrollmentUid).byProgramStageUid().eq(programStageUid)
                .orderByDueDate(RepositoryScope.OrderByDirection.DESC).get();

        Date activeDate = null;
        Date scheduleDate = null;
        if (!activeEvents.isEmpty()) {
            activeDate = activeEvents.get(0).eventDate();
        }
        if (!scheduleEvents.isEmpty())
            scheduleDate = scheduleEvents.get(0).dueDate();

        if (activeDate != null && scheduleDate != null) {
            return activeDate.before(scheduleDate) ? scheduleDate : activeDate;
        } else if (activeDate != null) {
            return activeDate;
        } else if (scheduleDate != null) {
            return scheduleDate;
        } else {
            return Calendar.getInstance().getTime();
        }
    }

    @Override
    public Observable<String> createEvent(String enrollmentUid, @Nullable String trackedEntityInstanceUid,
                                          @NonNull Context context, @NonNull String programUid,
                                          @NonNull String programStage, @NonNull Date date,
                                          @NonNull String orgUnitUid, @Nullable String categoryOptionsUid,
                                          @Nullable String categoryOptionComboUid, @NonNull String latitude, @NonNull String longitude) {


        Date createDate = Calendar.getInstance().getTime();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);


        String uid = codeGenerator.generate();

        EventModel eventModel = EventModel.builder()
                .uid(uid)
                .enrollment(enrollmentUid)
                .created(createDate)
                .lastUpdated(createDate)
                .status(EventStatus.ACTIVE)
                .latitude(latitude)
                .longitude(longitude)
                .program(programUid)
                .programStage(programStage)
                .organisationUnit(orgUnitUid)
                .eventDate(cal.getTime())
                .completedDate(null)
                .dueDate(null)
                .state(State.TO_POST)
                .attributeOptionCombo(categoryOptionComboUid)
                .build();

        long row = -1;

        try {
            row = briteDatabase.insert(EventModel.TABLE,
                    eventModel.toContentValues());
        } catch (Exception e) {
            Timber.e(e);
        }

        if (row < 0) {
            String message = String.format(Locale.US, "Failed to insert new event " +
                            "instance for organisationUnit=[%s] and programStage=[%s]",
                    orgUnitUid, programStage);
            return Observable.error(new SQLiteConstraintException(message));
        } else {
            if (enrollmentUid != null)
                updateEnrollment(enrollmentUid);
            if (trackedEntityInstanceUid != null)
                updateTei(trackedEntityInstanceUid);

            return Observable.just(uid);
        }
    }

    @Override
    public Observable<String> scheduleEvent(String enrollmentUid, @Nullable String trackedEntityInstanceUid,
                                            @NonNull Context context, @NonNull String program, @NonNull String programStage,
                                            @NonNull Date dueDate, @NonNull String orgUnitUid, @Nullable String categoryOptionsUid,
                                            @Nullable String categoryOptionComboUid, @NonNull String latitude, @NonNull String longitude) {
        Date createDate = Calendar.getInstance().getTime();
        Calendar cal = Calendar.getInstance();
        cal.setTime(dueDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        String uid = codeGenerator.generate();

        EventModel eventModel = EventModel.builder()
                .uid(uid)
                .enrollment(enrollmentUid)
                .created(createDate)
                .lastUpdated(createDate)
                .status(EventStatus.SCHEDULE)
                .latitude(latitude)
                .longitude(longitude)
                .program(program)
                .programStage(programStage)
                .organisationUnit(orgUnitUid)
                .completedDate(null)
                .dueDate(cal.getTime())
                .state(State.TO_POST)
                .attributeOptionCombo(categoryOptionComboUid)
                .build();

        long row = -1;

        try {
            row = briteDatabase.insert(EventModel.TABLE,
                    eventModel.toContentValues());
        } catch (Exception e) {
            Timber.e(e);
        }

        if (row < 0) {
            String message = String.format(Locale.US, "Failed to insert new event " +
                            "instance for organisationUnit=[%s] and programStage=[%s]",
                    orgUnitUid, programStage);
            return Observable.error(new SQLiteConstraintException(message));
        } else {
            if (enrollmentUid != null)
                updateEnrollment(enrollmentUid);
            String tei = d2.enrollmentModule().enrollments.uid(enrollmentUid).get().trackedEntityInstance();
            if (!isEmpty(tei))
                updateTei(tei);

            return Observable.just(uid);
        }
    }

    @Override
    public Observable<String> updateTrackedEntityInstance(String eventId, String trackedEntityInstanceUid, String orgUnitUid) {
        return Observable.just(d2.trackedEntityModule().trackedEntityInstances.uid(trackedEntityInstanceUid).get())
                .map(trackedEntityInstanceModel -> {
                    ContentValues contentValues = trackedEntityInstanceModel.toContentValues();
                    contentValues.put(TrackedEntityInstanceModel.Columns.ORGANISATION_UNIT, orgUnitUid);
                    long row = -1;
                    try {
                        row = briteDatabase.update(TrackedEntityInstanceModel.TABLE, contentValues, "TrackedEntityInstance.uid = ?", trackedEntityInstanceUid == null ? "" : trackedEntityInstanceUid);
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                    if (row != -1) {
                        return eventId; //Event created and referral complete
                    }
                    return eventId;
                });
    }


    @NonNull
    @Override
    public Observable<ProgramStageModel> programStage(String programUid) {
        String id = programUid == null ? "" : programUid;
        String SELECT_PROGRAM_STAGE = "SELECT * FROM " + ProgramStageModel.TABLE + " WHERE " + ProgramStageModel.Columns.PROGRAM + " = '" + id + "' LIMIT 1";
        return briteDatabase.createQuery(ProgramStageModel.TABLE, SELECT_PROGRAM_STAGE)
                .mapToOne(ProgramStageModel::create);
    }

    @NonNull
    @Override
    public Observable<ProgramStageModel> programStageWithId(String programStageUid) {
        String id = programStageUid == null ? "" : programStageUid;
        String SELECT_PROGRAM_STAGE_WITH_ID = "SELECT * FROM " + ProgramStageModel.TABLE + " WHERE " + ProgramStageModel.Columns.UID + " = '" + id + "' LIMIT 1";
        return briteDatabase.createQuery(ProgramStageModel.TABLE, SELECT_PROGRAM_STAGE_WITH_ID)
                .mapToOne(ProgramStageModel::create);
    }


    @NonNull
    @Override
    public Observable<Event> editEvent(String trackedEntityInstance,
                                       String eventUid,
                                       String date,
                                       String orgUnitUid,
                                       String catComboUid,
                                       String catOptionCombo,
                                       String latitude, String longitude) {

        Event event = d2.eventModule().events.uid(eventUid).get();

        boolean hasChanged = false;

        Date currentDate = Calendar.getInstance().getTime();
        Date mDate = null;
        try {
            mDate = DateUtils.databaseDateFormat().parse(date);
        } catch (ParseException e) {
            Timber.e(e);
        }

        if (event.eventDate() != mDate)
            hasChanged = true;
        if (!event.organisationUnit().equals(orgUnitUid))
            hasChanged = true;
        if ((event.coordinate() == null && (!isEmpty(latitude) && !isEmpty(longitude))) ||
                (event.coordinate() != null && (!String.valueOf(event.coordinate().latitude()).equals(latitude) || !String.valueOf(event.coordinate().longitude()).equals(longitude))))
            hasChanged = true;
        if (event.attributeOptionCombo() != null && !event.attributeOptionCombo().equals(catOptionCombo))
            hasChanged = true;

        if (hasChanged) {

            Calendar cal = Calendar.getInstance();
            cal.setTime(mDate);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            ContentValues contentValues = new ContentValues();
            contentValues.put(EventModel.Columns.EVENT_DATE, DateUtils.databaseDateFormat().format(cal.getTime()));
            contentValues.put(EventModel.Columns.ORGANISATION_UNIT, orgUnitUid);
            contentValues.put(EventModel.Columns.LATITUDE, latitude);
            contentValues.put(EventModel.Columns.LONGITUDE, longitude);
            contentValues.put(EventModel.Columns.ATTRIBUTE_OPTION_COMBO, catOptionCombo);
            contentValues.put(EventModel.Columns.LAST_UPDATED, BaseIdentifiableObject.DATE_FORMAT.format(currentDate));
            contentValues.put(EventModel.Columns.STATE, event.state() == State.TO_POST ? State.TO_POST.name() : State.TO_UPDATE.name());

            long row = -1;

            try {
                row = briteDatabase.update(EventModel.TABLE, contentValues, EventModel.Columns.UID + " = ?", eventUid);
            } catch (Exception e) {
                Timber.e(e);
            }

            if (row <= 0) {
                String message = String.format(Locale.US, "Failed to update event for uid=[%s]", eventUid);
                return Observable.error(new SQLiteConstraintException(message));
            }
            if (event.enrollment() != null)
                updateEnrollment(event.enrollment());
            if (trackedEntityInstance != null)
                updateTei(trackedEntityInstance);
        }
        return event(eventUid).map(eventModel1 -> eventModel1);
    }

    @Override
    public Observable<Boolean> accessDataWrite(String programId) {
        return Observable.fromCallable(() ->
                d2.programModule().programStages.byProgramUid().eq(programId).one().get().access().data().write()
                        && d2.programModule().programs.uid(programId).get().access().data().write());
    }

    @Override
    public void deleteEvent(String eventId, String trackedEntityInstance) {
        Event event = d2.eventModule().events.uid(eventId).get();
        if (event != null) {
            if (event.state() == State.TO_POST) {
                String DELETE_WHERE = String.format(
                        "%s.%s = ?",
                        EventModel.TABLE, EventModel.Columns.UID
                );
                briteDatabase.delete(EventModel.TABLE, DELETE_WHERE, eventId);
            } else {
                ContentValues contentValues = event.toContentValues();
                contentValues.put(EventModel.Columns.STATE, State.TO_DELETE.name());
                briteDatabase.update(EventModel.TABLE, contentValues, EventModel.Columns.UID + " = ?", eventId);
            }

            if (!isEmpty(event.enrollment()))
                updateEnrollment(event.enrollment());

            if (trackedEntityInstance != null)
                updateTei(trackedEntityInstance);
        }
    }

    @Override
    public boolean isEnrollmentOpen() {
        Event event = d2.eventModule().events.uid(eventUid).withAllChildren().get();
        return event == null || event.enrollment() == null || d2.enrollmentModule().enrollments.uid(event.enrollment()).get().status() == EnrollmentStatus.ACTIVE;
    }


    private void updateEnrollment(String enrollmentUid) {
        Enrollment enrollment = d2.enrollmentModule().enrollments.uid(enrollmentUid).get();
        ContentValues cv = enrollment.toContentValues();
        cv.put(EnrollmentModel.Columns.LAST_UPDATED, DateUtils.databaseDateFormat().format(Calendar.getInstance().getTime()));
        cv.put(EnrollmentModel.Columns.STATE, enrollment.state() == State.TO_POST ? State.TO_POST.name() : State.TO_UPDATE.name());
        int enrollmentUpdated = briteDatabase.update(EnrollmentModel.TABLE, cv, "uid = ?", enrollmentUid);
        Timber.d("ENROLLMENT %s UPDATED (%s)", enrollmentUid, enrollmentUpdated);
    }

    private void updateTei(String teiUid) {
        TrackedEntityInstance tei = d2.trackedEntityModule().trackedEntityInstances.uid(teiUid).get();
        ContentValues cv = tei.toContentValues();
        cv.put(TrackedEntityInstanceModel.Columns.LAST_UPDATED, DateUtils.databaseDateFormat().format(Calendar.getInstance().getTime()));
        cv.put(TrackedEntityInstanceModel.Columns.STATE, tei.state() == State.TO_POST ? State.TO_POST.name() : State.TO_UPDATE.name());
        int teiUpdated = briteDatabase.update(TrackedEntityInstanceModel.TABLE, cv, "uid = ?", teiUid);
        Timber.d("TEI %s UPDATED (%s)", teiUid, teiUpdated);

    }

    @Override
    public Observable<OrganisationUnit> getOrganisationUnit(String orgUnitUid) {
        return d2.organisationUnitModule().organisationUnits.byUid().eq(orgUnitUid).one().getAsync().toObservable();
    }
}