package org.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.Coordinates;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.Geometry;
import org.hisp.dhis.android.core.common.ObjectStyle;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventCreateProjection;
import org.hisp.dhis.android.core.event.EventObjectRepository;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStage;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import timber.log.Timber;

/**
 * QUADRAM. Created by Cristian on 22/03/2018.
 */

public class EventInitialRepositoryImpl implements EventInitialRepository {

    private final String eventUid;
    private final D2 d2;

    EventInitialRepositoryImpl(String eventUid, D2 d2) {
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
                    for (OrganisationUnit organisationUnit : organisationUnits) {
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
        } else if (scheduleDate != null) {
            return scheduleDate;
        } else {
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
                            if (organisationUnit.openingDate() != null && organisationUnit.openingDate().after(DateUtils.uiDateFormat().parse(date))
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
                    for (OrganisationUnit organisationUnit : organisationUnits) {
                        for (Program program : organisationUnit.programs()) {
                            if (program.uid().equals(programId))
                                programOrganisationUnits.add(organisationUnit);
                        }
                    }
                    return programOrganisationUnits;
                }).map(organisationUnits -> {
                    if (date != null) {
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

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return Observable.fromCallable(() ->
                d2.eventModule().events.blockingAdd(
                        EventCreateProjection.builder()
                                .enrollment(enrollmentUid)
                                .program(programUid)
                                .programStage(programStage)
                                .organisationUnit(orgUnitUid)
                                .attributeOptionCombo(categoryOptionComboUid)
                                .build()
                )
        ).map(uid -> {
            EventObjectRepository eventRepository = d2.eventModule().events.uid(uid);
            eventRepository.setEventDate(cal.getTime());
            switch (d2.programModule().programs.byUid().eq(programUid).one().blockingGet().featureType()){
                case NONE:
                    break;
                case POINT:
                    eventRepository.setGeometry(Geometry.builder()
                            .type(FeatureType.POINT)
                            .coordinates(Coordinates.create(Double.valueOf(latitude), Double.valueOf(longitude)).toString())
                            .build());
                    break;
                case POLYGON:
                case MULTI_POLYGON:
                    //TODO: IMPLEMENT CASES
                    break;
                default:
                    break;
            }
            return uid;
        });
    }

    @Override
    public Observable<String> scheduleEvent(String enrollmentUid, @Nullable String trackedEntityInstanceUid,
                                            @NonNull Context context, @NonNull String programUid, @NonNull String programStage,
                                            @NonNull Date dueDate, @NonNull String orgUnitUid, @Nullable String categoryOptionsUid,
                                            @Nullable String categoryOptionComboUid, @NonNull String latitude, @NonNull String longitude) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dueDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return Observable.fromCallable(() ->
                d2.eventModule().events.blockingAdd(
                        EventCreateProjection.builder()
                                .enrollment(enrollmentUid)
                                .program(programUid)
                                .programStage(programStage)
                                .organisationUnit(orgUnitUid)
                                .attributeOptionCombo(categoryOptionComboUid)
                                .build()
                )
        ).map(uid -> {
            EventObjectRepository eventRepository = d2.eventModule().events.uid(uid);
            eventRepository.setDueDate(cal.getTime());
            eventRepository.setStatus(EventStatus.SCHEDULE);
            switch (d2.programModule().programs.byUid().eq(programUid).one().blockingGet().featureType()) {
                case NONE:
                    break;
                case POINT:
                    eventRepository.setGeometry(Geometry.builder()
                            .type(FeatureType.POINT)
                            .coordinates(Coordinates.create(Double.valueOf(latitude), Double.valueOf(longitude)).toString())
                            .build());
                    break;
                case POLYGON:
                case MULTI_POLYGON:
                    //TODO: IMPLEMENT CASES
                    break;
                default:
                    break;
            }
            return uid;
        });
    }

    @NonNull
    @Override
    public Observable<ProgramStage> programStage(String programUid) {
        return d2.programModule().programStages.byProgramUid().eq(programUid).one().get().toObservable();
    }

    @NonNull
    @Override
    public Observable<ProgramStage> programStageWithId(String programStageUid) {
        return d2.programModule().programStages.byUid().eq(programStageUid).one().get().toObservable();
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

        return Observable.fromCallable(() -> d2.eventModule().events.uid(eventUid))
                .map(eventRepository -> {
                    eventRepository.setEventDate(DateUtils.databaseDateFormat().parse(date));
                    eventRepository.setOrganisationUnitUid(orgUnitUid);
                    eventRepository.setAttributeOptionComboUid(catOptionCombo);
                    eventRepository.setGeometry(Geometry.builder() //TODO: CHANGE TO SUPPORT ALL FEATURE TYPES
                            .type(FeatureType.POINT)
                            .coordinates(Coordinates.create(Double.valueOf(latitude), Double.valueOf(longitude)).toString())
                            .build());
                    return eventRepository.blockingGet();
                });
    }

    @Override
    public Observable<Boolean> accessDataWrite(String programId) {
        return Observable.fromCallable(() ->
                d2.programModule().programStages.byProgramUid().eq(programId).one().blockingGet().access().data().write()
                        && d2.programModule().programs.uid(programId).blockingGet().access().data().write());
    }

    @Override
    public void deleteEvent(String eventId, String trackedEntityInstance) {
        try {
            d2.eventModule().events.uid(eventId).blockingDelete();
        } catch (D2Error d2Error) {
            Timber.e(d2Error);
        }
    }

    @Override
    public boolean isEnrollmentOpen() {
        Event event = d2.eventModule().events.uid(eventUid).withAllChildren().blockingGet();
        return event == null || event.enrollment() == null || d2.enrollmentModule().enrollments.uid(event.enrollment()).blockingGet().status() == EnrollmentStatus.ACTIVE;
    }


    @Override
    public Observable<Program> getProgramWithId(String programUid) {
        return d2.programModule().programs.withAllChildren().byUid().eq(programUid).one().get().toObservable();
    }

    @Override
    public Flowable<ProgramStage> programStageForEvent(String eventId) {
        return d2.eventModule().events.byUid().eq(eventId).one().get().toFlowable()
                .map(event -> d2.programModule().programStages.byUid().eq(event.programStage()).one().blockingGet());
    }

    @Override
    public Observable<OrganisationUnit> getOrganisationUnit(String orgUnitUid) {
        return d2.organisationUnitModule().organisationUnits.byUid().eq(orgUnitUid).one().get().toObservable();
    }

    @Override
    public Observable<ObjectStyle> getObjectStyle(String uid) {
        return d2.programModule().programStages.byUid().eq(uid).withStyle().one().get().toObservable()
                .map(programStage -> (programStage.style() != null) ? programStage.style() : ObjectStyle.builder().build());
    }
}