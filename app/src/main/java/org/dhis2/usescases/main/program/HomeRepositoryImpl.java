package org.dhis2.usescases.main.program;

import androidx.annotation.NonNull;

import android.database.Cursor;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.dataset.DataSetElement;
import org.hisp.dhis.android.core.datavalue.DataValue;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.period.DatePeriod;

import java.util.ArrayList;
import java.util.List;

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
    public Flowable<List<ProgramViewModel>> aggregatesModels(List<DatePeriod> dateFilter, final List<String> orgUnitFilter) {
        final String GET_DATA_SETS = "SELECT " +
                "DataValue.organisationUnit, " +
                "DataValue.period, " +
                "DataValue.attributeOptionCombo " +
                "FROM DataValue " +
                "JOIN DataSetDataElementLink " +
                "ON DataSetDataElementLink.dataElement = DataValue.dataElement " +
                "WHERE DataSetDataElementLink.dataSet = ? %s " +
                "GROUP BY DataValue.period,DataValue.organisationUnit,DataValue.attributeOptionCombo";

        final String DATA_SETS_ORG_UNIT_FILTER = "AND DataValue.organisationUnit IN (%s) ";

        return Flowable.just(d2.dataSetModule().dataSets)
                .flatMap(programRepo -> Flowable.fromIterable(programRepo.withAllChildren().get()))
                .map(dataSet -> {
                            String SQL = GET_DATA_SETS;
                            String orgUnits = "";
                            if (orgUnits != null && !orgUnits.isEmpty()) {
                                StringBuilder orgUnitUids = new StringBuilder("");
                                for (int i = 0; i < orgUnitFilter.size(); i++) {
                                    orgUnitUids.append(orgUnitFilter.get(i));
                                    if (i != orgUnitFilter.size() - 1)
                                        orgUnitUids.append(",");
                                }

                                orgUnits = String.format(DATA_SETS_ORG_UNIT_FILTER, orgUnitFilter);
                            }

                            SQL = String.format(SQL, orgUnits);

                            int count;
                            try (Cursor dataSetCursor = briteDatabase.query(SQL, dataSet.uid())) {
                                count = dataSetCursor.getCount();
                            }

                            State state = State.SYNCED;
                            for(DataSetElement dataSetElement: dataSet.dataSetElements()){
                                for(DataValue dataValue: d2.dataValueModule().dataValues.byDataElementUid().eq(dataSetElement.dataElement().uid()).get()){
                                    if(dataValue.state() != State.SYNCED)
                                        state = State.TO_UPDATE;
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

                    int count;
                    State state = State.SYNCED;
                    if (program.programType() == WITHOUT_REGISTRATION) {
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

                        if (!d2.eventModule().events.byProgramUid().eq(program.uid()).byState().in(State.ERROR, State.WARNING).get().isEmpty())
                            state = State.WARNING;
                        else if (!d2.eventModule().events.byProgramUid().eq(program.uid()).byState().in(State.SENT_VIA_SMS, State.SYNCED_VIA_SMS).get().isEmpty())
                            state = State.SENT_VIA_SMS;
                        else if (!d2.eventModule().events.byProgramUid().eq(program.uid()).byState().in(State.TO_UPDATE, State.TO_POST, State.TO_DELETE).get().isEmpty())
                            state = State.TO_UPDATE;

                    } else {
                        List<String> programUids = new ArrayList<>();
                        programUids.add(program.uid());
                        if (!dateFilter.isEmpty()) {
                            if (!orgUnitFilter.isEmpty()) {
                                count = d2.trackedEntityModule().trackedEntityInstances
                                        .byProgramUids(programUids)
                                        .byLastUpdated().inDatePeriods(dateFilter)
                                        .byOrganisationUnitUid().in(orgUnitFilter).count();
                              /*  count = d2.eventModule().events
                                        .byProgramUid().eq(program.uid())
                                        .byEventDate().inDatePeriods(dateFilter)
                                        .byOrganisationUnitUid().in(orgUnitFilter)
                                        .countTrackedEntityInstances();*/
                            } else {
                                count = d2.trackedEntityModule().trackedEntityInstances
                                        .byProgramUids(programUids)
                                        .byLastUpdated().inDatePeriods(dateFilter).count();
                               /* count = d2.eventModule().events
                                        .byProgramUid().eq(program.uid())
                                        .byEventDate().inDatePeriods(dateFilter)
                                        .countTrackedEntityInstances();*/
                            }
                        } else if (!orgUnitFilter.isEmpty()) {
                            count = d2.trackedEntityModule().trackedEntityInstances
                                    .byProgramUids(programUids)
                                    .byOrganisationUnitUid().in(orgUnitFilter).count();
                          /*  count = d2.eventModule().events
                                    .byProgramUid().eq(program.uid())
                                    .byOrganisationUnitUid().in(orgUnitFilter)
                                    .countTrackedEntityInstances();*/
                        } else {
                            count = d2.trackedEntityModule().trackedEntityInstances
                                    .byProgramUids(programUids).count();
                           /* count = d2.eventModule().events
                                    .byProgramUid().eq(program.uid())
                                    .countTrackedEntityInstances();*/
                        }

                        if (!d2.trackedEntityModule().trackedEntityInstances.byProgramUids(programUids).byState().in(State.ERROR, State.WARNING).get().isEmpty())
                            state = State.WARNING;
                        else if (!d2.trackedEntityModule().trackedEntityInstances.byProgramUids(programUids).byState().in(State.SENT_VIA_SMS, State.SYNCED_VIA_SMS).get().isEmpty())
                            state = State.SENT_VIA_SMS;
                        else if (!d2.trackedEntityModule().trackedEntityInstances.byProgramUids(programUids).byState().in(State.TO_UPDATE, State.TO_POST, State.TO_DELETE).get().isEmpty())
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
