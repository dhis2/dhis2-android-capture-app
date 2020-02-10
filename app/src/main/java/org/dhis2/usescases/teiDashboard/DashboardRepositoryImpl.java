package org.dhis2.usescases.teiDashboard;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.Bindings.EventExtensionsKt;
import org.dhis2.R;
import org.dhis2.data.tuples.Pair;
import org.dhis2.data.tuples.Trio;
import org.dhis2.usescases.teiDashboard.dashboardfragments.relationships.RelationshipViewModel;
import org.dhis2.utils.AuthorityException;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.ValueUtils;
import org.dhis2.utils.resources.ResourceManager;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentCollectionRepository;
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.legendset.Legend;
import org.hisp.dhis.android.core.legendset.LegendSet;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.note.NoteCreateProjection;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramIndicator;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.android.core.relationship.RelationshipItem;
import org.hisp.dhis.android.core.relationship.RelationshipItemTrackedEntityInstance;
import org.hisp.dhis.android.core.relationship.RelationshipType;
import org.hisp.dhis.android.core.systeminfo.SystemInfo;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityTypeAttribute;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Consumer;
import timber.log.Timber;

/**
 * QUADRAM. Created by ppajuelo on 30/11/2017.
 */

public class DashboardRepositoryImpl
        implements
        DashboardRepository {

    private final D2 d2;
    private final ResourceManager resources;

    private String teiUid;

    private String programUid;


    public DashboardRepositoryImpl(D2 d2, String teiUid, String programUid, ResourceManager resources) {
        this.d2 = d2;
        this.teiUid = teiUid;
        this.programUid = programUid;
        this.resources = resources;
    }

    @Override
    public Observable<List<TrackedEntityAttributeValue>> mainTrackedEntityAttributes(String teiUid) {
        return d2.trackedEntityModule().trackedEntityAttributeValues().byTrackedEntityInstance().eq(teiUid).get()
                .toObservable();
    }

    @Override
    public Event updateState(Event eventModel, EventStatus newStatus) {

        try {
            d2.eventModule().events().uid(eventModel.uid()).setStatus(newStatus);
        } catch (D2Error d2Error) {
            Timber.e(d2Error);
        }

        return d2.eventModule().events().uid(eventModel.uid()).blockingGet();
    }

    @Override
    public Observable<List<ProgramStage>> getProgramStages(String programUid) {
        return d2.programModule().programStages().byProgramUid().eq(programUid).get().toObservable();
    }

    @Override
    public Observable<Enrollment> getEnrollment(String programUid, String teiUid) {
        String progId = programUid == null ? "" : programUid;
        String teiId = teiUid == null ? "" : teiUid;
        return Observable.fromCallable(() -> d2.enrollmentModule().enrollments().byTrackedEntityInstance().eq(teiId)
                .byProgram().eq(progId).one().blockingGet());
    }

    @Override
    public Observable<List<Event>> getTEIEnrollmentEvents(String programUid, String teiUid) {

        return d2.enrollmentModule().enrollments().byProgram().eq(programUid).byTrackedEntityInstance().eq(teiUid)
                .one().get().flatMap(enrollment -> d2.eventModule().events().byEnrollmentUid().eq(enrollment.uid())
                        .byDeleted().isFalse().get().toFlowable().flatMapIterable(events -> events).map(event -> {
                            if (Boolean.FALSE
                                    .equals(d2.programModule().programs().uid(programUid).blockingGet().ignoreOverdueEvents()))
                                if (event.status() == EventStatus.SCHEDULE
                                        && event.dueDate().before(DateUtils.getInstance().getToday()))
                                    event = updateState(event, EventStatus.OVERDUE);

                            return event;
                        }).toSortedList((event1, event2) ->
                                EventExtensionsKt.primaryDate(event2).compareTo(
                                        EventExtensionsKt.primaryDate(event1)
                                ))

                ).toObservable();
    }

    @Override
    public Observable<List<Event>> getEnrollmentEventsWithDisplay(String programUid, String teiUid) {
        return getEnrollment(programUid, teiUid)
                .flatMapSingle(enrollment -> d2.eventModule().events().byEnrollmentUid().eq(enrollment.uid()).get())
                .map(events -> {
                    List<Event> finalEvents = new ArrayList<>();
                    for (Event event : events) {
                        if (d2.programModule().programStages().uid(event.programStage()).blockingGet().displayGenerateEventBox()) {
                            finalEvents.add(event);
                        }
                    }
                    return finalEvents;
                });
    }

    @Override
    public Observable<ProgramStage> displayGenerateEvent(String eventUid) {
        return d2.eventModule().events().uid(eventUid).get().map(Event::programStage)
                .flatMap(stageUid -> d2.programModule().programStages().uid(stageUid).get()).toObservable();
    }

    @Override
    public Observable<Trio<ProgramIndicator, String, String>> getLegendColorForIndicator(ProgramIndicator indicator,
                                                                                         String value) {
        String color = "";
        if (indicator.legendSets() != null && !indicator.legendSets().isEmpty()) {
            LegendSet legendSet = indicator.legendSets().get(0);
            List<Legend> legends = d2.legendSetModule().legends().byStartValue().smallerThan(Double.valueOf(value)).byEndValue().biggerThan(Double.valueOf(value))
                    .byLegendSet().eq(legendSet.uid()).blockingGet();
            color = legends.get(0).color();
        }
        return Observable.just(Trio.create(indicator, value, color));
    }

    @Override
    public Integer getObjectStyle(String uid) {
        TrackedEntityType teType = d2.trackedEntityModule().trackedEntityTypes().uid(uid).blockingGet();
        return resources.getObjectStyleDrawableResource(
                teType.style() != null ? teType.style().icon() : null,
                R.drawable.ic_person
        );
    }

    @Override
    public Observable<List<Pair<RelationshipType, String>>> relationshipsForTeiType(String teType) {
        return d2.systemInfoModule().systemInfo().get().toObservable()
                .map(SystemInfo::version)
                .flatMap(version -> {
                    if (version.equals("2.29"))
                        return d2.relationshipModule().relationshipTypes().get().toObservable()
                                .flatMapIterable(list -> list)
                                .map(relationshipType -> Pair.create(relationshipType, teType)).toList().toObservable();
                    else
                        return d2.relationshipModule().relationshipTypes().withConstraints().get()
                                .map(relationshipTypes -> {
                                    List<Pair<RelationshipType, String>> relTypeList = new ArrayList<>();
                                    for (RelationshipType relationshipType : relationshipTypes) {
                                        if (relationshipType.fromConstraint() != null && relationshipType.fromConstraint().trackedEntityType() != null &&
                                                relationshipType.fromConstraint().trackedEntityType().uid().equals(teType)) {
                                            if (relationshipType.toConstraint() != null && relationshipType.toConstraint().trackedEntityType() != null) {
                                                relTypeList.add(Pair.create(relationshipType, relationshipType.toConstraint().trackedEntityType().uid()));
                                            }
                                        } else if (relationshipType.bidirectional() && relationshipType.toConstraint() != null && relationshipType.toConstraint().trackedEntityType() != null &&
                                                relationshipType.toConstraint().trackedEntityType().uid().equals(teType)) {
                                            if (relationshipType.fromConstraint() != null && relationshipType.fromConstraint().trackedEntityType() != null) {
                                                relTypeList.add(Pair.create(relationshipType, relationshipType.fromConstraint().trackedEntityType().uid()));
                                            }
                                        }
                                    }
                                    return relTypeList;
                                }).toObservable();
                });
    }

    @Override
    public Observable<CategoryCombo> catComboForProgram(String programUid) {
        return Observable.defer(() -> Observable.just(d2.categoryModule().categoryCombos().withCategories()
                .withCategoryOptionCombos()
                .uid(d2.programModule().programs().uid(programUid).blockingGet().categoryComboUid()).blockingGet()))
                .map(categoryCombo -> {
                    List<Category> fullCategories = new ArrayList<>();
                    List<CategoryOptionCombo> fullOptionCombos = new ArrayList<>();
                    for (Category category : categoryCombo.categories()) {
                        fullCategories.add(
                                d2.categoryModule().categories().withCategoryOptions().uid(category.uid()).blockingGet());
                    }

                    List<CategoryOptionCombo> catOptionCombos = d2.categoryModule().categoryOptionCombos()
                            .byCategoryComboUid().eq(categoryCombo.uid()).blockingGet();

                    for (CategoryOptionCombo categoryOptionCombo : catOptionCombos) {
                        fullOptionCombos.add(d2.categoryModule().categoryOptionCombos().withCategoryOptions()
                                .uid(categoryOptionCombo.uid()).blockingGet());
                    }
                    return categoryCombo.toBuilder().categories(fullCategories).categoryOptionCombos(fullOptionCombos)
                            .build();
                });
    }

    @Override
    public Observable<List<CategoryOptionCombo>> catOptionCombos(String catComboUid) {
        return d2.categoryModule().categoryOptionCombos().byCategoryComboUid().eq(catComboUid).get().toObservable();
    }

    @Override
    public void setDefaultCatOptCombToEvent(String eventUid) {
        CategoryCombo defaultCatCombo = d2.categoryModule().categoryCombos().byIsDefault().isTrue().one().blockingGet();
        CategoryOptionCombo defaultCatOptComb = d2.categoryModule().categoryOptionCombos().byCategoryComboUid()
                .eq(defaultCatCombo.uid()).one().blockingGet();
        try {
            d2.eventModule().events().uid(eventUid).setAttributeOptionComboUid(defaultCatOptComb.uid());
        } catch (D2Error d2Error) {
            Timber.e(d2Error);
        }
    }

    @Override
    public Observable<List<TrackedEntityAttributeValue>> getTEIAttributeValues(String programUid, String teiUid) {
        if (programUid != null) {
            return d2.programModule().programTrackedEntityAttributes()
                    .byProgram().eq(programUid)
                    .orderBySortOrder(RepositoryScope.OrderByDirection.ASC).get()
                    .map(programTrackedEntityAttributes -> {
                        List<TrackedEntityAttributeValue> attributeValues = new ArrayList<>();
                        for (ProgramTrackedEntityAttribute programAttribute : programTrackedEntityAttributes) {
                            if (d2.trackedEntityModule().trackedEntityAttributeValues().value(programAttribute.trackedEntityAttribute().uid(), teiUid).blockingExists()) {
                                TrackedEntityAttributeValue attributeValue = d2.trackedEntityModule().trackedEntityAttributeValues().value(programAttribute.trackedEntityAttribute().uid(), teiUid).blockingGet();
                                TrackedEntityAttribute attribute = d2.trackedEntityModule().trackedEntityAttributes().uid(programAttribute.trackedEntityAttribute().uid()).blockingGet();
                                if (attribute.valueType() != ValueType.IMAGE) {
                                    attributeValues.add(
                                            ValueUtils.transform(d2, attributeValue, attribute.valueType(), attribute.optionSet() != null ? attribute.optionSet().uid() : null)
                                    );
                                }
                            }
                        }
                        return attributeValues;
                    }).toObservable();

        } else {
            return d2.trackedEntityModule().trackedEntityAttributeValues().byTrackedEntityInstance().eq(teiUid).get()
                    .map(attributeValueList -> {
                        List<TrackedEntityAttributeValue> attributeValues = new ArrayList<>();
                        for (TrackedEntityAttributeValue attributeValue : attributeValueList) {
                            TrackedEntityAttribute attribute = d2.trackedEntityModule().trackedEntityAttributes().uid(attributeValue.trackedEntityAttribute()).blockingGet();
                            if (attribute.valueType() != ValueType.IMAGE) {
                                attributeValues.add(
                                        ValueUtils.transform(d2, attributeValue, attribute.valueType(), attribute.optionSet() != null ? attribute.optionSet().uid() : null)
                                );
                            }
                        }
                        return attributeValues;
                    }).toObservable();
        }
    }

    @Override
    public Flowable<List<ProgramIndicator>> getIndicators(String programUid) {
        return d2.programModule().programIndicators().byProgramUid().eq(programUid).withLegendSets().get()
                .toFlowable();
    }

    @Override
    public boolean setFollowUp(String enrollmentUid) {

        boolean followUp = Boolean.TRUE
                .equals(d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet().followUp());
        try {
            d2.enrollmentModule().enrollments().uid(enrollmentUid).setFollowUp(!followUp);
            return !followUp;
        } catch (D2Error d2Error) {
            Timber.e(d2Error);
            return followUp;
        }
    }

    @Override
    public Consumer<Pair<String, Boolean>> handleNote() {
        return stringBooleanPair -> {
            if (stringBooleanPair.val1()) {

                d2.noteModule().notes().blockingAdd(
                        NoteCreateProjection.builder()
                                .enrollment(d2.enrollmentModule().enrollments().byProgram().eq(programUid)
                                        .byTrackedEntityInstance().eq(teiUid)
                                        .byStatus().eq(EnrollmentStatus.ACTIVE).one().blockingGet().uid())
                                .value(stringBooleanPair.val0())
                                .build()
                );
            }
        };
    }

    @Override
    public Flowable<Enrollment> completeEnrollment(@NonNull String enrollmentUid) {
        return Flowable.fromCallable(() -> {
            d2.enrollmentModule().enrollments().uid(enrollmentUid)
                    .setStatus(EnrollmentStatus.COMPLETED);
            return d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet();
        });
    }

    @Override
    public Observable<TrackedEntityInstance> getTrackedEntityInstance(String teiUid) {
        return Observable.fromCallable(
                () -> d2.trackedEntityModule().trackedEntityInstances().byUid().eq(teiUid).one().blockingGet());
    }

    @Override
    public Observable<List<ProgramTrackedEntityAttribute>> getProgramTrackedEntityAttributes(String programUid) {
        if (programUid != null) {
            return d2.programModule().programTrackedEntityAttributes().byProgram().eq(programUid)
                    .orderBySortOrder(RepositoryScope.OrderByDirection.ASC).get().toObservable();
        } else {
            return Observable.fromCallable(() -> d2.trackedEntityModule().trackedEntityAttributes()
                    .byDisplayInListNoProgram().eq(true).blockingGet()).map(trackedEntityAttributes -> {
                List<Program> programs = d2.programModule().programs().blockingGet();

                List<String> teaUids = UidsHelper.getUidsList(trackedEntityAttributes);
                List<ProgramTrackedEntityAttribute> programTrackedEntityAttributes = new ArrayList<>();

                for (Program program : programs) {
                    List<ProgramTrackedEntityAttribute> attributeList = d2.programModule()
                            .programTrackedEntityAttributes().byProgram().eq(program.uid())
                            .orderBySortOrder(RepositoryScope.OrderByDirection.ASC).blockingGet();

                    for (ProgramTrackedEntityAttribute pteattr : attributeList) {
                        if (teaUids.contains(pteattr.uid()))
                            programTrackedEntityAttributes.add(pteattr);
                    }
                }
                return programTrackedEntityAttributes;
            });
        }
    }

    @Override
    public Observable<List<OrganisationUnit>> getTeiOrgUnits(@NonNull String teiUid, @Nullable String programUid) {
        EnrollmentCollectionRepository enrollmentRepo = d2.enrollmentModule().enrollments().byTrackedEntityInstance()
                .eq(teiUid);
        if (programUid != null) {
            enrollmentRepo = enrollmentRepo.byProgram().eq(programUid);
        }

        return enrollmentRepo.get().toObservable().map(enrollments -> {
            List<String> orgUnitIds = new ArrayList<>();
            for (Enrollment enrollment : enrollments) {
                orgUnitIds.add(enrollment.organisationUnit());
            }
            return d2.organisationUnitModule().organisationUnits().byUid().in(orgUnitIds).blockingGet();
        });
    }

    @Override
    public Observable<List<Program>> getTeiActivePrograms(String teiUid, boolean showOnlyActive) {
        EnrollmentCollectionRepository enrollmentRepo = d2.enrollmentModule().enrollments().byTrackedEntityInstance()
                .eq(teiUid);
        if (showOnlyActive)
            enrollmentRepo.byStatus().eq(EnrollmentStatus.ACTIVE);
        return enrollmentRepo.get().toObservable().flatMapIterable(enrollments -> enrollments)
                .map(Enrollment::program).toList().toObservable()
                .map(programUids -> d2.programModule().programs().byUid().in(programUids).blockingGet());
    }

    @Override
    public Observable<List<Enrollment>> getTEIEnrollments(String teiUid) {
        return d2.enrollmentModule().enrollments().byTrackedEntityInstance().eq(teiUid).get().toObservable();
    }

    @Override
    public void saveCatOption(String eventUid, String catOptionComboUid) {
        try {
            d2.eventModule().events().uid(eventUid).setAttributeOptionComboUid(catOptionComboUid);
        } catch (D2Error d2Error) {
            Timber.e(d2Error);
        }
    }

    @Override
    public Single<Boolean> deleteTeiIfPossible() {
        return Single.fromCallable(() -> {
            boolean local = d2.trackedEntityModule()
                    .trackedEntityInstances()
                    .uid(teiUid)
                    .blockingGet()
                    .state() == State.TO_POST;
            boolean hasAuthority = d2.userModule()
                    .authorities()
                    .byName().eq("F_TEI_CASCADE_DELETE")
                    .one().blockingExists();
            return local || hasAuthority;
        }).flatMap(canDelete -> {
            if (canDelete) {
                return d2.trackedEntityModule()
                        .trackedEntityInstances()
                        .uid(teiUid)
                        .delete()
                        .andThen(Single.fromCallable(() -> true));
            } else {
                return Single.fromCallable(() -> false);
            }
        });
    }

    @Override
    public Single<Boolean> deleteEnrollmentIfPossible(String enrollmentUid) {
        return Single.fromCallable(() -> {
            boolean local = d2.enrollmentModule()
                    .enrollments()
                    .uid(enrollmentUid)
                    .blockingGet().state() == State.TO_POST;
            boolean hasAuthority = d2.userModule()
                    .authorities()
                    .byName().eq("F_ENROLLMENT_CASCADE_DELETE")
                    .one().blockingExists();
            return local || hasAuthority;
        }).flatMap(canDelete -> {
            if (canDelete) {
                return Single.fromCallable(() -> {
                    EnrollmentObjectRepository enrollmentObjectRepository = d2.enrollmentModule()
                            .enrollments().uid(enrollmentUid);
                    enrollmentObjectRepository.setStatus(
                            enrollmentObjectRepository.blockingGet().status()
                    );
                    enrollmentObjectRepository.blockingDelete();
                    return !d2.enrollmentModule().enrollments().byTrackedEntityInstance().eq(teiUid)
                            .byDeleted().isFalse()
                            .byStatus().eq(EnrollmentStatus.ACTIVE).blockingGet().isEmpty();
                });
            } else {
                return Single.error(new AuthorityException(null));
            }
        });
    }

    @Override
    public Flowable<List<RelationshipViewModel>> listTeiRelationships() {
        return Flowable.fromIterable(
                d2.relationshipModule().relationships().getByItem(
                        RelationshipItem.builder().trackedEntityInstance(
                                RelationshipItemTrackedEntityInstance.builder().trackedEntityInstance(teiUid).build()).build()
                ))
                .map(relationship -> {
                    RelationshipType relationshipType = null;
                    for (RelationshipType type : d2.relationshipModule().relationshipTypes().blockingGet())
                        if (type.uid().equals(relationship.relationshipType()))
                            relationshipType = type;

                    String relationshipTEIUid;
                    RelationshipViewModel.RelationshipDirection direction;
                    if (!teiUid.equals(relationship.from().trackedEntityInstance().trackedEntityInstance())) {
                        relationshipTEIUid = relationship.from().trackedEntityInstance().trackedEntityInstance();
                        direction = RelationshipViewModel.RelationshipDirection.FROM;
                    } else {
                        relationshipTEIUid = relationship.to().trackedEntityInstance().trackedEntityInstance();
                        direction = RelationshipViewModel.RelationshipDirection.TO;
                    }

                    TrackedEntityInstance tei = d2.trackedEntityModule().trackedEntityInstances().withTrackedEntityAttributeValues().uid(relationshipTEIUid).blockingGet();
                    List<TrackedEntityTypeAttribute> typeAttributes = d2.trackedEntityModule().trackedEntityTypeAttributes()
                            .byTrackedEntityTypeUid().eq(tei.trackedEntityType())
                            .byDisplayInList().isTrue()
                            .blockingGet();
                    List<String> attributeUids = new ArrayList<>();
                    for (TrackedEntityTypeAttribute typeAttribute : typeAttributes)
                        attributeUids.add(typeAttribute.trackedEntityAttribute().uid());
                    List<TrackedEntityAttributeValue> attributeValues = d2.trackedEntityModule().trackedEntityAttributeValues().byTrackedEntityInstance().eq(tei.uid())
                            .byTrackedEntityAttribute().in(attributeUids).blockingGet();

                    return RelationshipViewModel.create(relationship, relationshipType, direction, relationshipTEIUid, attributeValues);
                })
                .toList().toFlowable();
    }

    @Override
    public Single<Integer> getNoteCount() {
        return d2.enrollmentModule().enrollments()
                .byProgram().eq(programUid)
                .byTrackedEntityInstance().eq(teiUid)
                .withNotes()
                .one()
                .get()
                .map(enrollment -> enrollment.notes() != null ? enrollment.notes().size() : 0);
    }
}