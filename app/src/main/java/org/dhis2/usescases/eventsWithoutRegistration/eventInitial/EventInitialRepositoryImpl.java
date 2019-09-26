package org.dhis2.usescases.eventsWithoutRegistration.eventInitial;

import android.content.Context;
import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.FeatureType;
import org.hisp.dhis.android.core.common.Geometry;
import org.hisp.dhis.android.core.common.ObjectStyle;
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
import java.util.Collections;
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
        return d2.eventModule().events.uid(eventId).get().toObservable();
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
    @Override
    public Observable<List<OrganisationUnit>> orgUnits(String programId) {
        return d2.organisationUnitModule().organisationUnits.byProgramUids(Collections.singletonList(programId)).withPrograms().get().toObservable();
    }

    public Observable<List<OrganisationUnit>> orgUnits(String programId, String parentUid) {
        return d2.organisationUnitModule().organisationUnits
                .byParentUid().eq(parentUid)
                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                .withPrograms().get()
                .map(organisationUnits -> {
                    List<OrganisationUnit> programOrganisationUnits = new ArrayList<>();
                    for (OrganisationUnit organisationUnit : organisationUnits) {
                        if (UidsHelper.getUids(organisationUnit.programs()).contains(programId))
                            programOrganisationUnits.add(organisationUnit);
                    }
                    return programOrganisationUnits.isEmpty() ? organisationUnits : programOrganisationUnits;
                }).toObservable();
    }

    @NonNull
    @Override
    public Observable<CategoryCombo> catCombo(String programUid) {
        return Observable.defer(() -> Observable.just(d2.categoryModule().categoryCombos.withCategories().withCategoryOptionCombos().uid(d2.programModule().programs.withCategoryCombo().uid(programUid).blockingGet().categoryCombo().uid()).blockingGet()))
                .map(categoryCombo -> {
                    List<Category> fullCategories = new ArrayList<>();
                    List<CategoryOptionCombo> fullOptionCombos = new ArrayList<>();
                    for (Category category : categoryCombo.categories()) {
                        fullCategories.add(d2.categoryModule().categories.withCategoryOptions().uid(category.uid()).blockingGet());
                    }
                    for (CategoryOptionCombo categoryOptionCombo : categoryCombo.categoryOptionCombos())
                        fullOptionCombos.add(d2.categoryModule().categoryOptionCombos.withCategoryOptions().uid(categoryOptionCombo.uid()).blockingGet());
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
                                List<CategoryOption> selectedCatOptions = d2.categoryModule().categoryOptionCombos.withCategoryOptions().uid(event.attributeOptionCombo()).blockingGet().categoryOptions();
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

    @Override
    public Observable<String> createEvent(String enrollmentUid, @Nullable String trackedEntityInstanceUid,
                                          @NonNull Context context, @NonNull String programUid,
                                          @NonNull String programStage, @NonNull Date date,
                                          @NonNull String orgUnitUid, @Nullable String categoryOptionsUid,
                                          @Nullable String categoryOptionComboUid, @NonNull Geometry geometry) {

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
            if (d2.programModule().programStages.uid(eventRepository.blockingGet().programStage()).blockingGet().featureType() != null)
                switch (d2.programModule().programStages.uid(eventRepository.blockingGet().programStage()).blockingGet().featureType()) {
                    case NONE:
                        break;
                    case POINT:
                    case POLYGON:
                    case MULTI_POLYGON:
                        eventRepository.setGeometry(geometry);
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
                                            @Nullable String categoryOptionComboUid, @NonNull Geometry geometry) {
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
            if (d2.programModule().programStages.uid(eventRepository.blockingGet().programStage()).blockingGet().featureType() != null)
                switch (d2.programModule().programStages.uid(eventRepository.blockingGet().programStage()).blockingGet().featureType()) {
                    case NONE:
                        break;
                    case POINT:
                    case POLYGON:
                    case MULTI_POLYGON:
                        eventRepository.setGeometry(geometry);
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
                                       Geometry geometry) {

        return Observable.fromCallable(() -> d2.eventModule().events.uid(eventUid))
                .map(eventRepository -> {
                    eventRepository.setEventDate(DateUtils.databaseDateFormat().parse(date));
                    eventRepository.setOrganisationUnitUid(orgUnitUid);
                    eventRepository.setAttributeOptionComboUid(catOptionCombo);
                    FeatureType featureType = d2.programModule().programStages.uid(eventRepository.blockingGet().programStage()).blockingGet().featureType();
                    if (featureType != null)
                        switch (featureType) {
                            case NONE:
                                break;
                            case POINT:
                            case POLYGON:
                            case MULTI_POLYGON:
                                eventRepository.setGeometry(geometry);
                                break;
                            default:
                                break;
                        }
                    return eventRepository.blockingGet();
                });
    }

    @Override
    public Observable<Boolean> accessDataWrite(String programUid) {
        if (eventUid != null)
            return d2.eventModule().events.uid(eventUid).get().toObservable()
                    .flatMap(event -> {
                        if (event.attributeOptionCombo() != null)
                            return accessWithCatOption(programUid, event.attributeOptionCombo());
                        else
                            return programAccess(programUid);
                    });
        else
            return programAccess(programUid);


    }

    private Observable<Boolean> accessWithCatOption(String programUid, String catOptionCombo) {
        return d2.categoryModule().categoryOptionCombos.withCategoryOptions().uid(catOptionCombo).get()
                .map(data -> UidsHelper.getUidsList(data.categoryOptions()))
                .flatMap(categoryOptionsUids -> d2.categoryModule().categoryOptions.byUid().in(categoryOptionsUids).get())
                .toObservable()
                .map(categoryOptions -> {
                    boolean access = true;
                    for (CategoryOption option : categoryOptions) {
                        if (!option.access().data().write())
                            access = false;
                    }
                    return access;
                }).flatMap(catComboAccess -> {
                    if (catComboAccess)
                        return programAccess(programUid);
                    else
                        return Observable.just(catComboAccess);
                });
    }

    private Observable<Boolean> programAccess(String programUid) {
        return Observable.fromCallable(() ->
                d2.programModule().programStages.byProgramUid().eq(programUid).one().blockingGet().access().data().write() &&
                        d2.programModule().programs.uid(programUid).blockingGet().access().data().write()

        );
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
        Event event = d2.eventModule().events.uid(eventUid).blockingGet();
        return event == null || event.enrollment() == null || d2.enrollmentModule().enrollments.uid(event.enrollment()).blockingGet().status() == EnrollmentStatus.ACTIVE;
    }


    @Override
    public Observable<Program> getProgramWithId(String programUid) {
        return d2.programModule().programs.withCategoryCombo().withProgramIndicators().withProgramRules().withProgramRuleVariables().withProgramSections().withProgramStages()
            .withProgramTrackedEntityAttributes().withRelatedProgram().withStyle().withTrackedEntityType().byUid().eq(programUid).one().get().toObservable();
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