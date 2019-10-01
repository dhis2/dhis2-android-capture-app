package org.dhis2.usescases.main.program;

import androidx.annotation.NonNull;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.dataset.DataSetCompleteRegistration;
import org.hisp.dhis.android.core.dataset.DataSetInstance;
import org.hisp.dhis.android.core.dataset.DataSetInstanceCollectionRepository;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.dataset.DataSetElement;
import org.hisp.dhis.android.core.datavalue.DataValue;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.period.DatePeriod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;

import static org.hisp.dhis.android.core.program.ProgramType.WITHOUT_REGISTRATION;
import static org.hisp.dhis.android.core.program.ProgramType.WITH_REGISTRATION;

class HomeRepositoryImpl implements HomeRepository {

    private final D2 d2;
    private final String eventLabel;

    HomeRepositoryImpl(D2 d2,String eventLabel) {
        this.d2 = d2;
        this.eventLabel = eventLabel;
    }

    @NonNull
    @Override
    public Flowable<List<ProgramViewModel>> aggregatesModels(List<DatePeriod> dateFilter, final List<String> orgUnitFilter, List<State> statesFilter) {

        return Flowable.just(d2.dataSetModule().dataSets)
                .flatMap(programRepo -> Flowable.fromIterable(programRepo.withCompulsoryDataElementOperands()
                        .withDataInputPeriods().withDataSetElements().withIndicators().withSections().withStyle().blockingGet()))
                .map(dataSet -> {
                            DataSetInstanceCollectionRepository repo = d2.dataSetModule().dataSetInstances.byDataSetUid().eq(dataSet.uid());
                            if (!orgUnitFilter.isEmpty())
                                repo = repo.byOrganisationUnitUid().in(orgUnitFilter);
                            if (!dateFilter.isEmpty())
                                repo = repo.byPeriodStartDate().inDatePeriods(dateFilter);

                            int count = 0;
                            if(!statesFilter.isEmpty()) {
                                for (DataSetInstance instance : repo.blockingGet())
                                    if (statesFilter.contains(instance.state()))
                                        count++;
                            }else
                                count = repo.blockingCount();

                            State state = State.SYNCED;

                            for (DataSetElement dataSetElement : dataSet.dataSetElements()) {
                                DataElement dataElement = d2.dataElementModule().dataElements.uid(dataSetElement.dataElement().uid()).blockingGet();
                                List<String> categoryOptionCombos;
                                if(dataSetElement.categoryCombo() != null)
                                    categoryOptionCombos = UidsHelper.getUidsList(
                                            d2.categoryModule().categoryCombos.withCategoryOptionCombos().uid(dataSetElement.categoryCombo().uid()).blockingGet().categoryOptionCombos());
                                else
                                    categoryOptionCombos = UidsHelper.getUidsList(
                                            d2.categoryModule().categoryCombos.withCategoryOptionCombos().uid(dataElement.categoryComboUid()).blockingGet().categoryOptionCombos());
                                List<String> attributeOptionCombos = UidsHelper.getUidsList(
                                        d2.categoryModule().categoryCombos.withCategoryOptionCombos().uid(dataSet.categoryCombo().uid()).blockingGet().categoryOptionCombos()
                                );
                                for (DataValue dataValue : d2.dataValueModule().dataValues.byAttributeOptionComboUid().in(attributeOptionCombos)
                                        .byCategoryOptionComboUid().in(categoryOptionCombos)
                                        .byDataElementUid().eq(dataSetElement.dataElement().uid()).blockingGet()) {
                                    if (dataValue.state() != State.SYNCED)
                                        state = State.TO_UPDATE;
                                }
                            }

                            List<DataSetCompleteRegistration> dscr = d2.dataSetModule().dataSetCompleteRegistrations
                                    .byDataSetUid().eq(dataSet.uid()).blockingGet();

                            for(DataSetCompleteRegistration completeRegistration: dscr){
                                if(completeRegistration.state() != State.SYNCED) {
                                    if (completeRegistration.deleted() != null && completeRegistration.deleted())
                                        state = State.TO_UPDATE;
                                    else
                                        state = completeRegistration.state();
                                }
                            }


                            return ProgramViewModel.create(
                                    dataSet.uid(),
                                    dataSet.displayName(),
                                    dataSet.style() != null ? dataSet.style().color() : null,
                                    dataSet.style() != null ? dataSet.style().icon() : null,
                                    count,
                                    null,
                                    "DataSets",
                                    "",
                                    dataSet.displayDescription(),
                                    true,
                                    dataSet.access().data().write(),
                                    state.name());
                        }
                ).toList().toFlowable();
    }

