package org.dhis2.usescases.main.program;

import androidx.annotation.NonNull;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.period.DatePeriod;
import org.hisp.dhis.android.core.program.Program;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;

import static org.hisp.dhis.android.core.program.ProgramType.WITHOUT_REGISTRATION;
import static org.hisp.dhis.android.core.program.ProgramType.WITH_REGISTRATION;

class HomeRepositoryImpl implements HomeRepository {

    private final D2 d2;

    HomeRepositoryImpl(D2 d2) {
        this.d2 = d2;
    }

    private String getTypeName(Program program) {
        String typeName;
        if (program.programType() == WITH_REGISTRATION) {
            typeName = program.trackedEntityType() != null ? program.trackedEntityType().displayName() : "TEI";
            if (typeName == null)
                typeName = d2.trackedEntityModule().trackedEntityTypes.uid(program.trackedEntityType().uid()).get().displayName();
        } else if (program.programType() == WITHOUT_REGISTRATION)
            typeName = "Events";
        else
            typeName = "DataSets";
        return typeName;
    }

    private int getProgramWORegistrationCount(Program program, List<DatePeriod> dateFilter, List<String> orgUnitFilter) {
        int count = 0;
        if (!dateFilter.isEmpty()) {
            if (!orgUnitFilter.isEmpty()) {
                count = d2.eventModule().events
                        .byProgramUid().eq(program.uid())
                        .byEventDate().inDatePeriods(dateFilter)
                        .byOrganisationUnitUid().in(orgUnitFilter)
                        .count();
            } else {
                count = d2.eventModule().events
                        .byProgramUid().eq(program.uid())
                        .byEventDate().inDatePeriods(dateFilter)
                        .count();
            }
        } else if (!orgUnitFilter.isEmpty()) {
            count = d2.eventModule().events
                    .byProgramUid().eq(program.uid())
                    .byOrganisationUnitUid().in(orgUnitFilter)
                    .count();
        } else {
            count = d2.eventModule().events
                    .byProgramUid().eq(program.uid())
                    .count();
        }
        return count;
    }

    private int getProgramWithRegistrationCount(List<DatePeriod> dateFilter, List<String> orgUnitFilter, List<String> programUids) {
        int count = 0;
        if (!dateFilter.isEmpty()) {
            if (!orgUnitFilter.isEmpty()) {
                count = d2.trackedEntityModule().trackedEntityInstances
                        .byProgramUids(programUids)
                        .byLastUpdated().inDatePeriods(dateFilter)
                        .byOrganisationUnitUid().in(orgUnitFilter).count();
            } else {
                count = d2.trackedEntityModule().trackedEntityInstances
                        .byProgramUids(programUids)
                        .byLastUpdated().inDatePeriods(dateFilter).count();
            }
        } else if (!orgUnitFilter.isEmpty()) {
            count = d2.trackedEntityModule().trackedEntityInstances
                    .byProgramUids(programUids)
                    .byOrganisationUnitUid().in(orgUnitFilter).count();
        } else {
            count = d2.trackedEntityModule().trackedEntityInstances
                    .byProgramUids(programUids).count();
        }

        return count;
    }

    private State getProgramWORegistrationState(Program program) {
        State state = State.SYNCED;

        if (!d2.eventModule().events.byProgramUid().eq(program.uid()).byState().in(State.ERROR, State.WARNING).get().isEmpty())
            state = State.WARNING;
        else if (!d2.eventModule().events.byProgramUid().eq(program.uid()).byState().in(State.SENT_VIA_SMS, State.SYNCED_VIA_SMS).get().isEmpty())
            state = State.SENT_VIA_SMS;
        else if (!d2.eventModule().events.byProgramUid().eq(program.uid()).byState().in(State.TO_UPDATE, State.TO_POST, State.TO_DELETE).get().isEmpty())
            state = State.TO_UPDATE;

        return state;
    }

    private State getProgramWithRegistrationState(List<String> programUids) {
        State state = State.SYNCED;

        if (!d2.trackedEntityModule().trackedEntityInstances.byProgramUids(programUids).byState().in(State.ERROR, State.WARNING).get().isEmpty())
            state = State.WARNING;
        else if (!d2.trackedEntityModule().trackedEntityInstances.byProgramUids(programUids).byState().in(State.SENT_VIA_SMS, State.SYNCED_VIA_SMS).get().isEmpty())
            state = State.SENT_VIA_SMS;
        else if (!d2.trackedEntityModule().trackedEntityInstances.byProgramUids(programUids).byState().in(State.TO_UPDATE, State.TO_POST, State.TO_DELETE).get().isEmpty())
            state = State.TO_UPDATE;

        return state;
    }

    @NonNull
    @Override
    public Flowable<List<ProgramViewModel>> programModels(List<DatePeriod> dateFilter, List<String> orgUnitFilter) {

        return Flowable.just(d2.programModule().programs)
                .flatMap(programRepo -> {
                    if (orgUnitFilter != null && !orgUnitFilter.isEmpty())
                        return Flowable.fromIterable(programRepo.byOrganisationUnitList(orgUnitFilter).withStyle().withAllChildren().get());
                    else
                        return Flowable.fromIterable(programRepo.withStyle().withAllChildren().get());
                })
                .map(program -> {
                    String typeName = getTypeName(program);

                    int count;
                    State state = State.SYNCED;
                    if (program.programType() == WITHOUT_REGISTRATION) {
                        count = getProgramWORegistrationCount(program, dateFilter, orgUnitFilter);
                        state = getProgramWORegistrationState(program);

                    } else {
                        List<String> programUids = new ArrayList<>();
                        programUids.add(program.uid());
                        count = getProgramWithRegistrationCount(dateFilter, orgUnitFilter, programUids);
                        state = getProgramWithRegistrationState(programUids);
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

    @NonNull
    @Override
    public Observable<List<OrganisationUnit>> orgUnits(String parentUid) {
        return Observable.defer(() -> Observable.just(
                d2.organisationUnitModule().organisationUnits
                        .byParentUid().eq(parentUid)
                        .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE)
                        .orderByDisplayName(RepositoryScope.OrderByDirection.ASC)
                        .get()
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
                        .get()
        ));
    /*    String SELECT_ORG_UNITS =
                "SELECT * FROM " + OrganisationUnitModel.TABLE + ", " + UserOrganisationUnitLinkModel.TABLE + " " +
                        "WHERE " + OrganisationUnitModel.TABLE + "." + OrganisationUnitModel.Columns.UID + " = " + UserOrganisationUnitLinkModel.TABLE + "." + UserOrganisationUnitLinkModel.Columns.ORGANISATION_UNIT +
                        " AND " + UserOrganisationUnitLinkModel.TABLE + "." + UserOrganisationUnitLinkModel.Columns.ORGANISATION_UNIT_SCOPE + " = '" + OrganisationUnitModel.Scope.SCOPE_DATA_CAPTURE +
                        "' AND UserOrganisationUnit.root = '1' " +
                        " ORDER BY " + OrganisationUnitModel.TABLE + "." + OrganisationUnitModel.Columns.DISPLAY_NAME + " ASC";
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, SELECT_ORG_UNITS)
                .mapToList(OrganisationUnitModel::create);*/
    }
}
