package org.dhis2.usescases.teiDashboard;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.dhis2.R;
import org.dhis2.commons.data.tuples.Pair;
import org.dhis2.commons.data.tuples.Trio;
import org.dhis2.commons.resources.ResourceManager;
import org.dhis2.utils.AuthorityException;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.ValueUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.ObjectWithUid;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentCollectionRepository;
import org.hisp.dhis.android.core.enrollment.EnrollmentObjectRepository;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.legendset.Legend;
import org.hisp.dhis.android.core.maintenance.D2Error;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramIndicator;
import org.hisp.dhis.android.core.program.ProgramRuleActionType;
import org.hisp.dhis.android.core.program.ProgramStage;
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute;
import org.hisp.dhis.android.core.relationship.RelationshipType;
import org.hisp.dhis.android.core.systeminfo.SystemInfo;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityType;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityTypeAttribute;

import java.util.ArrayList;
import java.util.List;

import dhis2.org.analytics.charts.Charts;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.Single;
import timber.log.Timber;

public class DashboardRepositoryImpl implements DashboardRepository {

    private final D2 d2;
    private final ResourceManager resources;
    private final String enrollmentUid;
    @Nullable
    private final Charts charts;

    private String teiUid;

    private String programUid;

    private TeiAttributesProvider teiAttributesProvider;


