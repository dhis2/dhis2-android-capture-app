package org.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.utils.CodeGenerator;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentModel;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;
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
        return Observable.fromCallable(() -> d2.eventModule().events.uid(eventId).blockingGet()).filter(event -> event.state() != State.TO_DELETE);
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnit>> orgUnits(String programId) {
        return Observable.fromCallable(() -> d2.organisationUnitModule().organisationUnits.byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE).withPrograms().blockingGet())
                .map(organisationUnits -> {
                    List<OrganisationUnit> programOrganisationUnits = new ArrayList<>();
                    for(OrganisationUnit organisationUnit : organisationUnits){
                        for (Program program : organisationUnit.programs()) {
                            if (program.uid().equals(programId))
                                programOrganisationUnits.add(organisationUnit);
                        }
                    }
                    return programOrganisationUnits;
                });
    }

    @NonNull
    @Override
    public Observable<CategoryCombo> catCombo(String programUid) {
        return Observable.defer(() -> Observable.just(d2.categoryModule().categoryCombos.uid(d2.programModule().programs.uid(programUid).blockingGet().categoryCombo().uid()).withAllChildren().blockingGet()))
                .map(categoryCombo -> {
                    List<Category> fullCategories = new ArrayList<>();
                    List<CategoryOptionCombo> fullOptionCombos = new ArrayList<>();
                    for (Category category : categoryCombo.categories()) {
                        fullCategories.add(d2.categoryModule().categories.uid(category.uid()).withAllChildren().blockingGet());
                    }
                    for (CategoryOptionCombo categoryOptionCombo : categoryCombo.categoryOptionCombos())
                        fullOptionCombos.add(d2.categoryModule().categoryOptionCombos.uid(categoryOptionCombo.uid()).withAllChildren().blockingGet());
                    return categoryCombo.toBuilder().categories(fullCategories).categoryOptionCombos(fullOptionCombos).build();
                });
    }

    @Override
    public Flowable<Map<String, CategoryOption>> getOptionsFromCatOptionCombo(String eventId) {
        return Flowable.just(d2.eventModule().events.uid(eventUid).blockingGet())
                .flatMap(event -> catCombo(event.program()).toFlowable(BackpressureStrategy.LATEST)
                        .flatMap(categoryCombo -> {
                            Map<String, CategoryOption> map = new HashMap<>();
                            if (!categoryCombo.isDefault() && event.attributeOptionCombo() != null) {
                                List<CategoryOption> selectedCatOptions = d2.categoryModule().categoryOptionCombos.uid(event.attributeOptionCombo()).withAllChildren().blockingGet().categoryOptions();
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
                .orderByEventDate(RepositoryScope.OrderByDirection.DESC).blockingGet();
        List<Event> scheduleEvents = d2.eventModule().events.byEnrollmentUid().eq(enrollmentUid).byProgramStageUid().eq(programStageUid)
                .orderByDueDate(RepositoryScope.OrderByDirection.DESC).blockingGet();

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
        } else if (scheduleDate != null){
            return scheduleDate;
        }else{
            return Calendar.getInstance().getTime();
        }
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnit>> filteredOrgUnits(String date, String programId) {
        if (date == null)
            return orgUnits(programId);
        else
            return orgUnits(programId)
                    .map(organisationUnits -> {
                        Iterator<OrganisationUnit> iterator = organisationUnits.iterator();
                        while (iterator.hasNext()) {
                            OrganisationUnit organisationUnit = iterator.next();
                            if(organisationUnit.openingDate() != null && organisationUnit.openingDate().after(DateUtils.uiDateFormat().parse(date))
                                    || organisationUnit.closedDate() != null && organisationUnit.closedDate().before(DateUtils.uiDateFormat().parse(date)))
                                iterator.remove();
                        }
                        return organisationUnits;
                    });
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnit>> searchOrgUnits(String date, String programId) {
        return Observable.fromCallable(() -> d2.organisationUnitModule().organisationUnits.withPrograms().blockingGet())
                    .map(organisationUnits -> {
                        List<OrganisationUnit> programOrganisationUnits = new ArrayList<>();
                        for(OrganisationUnit organisationUnit : organisationUnits){
                            for (Program program : organisationUnit.programs()) {
                                if (program.uid().equals(programId))
                                    programOrganisationUnits.add(organisationUnit);
                            }
                        }
                        return programOrganisationUnits;
                    }).map(organisationUnits -> {
                        if(date!=null) {
                            Iterator<OrganisationUnit> iterator = organisationUnits.iterator();
                            while (iterator.hasNext()) {
                                OrganisationUnit organisationUnit = iterator.next();
                                if (organisationUnit.openingDate() != null && organisationUnit.openingDate().after(DateUtils.uiDateFormat().parse(date))
                                        || organisationUnit.closedDate() != null && organisationUnit.closedDate().before(DateUtils.uiDateFormat().parse(date)))
                                    iterator.remove();
                            }
                        }
                    return organisationUnits;
                })
                ;
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
            String tei = d2.enrollmentModule().enrollments.uid(enrollmentUid).blockingGet().trackedEntityInstance();
            if (!isEmpty(tei))
                updateTei(tei);

            return Observable.just(uid);
        }
    }

    @Override
    public Observable<String> updateTrackedEntityInstance(String eventId, String trackedEntityInstanceUid, String orgUnitUid) {
        return  Observable.just(d2.trackedEntityModule().trackedEntityInstances.uid(trackedEntityInstanceUid).blockingGet())
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
    public Observable<ProgramStage> programStage(String programUid) {
        return d2.programModule().programStages.byProgramUid().eq(programUid).one().getAsync().toObservable();
    }

    @NonNull
    @Override
    public Observable<ProgramStage> programStageWithId(String programStageUid) {
        return d2.programModule().programStages.byUid().eq(programStageUid).one().getAsync().toObservable();
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

        Event event = d2.eventModule().events.uid(eventUid).blockingGet();

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
                d2.programModule().programStages.byProgramUid().eq(programId).one().blockingGet().access().data().write()
                && d2.programModule().programs.uid(programId).blockingGet().access().data().write());
    }

    @Override
    public void deleteEvent(String eventId, String trackedEntityInstance) {
        Event event = d2.eventModule().events.uid(eventId).blockingGet();
            if (event != null ) {
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
        Event event = d2.eventModule().events.uid(eventUid).withAllChildren().blockingGet();
        return event == null || event.enrollment() == null || d2.enrollmentModule().enrollments.uid(event.enrollment()).blockingGet().status() == EnrollmentStatus.ACTIVE;
    }


    private void updateEnrollment(String enrollmentUid) {
        Enrollment enrollment = d2.enrollmentModule().enrollments.uid(enrollmentUid).blockingGet();
        ContentValues cv = enrollment.toContentValues();
        cv.put(EnrollmentModel.Columns.LAST_UPDATED, DateUtils.databaseDateFormat().format(Calendar.getInstance().getTime()));
        cv.put(EnrollmentModel.Columns.STATE, enrollment.state() == State.TO_POST ? State.TO_POST.name() : State.TO_UPDATE.name());
        int enrollmentUpdated = briteDatabase.update(EnrollmentModel.TABLE, cv, "uid = ?", enrollmentUid);
        Timber.d("ENROLLMENT %s UPDATED (%s)", enrollmentUid, enrollmentUpdated);
    }

    private void updateTei(String teiUid) {
        TrackedEntityInstance tei = d2.trackedEntityModule().trackedEntityInstances.uid(teiUid).blockingGet();
        ContentValues cv = tei.toContentValues();
        cv.put(TrackedEntityInstanceModel.Columns.LAST_UPDATED, DateUtils.databaseDateFormat().format(Calendar.getInstance().getTime()));
        cv.put(TrackedEntityInstanceModel.Columns.STATE, tei.state() == State.TO_POST ? State.TO_POST.name() : State.TO_UPDATE.name());
        int teiUpdated = briteDatabase.update(TrackedEntityInstanceModel.TABLE, cv, "uid = ?", teiUid);
        Timber.d("TEI %s UPDATED (%s)", teiUid, teiUpdated);

    }

    @Override
    public Observable<Program> getProgramWithId(String programUid) {
        return d2.programModule().programs.withAllChildren().byUid().eq(programUid).one().getAsync().toObservable();
    }

    @Override
    public Flowable<ProgramStage> programStageForEvent(String eventId) {
        return d2.eventModule().events.byUid().eq(eventId).one().getAsync().toFlowable()
                .map(event -> d2.programModule().programStages.byUid().eq(event.programStage()).one().get());
    }

    @Override
    public Observable<OrganisationUnit> getOrganisationUnit(String orgUnitUid) {
        return d2.organisationUnitModule().organisationUnits.byUid().eq(orgUnitUid).one().getAsync().toObservable();
    }

    @Override
    public Observable<ObjectStyle> getObjectStyle(String uid) {
        return d2.programModule().programStages.byUid().eq(uid).withStyle().one().getAsync().toObservable()
                .map(programStage -> (programStage.style() != null) ? programStage.style() : ObjectStyle.builder().build());
    }
}