    @NonNull
    @Override
    public Flowable<List<ProgramViewModel>> programModels(List<DatePeriod> dateFilter, List<String> orgUnitFilter, List<State> statesFilter) {

        return Flowable.just(d2.organisationUnitModule().organisationUnits.byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE).blockingGet())
                .map(captureOrgUnits -> {
                    Iterator<OrganisationUnit> it = captureOrgUnits.iterator();
                    List<String> captureOrgUnitUids = new ArrayList();
                    while (it.hasNext()) {
                        OrganisationUnit ou = it.next();
                        captureOrgUnitUids.add(ou.uid());
                    }
                    return captureOrgUnitUids;
                })
                .flatMap(orgUnits -> Flowable.just(d2.programModule().programs.withStyle().withCategoryCombo()
                        .withProgramIndicators().withProgramRules().withProgramRuleVariables().withProgramSections()
                        .withProgramStages().withProgramTrackedEntityAttributes().withStyle()
                        .withRelatedProgram().withTrackedEntityType().byOrganisationUnitList(orgUnits)))
                .flatMap(programRepo -> {
                    if (orgUnitFilter != null && !orgUnitFilter.isEmpty())
                        return Flowable.fromIterable(programRepo.byOrganisationUnitList(orgUnitFilter).blockingGet());
                    else
                        return Flowable.fromIterable(programRepo.blockingGet());
                })
                .map(program -> {

                    String typeName;
                    if (program.programType() == WITH_REGISTRATION) {
                        typeName = program.trackedEntityType() != null ? program.trackedEntityType().displayName() : "TEI";
                        if (typeName == null)
                            typeName = d2.trackedEntityModule().trackedEntityTypes.uid(program.trackedEntityType().uid()).blockingGet().displayName();
                    } else if (program.programType() == WITHOUT_REGISTRATION)
                        typeName = eventLabel;
                    else
                        typeName = "DataSets";

                    int count;
                    State state = State.SYNCED;
                    if (program.programType() == WITHOUT_REGISTRATION) {
                        if (!dateFilter.isEmpty()) {
                            if (!orgUnitFilter.isEmpty()) {
                                if(!statesFilter.isEmpty()) {
                                    count = d2.eventModule().events
                                            .byProgramUid().eq(program.uid())
                                            .byEventDate().inDatePeriods(dateFilter)
                                            .byOrganisationUnitUid().in(orgUnitFilter)
                                            .byState().in(statesFilter)
                                            .blockingCount();
                                }
                                else
                                    count = d2.eventModule().events
                                            .byProgramUid().eq(program.uid())
                                            .byEventDate().inDatePeriods(dateFilter)
                                            .byOrganisationUnitUid().in(orgUnitFilter)
                                            .blockingCount();
                            } else {
                                if(!statesFilter.isEmpty())
                                    count = d2.eventModule().events
                                            .byProgramUid().eq(program.uid())
                                            .byEventDate().inDatePeriods(dateFilter)
                                            .byState().in(statesFilter)
                                            .blockingCount();
                                else
                                    count = d2.eventModule().events
                                            .byProgramUid().eq(program.uid())
                                            .byEventDate().inDatePeriods(dateFilter)
                                            .blockingCount();
                            }
                        } else if (!orgUnitFilter.isEmpty()) {
                            if(!statesFilter.isEmpty())
                                count = d2.eventModule().events
                                        .byProgramUid().eq(program.uid())
                                        .byOrganisationUnitUid().in(orgUnitFilter)
                                        .byState().in(statesFilter)
                                        .blockingCount();
                            else
                                count = d2.eventModule().events
                                        .byProgramUid().eq(program.uid())
                                        .byOrganisationUnitUid().in(orgUnitFilter)
                                        .blockingCount();
                        } else {
                            if(!statesFilter.isEmpty())
                                count = d2.eventModule().events
                                        .byProgramUid().eq(program.uid())
                                        .byState().in(statesFilter)
                                        .blockingCount();
                            else
                                count = d2.eventModule().events
                                        .byProgramUid().eq(program.uid())
                                        .blockingCount();
                        }

                        if (!d2.eventModule().events.byProgramUid().eq(program.uid()).byState().in(State.ERROR, State.WARNING).blockingGet().isEmpty())
                            state = State.WARNING;
                        else if (!d2.eventModule().events.byProgramUid().eq(program.uid()).byState().in(State.SENT_VIA_SMS, State.SYNCED_VIA_SMS).blockingGet().isEmpty())
                            state = State.SENT_VIA_SMS;
                        else if (!d2.eventModule().events.byProgramUid().eq(program.uid()).byState().in(State.TO_UPDATE, State.TO_POST).blockingGet().isEmpty() ||
                                !d2.eventModule().events.byProgramUid().eq(program.uid()).byDeleted().isTrue().blockingGet().isEmpty())
                            state = State.TO_UPDATE;

                    } else {
                        List<String> programUids = new ArrayList<>();
                        programUids.add(program.uid());
                        if (!dateFilter.isEmpty()) {
                            List<Enrollment> enrollments;
                            if (!orgUnitFilter.isEmpty()) {
                                if(!statesFilter.isEmpty())
                                    enrollments = d2.enrollmentModule().enrollments
                                            .byProgram().in(programUids)
                                            .byEnrollmentDate().inDatePeriods(dateFilter)
                                            .byOrganisationUnit().in(orgUnitFilter)
                                            .byStatus().eq(EnrollmentStatus.ACTIVE)
                                            .byDeleted().isFalse()
                                            .byState().in(statesFilter)
                                            .blockingGet();
                                else
                                    enrollments = d2.enrollmentModule().enrollments
                                            .byProgram().in(programUids)
                                            .byEnrollmentDate().inDatePeriods(dateFilter)
                                            .byOrganisationUnit().in(orgUnitFilter)
                                            .byStatus().eq(EnrollmentStatus.ACTIVE)
                                            .byDeleted().isFalse()
                                            .blockingGet();
                            } else {
                                if(!statesFilter.isEmpty())
                                    enrollments = d2.enrollmentModule().enrollments
                                            .byProgram().in(programUids)
                                            .byEnrollmentDate().inDatePeriods(dateFilter)
                                            .byStatus().eq(EnrollmentStatus.ACTIVE)
                                            .byDeleted().isFalse()
                                            .byState().in(statesFilter)
                                            .blockingGet();
                                else
                                    enrollments = d2.enrollmentModule().enrollments
                                            .byProgram().in(programUids)
                                            .byEnrollmentDate().inDatePeriods(dateFilter)
                                            .byStatus().eq(EnrollmentStatus.ACTIVE)
                                            .byDeleted().isFalse()
                                            .blockingGet();
                            }
                            count = countEnrollment(enrollments);
                        } else if (!orgUnitFilter.isEmpty()) {
                            List<Enrollment> enrollments;
                            if(!statesFilter.isEmpty())
                                enrollments = d2.enrollmentModule().enrollments
                                        .byProgram().in(programUids)
                                        .byOrganisationUnit().in(orgUnitFilter)
                                        .byStatus().eq(EnrollmentStatus.ACTIVE)
                                        .byDeleted().isFalse()
                                        .byState().in(statesFilter)
                                        .blockingGet();
                            else
                                enrollments = d2.enrollmentModule().enrollments
                                        .byProgram().in(programUids)
                                        .byOrganisationUnit().in(orgUnitFilter)
                                        .byStatus().eq(EnrollmentStatus.ACTIVE)
                                        .byDeleted().isFalse()
                                        .blockingGet();

                            count = countEnrollment(enrollments);
                        } else {
                            List<Enrollment> enrollments;
                            if(!statesFilter.isEmpty())
                                enrollments = d2.enrollmentModule().enrollments
                                        .byProgram().in(programUids)
                                        .byStatus().eq(EnrollmentStatus.ACTIVE)
                                        .byDeleted().isFalse()
                                        .byState().in(statesFilter)
                                        .blockingGet();
                            else
                                enrollments = d2.enrollmentModule().enrollments
                                        .byProgram().in(programUids)
                                        .byStatus().eq(EnrollmentStatus.ACTIVE)
                                        .byDeleted().isFalse()
                                        .blockingGet();

                            count = countEnrollment(enrollments);
                        }

                        if (!d2.trackedEntityModule().trackedEntityInstances.byProgramUids(programUids).byState().in(State.ERROR, State.WARNING).blockingGet().isEmpty())
                            state = State.WARNING;
                        else if (!d2.trackedEntityModule().trackedEntityInstances.byProgramUids(programUids).byState().in(State.SENT_VIA_SMS, State.SYNCED_VIA_SMS).blockingGet().isEmpty())
                            state = State.SENT_VIA_SMS;
                        else if (!d2.trackedEntityModule().trackedEntityInstances.byProgramUids(programUids).byState().in(State.TO_UPDATE, State.TO_POST).blockingGet().isEmpty() ||
                                !d2.trackedEntityModule().trackedEntityInstances.byProgramUids(programUids).byDeleted().isTrue().blockingGet().isEmpty())
                            state = State.TO_UPDATE;
                    }


                    return ProgramViewModel.create(
                            program.uid(),
                            program.displayName(),
                            program.style() != null ? program.style().color() : null,
                            program.style() != null ? program.style().icon() : null,
                            count,
                            program.trackedEntityType() != null ? program.trackedEntityType().uid() : null,
                            typeName,
                            program.programType() != null ? program.programType().name() : null,
                            program.displayDescription(),
                            true,
                            true,
                            state.name()
                    );
                }).toList().toFlowable();
    }

    private int countEnrollment(List<Enrollment> enrollments) {
        List<String> teiUids = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            if (!teiUids.contains(enrollment.trackedEntityInstance()))
                teiUids.add(enrollment.trackedEntityInstance());

        }
        return teiUids.size();
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnit>> orgUnits(String parentUid) {
        return Observable.defer(() -> Observable.just(
                d2.organisationUnitModule().organisationUnits
                        .byParentUid().eq(parentUid)
                        .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                        .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                        .blockingGet()
        ));
    }


    @NonNull
    @Override
    public Observable<List<OrganisationUnit>> orgUnits() {
        return Observable.defer(() -> Observable.just(
                d2.organisationUnitModule().organisationUnits
                        .byRootOrganisationUnit(true)
                        .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                        .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                        .blockingGet()
        ));
    }
}