    public DashboardRepositoryImpl(D2 d2,
                                   @Nullable Charts charts,
                                   String teiUid,
                                   String programUid,
                                   String enrollmentUid,
                                   ResourceManager resources,
                                   TeiAttributesProvider teiAttributesProvider) {
        this.d2 = d2;
        this.teiUid = teiUid;
        this.programUid = programUid;
        this.enrollmentUid = enrollmentUid;
        this.resources = resources;
        this.charts = charts;
        this.teiAttributesProvider = teiAttributesProvider;
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
    public Observable<Enrollment> getEnrollment() {
        return d2.enrollmentModule().enrollments().uid(enrollmentUid).get().toObservable();
    }

    @Override
    public Observable<List<Event>> getTEIEnrollmentEvents(String programUid, String teiUid) {

        return d2.eventModule().events().byEnrollmentUid().eq(enrollmentUid)
                .byDeleted().isFalse()
                .orderByTimeline(RepositoryScope.OrderByDirection.ASC)
                .get().toFlowable().flatMapIterable(events -> events).map(event -> {
                    if (Boolean.FALSE
                            .equals(d2.programModule().programs().uid(programUid).blockingGet().ignoreOverdueEvents()))
                        if (event.status() == EventStatus.SCHEDULE
                                && event.dueDate().before(DateUtils.getInstance().getToday()))
                            event = updateState(event, EventStatus.OVERDUE);

                    return event;
                }).toList()
                .toObservable();
    }

    @Override
    public Observable<List<Event>> getEnrollmentEventsWithDisplay(String programUid, String teiUid) {
        return d2.eventModule().events().byEnrollmentUid().eq(enrollmentUid).get()
                .toObservable()
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
            ObjectWithUid legendSet = null;
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
                R.drawable.ic_navigation_relationships
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
        return d2.programModule().programs().uid(programUid).get()
                .map(program -> program.categoryComboUid())
                .flatMap(catComboUid -> d2.categoryModule().categoryCombos().uid(catComboUid).get())
                .toObservable();
    }

    @Override
    public boolean isStageFromProgram(String stageUid) {
        List<ProgramStage> programStages = getProgramStages(programUid).blockingFirst();
        boolean stageIsInProgram = false;
        for (ProgramStage stage : programStages) {
            if (stage.uid().equals(stageUid)) {
                stageIsInProgram = true;
                break;
            }
        }
        return stageIsInProgram;
    }

    @Override
    public CategoryOptionCombo catOptionCombo(String catComboUid) {
        return d2.categoryModule().categoryOptionCombos().uid(catComboUid).blockingGet();
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
            return teiAttributesProvider.getValuesFromProgramTrackedEntityAttributesByProgram(programUid, teiUid)
                    .map(attributesValues -> {
                        List<TrackedEntityAttributeValue> formattedValues = new ArrayList<>();
                        for (TrackedEntityAttributeValue attributeValue : attributesValues) {
                            if (attributeValue.value() != null) {
                                TrackedEntityAttribute attribute = d2.trackedEntityModule().trackedEntityAttributes().uid(attributeValue.trackedEntityAttribute()).blockingGet();
                                if (attribute.valueType() != ValueType.IMAGE) {
                                    formattedValues.add(
                                            ValueUtils.transform(d2, attributeValue, attribute.valueType(), attribute.optionSet() != null ? attribute.optionSet().uid() : null)
                                    );
                                }
                            } else {
                                formattedValues.add(
                                        TrackedEntityAttributeValue.builder()
                                                .trackedEntityAttribute(attributeValue.trackedEntityAttribute())
                                                .trackedEntityInstance(teiUid)
                                                .value("")
                                                .build()
                                );
                            }
                        }
                        return formattedValues;
                    }).toObservable();

        } else {
            String teType = d2.trackedEntityModule().trackedEntityInstances().uid(teiUid).blockingGet().trackedEntityType();
            List<TrackedEntityAttributeValue> attributeValues = new ArrayList<>();

            for (TrackedEntityAttributeValue attributeValue: teiAttributesProvider.getValuesFromTrackedEntityTypeAttributes(teType, teiUid)) {
                if (attributeValue != null) {
                    TrackedEntityAttribute attribute = d2.trackedEntityModule().trackedEntityAttributes().uid(attributeValue.trackedEntityAttribute()).blockingGet();
                    if (attribute.valueType() != ValueType.IMAGE && attributeValue.value() != null) {
                        attributeValues.add(
                                ValueUtils.transform(d2, attributeValue, attribute.valueType(), attribute.optionSet() != null ? attribute.optionSet().uid() : null)
                        );
                    }
                }
            }

            if (attributeValues.isEmpty()) {
                for (TrackedEntityAttributeValue attributeValue: teiAttributesProvider.getValuesFromProgramTrackedEntityAttributes(teType, teiUid)) {
                    if (attributeValue != null) {
                        TrackedEntityAttribute attribute = d2.trackedEntityModule().trackedEntityAttributes().uid(attributeValue.trackedEntityAttribute()).blockingGet();
                        attributeValues.add(
                                ValueUtils.transform(d2, attributeValue, attribute.valueType(), attribute.optionSet() != null ? attribute.optionSet().uid() : null)
                        );
                    }
                }
            }
            return Observable.just(attributeValues);
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
                .eq(teiUid).byDeleted().eq(false);
        if (showOnlyActive)
            enrollmentRepo.byStatus().eq(EnrollmentStatus.ACTIVE);
        return enrollmentRepo.get().toObservable().flatMapIterable(enrollments -> enrollments)
                .map(Enrollment::program).toList().toObservable()
                .map(programUids -> d2.programModule().programs().byUid().in(programUids).blockingGet());
    }

    @Override
    public Observable<List<Enrollment>> getTEIEnrollments(String teiUid) {
        return d2.enrollmentModule().enrollments().byTrackedEntityInstance().eq(teiUid).byDeleted().eq(false).get().toObservable();
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
    public Single<Integer> getNoteCount() {
        return d2.enrollmentModule().enrollments()
                .withNotes()
                .uid(enrollmentUid)
                .get()
                .map(enrollment -> enrollment.notes() != null ? enrollment.notes().size() : 0);
    }

    @Override
    public EnrollmentStatus getEnrollmentStatus(String enrollmentUid) {
        return d2.enrollmentModule().enrollments().uid(enrollmentUid).blockingGet().status();
    }

    @Override
    public Observable<StatusChangeResultCode> updateEnrollmentStatus(String enrollmentUid, EnrollmentStatus status) {
        try {
            if (d2.programModule().programs().uid(programUid).blockingGet().access().data().write()) {
                if (reopenCheck(status)) {
                    d2.enrollmentModule().enrollments().uid(enrollmentUid).setStatus(status);
                    return Observable.just(StatusChangeResultCode.CHANGED);
                } else {
                    return Observable.just(StatusChangeResultCode.ACTIVE_EXIST);
                }
            } else {
                return Observable.just(StatusChangeResultCode.WRITE_PERMISSION_FAIL);
            }
        } catch (D2Error error) {
            return Observable.just(StatusChangeResultCode.FAILED);
        }
    }

    private boolean reopenCheck(EnrollmentStatus status) {
        return status != EnrollmentStatus.ACTIVE || d2.enrollmentModule().enrollments()
                .byProgram().eq(programUid)
                .byTrackedEntityInstance().eq(teiUid)
                .byStatus().eq(EnrollmentStatus.ACTIVE)
                .blockingIsEmpty();
    }

    @Override
    public boolean programHasRelationships() {
        if (programUid != null) {
            String teiTypeUid = d2.programModule().programs()
                    .uid(programUid)
                    .blockingGet()
                    .trackedEntityType()
                    .uid();
            return !relationshipsForTeiType(teiTypeUid).blockingFirst().isEmpty();
        } else {
            return false;
        }
    }

    @Override
    public boolean programHasAnalytics() {
        if (programUid != null) {
            List<String> enrollmentScopeRulesUids = d2.programModule().programRules()
                    .byProgramUid().eq(programUid)
                    .byProgramStageUid().isNull()
                    .blockingGetUids();
            boolean hasDisplayRuleActions = !d2.programModule().programRuleActions()
                    .byProgramRuleUid().in(enrollmentScopeRulesUids)
                    .byProgramRuleActionType().in(ProgramRuleActionType.DISPLAYKEYVALUEPAIR, ProgramRuleActionType.DISPLAYTEXT)
                    .blockingIsEmpty();
            boolean hasProgramIndicator = !d2.programModule().programIndicators().byProgramUid().eq(programUid).blockingIsEmpty();
            boolean hasCharts = charts != null && !charts.geEnrollmentCharts(enrollmentUid).isEmpty();
            return hasDisplayRuleActions || hasProgramIndicator || hasCharts;
        } else {
            return false;
        }
    }

    @Override
    public String getTETypeName() {
        return getTrackedEntityInstance(teiUid).flatMap(tei ->
                d2.trackedEntityModule().trackedEntityTypes()
                        .uid(tei.trackedEntityType())
                        .get()
                        .toObservable()
        ).blockingFirst().displayName();
    }
}
