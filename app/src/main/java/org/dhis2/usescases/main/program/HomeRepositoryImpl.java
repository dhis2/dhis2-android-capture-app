package org.dhis2.usescases.main.program;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.period.DatePeriod;
import org.hisp.dhis.android.core.user.UserOrganisationUnitLinkModel;

import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.Flowable;
import io.reactivex.Observable;

import static org.hisp.dhis.android.core.program.ProgramType.WITHOUT_REGISTRATION;
import static org.hisp.dhis.android.core.program.ProgramType.WITH_REGISTRATION;

class HomeRepositoryImpl implements HomeRepository {


    private final BriteDatabase briteDatabase;
    private final D2 d2;

    HomeRepositoryImpl(BriteDatabase briteDatabase, D2 d2) {
        this.briteDatabase = briteDatabase;
        this.d2 = d2;
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

                    String typeName;
                    if (program.programType() == WITH_REGISTRATION) {
                        typeName = program.trackedEntityType() != null ? program.trackedEntityType().displayName() : "TEI";
                        if (typeName == null)
                            typeName = d2.trackedEntityModule().trackedEntityTypes.uid(program.trackedEntityType().uid()).get().displayName();
                    } else if (program.programType() == WITHOUT_REGISTRATION)
                        typeName = "Events";
                    else
                        typeName = "DataSets";

                    int count = 0;
                    if (program.programType() == WITHOUT_REGISTRATION)
                        if (!dateFilter.isEmpty())
                            count = d2.eventModule().events.byProgramUid().eq(program.uid()).byEventDate().inDatePeriods(dateFilter).count();
                        else
                            count = d2.eventModule().events.byProgramUid().eq(program.uid()).count();
                    else {
                        if (!dateFilter.isEmpty()) {
                            count = d2.eventModule().events.byProgramUid().eq(program.uid()).byEventDate().inDatePeriods(dateFilter).countTrackedEntityInstances();
                        } else
                            count = d2.eventModule().events.byProgramUid().eq(program.uid()).countTrackedEntityInstances();
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
                            true
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
        /*String SELECT_ORG_UNITS_BY_PARENT = "SELECT OrganisationUnit.* FROM OrganisationUnit " +
                "JOIN UserOrganisationUnit ON UserOrganisationUnit.organisationUnit = OrganisationUnit.uid " +
                "WHERE OrganisationUnit.parent = ? AND UserOrganisationUnit.organisationUnitScope = 'SCOPE_DATA_CAPTURE' " +
                "ORDER BY OrganisationUnit.displayName ASC";

        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, SELECT_ORG_UNITS_BY_PARENT, parentUid)
                .mapToList(OrganisationUnitModel::create);*/
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
