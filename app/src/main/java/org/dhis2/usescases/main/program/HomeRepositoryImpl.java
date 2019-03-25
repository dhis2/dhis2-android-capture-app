package org.dhis2.usescases.main.program;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.enrollment.Enrollment;
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.period.DatePeriod;
import org.hisp.dhis.android.core.user.UserOrganisationUnitLinkModel;

import java.util.ArrayList;
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
    public Flowable<List<ProgramViewModel>> aggregatesModels(List<DatePeriod> dateFilter, List<String> orgUnitFilter) {
        return Flowable.just(d2.dataSetModule().dataSets)
                .flatMap(programRepo -> Flowable.fromIterable(programRepo.withAllChildren().get()))
                .map(dataSet -> {
                    int count = d2.dataSetModule().dataSets.byCategoryComboUid().like(dataSet.categoryCombo().uid()).count();

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
                        dataSet.access().data().write());}
                ).toList().toFlowable();
    }

    @NonNull
    @Override
    public Flowable<List<ProgramViewModel>> programModels(List<DatePeriod> dateFilter, List<String> orgUnitFilter) {

        return Flowable.just(d2.programModule().programs)
                .flatMap(programRepo -> {
                    if (orgUnitFilter != null && !orgUnitFilter.isEmpty())
                        return Flowable.fromIterable(programRepo.byOrganisationUnitList(orgUnitFilter).withObjectStyle().withAllChildren().get());
                    else
                        return Flowable.fromIterable(programRepo.withObjectStyle().withAllChildren().get());
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
                            count = d2.eventModule().events.byProgramUid().eq(program.uid()).byEventDate().inDatePeriods(dateFilter).get().size();
                        else
                            count = d2.eventModule().events.byProgramUid().eq(program.uid()).get().size();
                    else {
                        if (!dateFilter.isEmpty()) {
                            count = getEnrollmentCount(d2.eventModule().events
                                    .byEnrollmentUid().in(
                                            getEnrollmentsForProgram(program.uid())
                                    )
                                    .byEventDate().inDatePeriods(dateFilter)
                                    .get());
                        } else
                            count = getEnrollmentCount(d2.eventModule().events
                                    .byEnrollmentUid().in(
                                            getEnrollmentsForProgram(program.uid())
                                    )
                                    .get());
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

    private List<String> getEnrollmentsForProgram(String programUid) {
        List<Enrollment> enrollments = d2.enrollmentModule().enrollments.byProgram().eq(programUid)
                .byStatus().eq(EnrollmentStatus.ACTIVE)
                .get();

        List<String> enrollmentsUid = new ArrayList<>();
        for (Enrollment enrollment : enrollments)
            enrollmentsUid.add(enrollment.uid());
        return enrollmentsUid;
    }

    private int getEnrollmentCount(List<Event> events) {
        List<String> enrollmentsUid = new ArrayList<>();
        for (Event event : events)
            if (!enrollmentsUid.contains(event.enrollment()))
                enrollmentsUid.add(event.enrollment());

        return enrollmentsUid.size();
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnitModel>> orgUnits(String parentUid) {
        String SELECT_ORG_UNITS_BY_PARENT = "SELECT OrganisationUnit.* FROM OrganisationUnit " +
                "JOIN UserOrganisationUnit ON UserOrganisationUnit.organisationUnit = OrganisationUnit.uid " +
                "WHERE OrganisationUnit.parent = ? AND UserOrganisationUnit.organisationUnitScope = 'SCOPE_DATA_CAPTURE' " +
                "ORDER BY OrganisationUnit.displayName ASC";

        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, SELECT_ORG_UNITS_BY_PARENT, parentUid)
                .mapToList(OrganisationUnitModel::create);
    }


    @NonNull
    @Override
    public Observable<List<OrganisationUnitModel>> orgUnits() {
        String SELECT_ORG_UNITS =
                "SELECT * FROM " + OrganisationUnitModel.TABLE + ", " + UserOrganisationUnitLinkModel.TABLE + " " +
                        "WHERE " + OrganisationUnitModel.TABLE + "." + OrganisationUnitModel.Columns.UID + " = " + UserOrganisationUnitLinkModel.TABLE + "." + UserOrganisationUnitLinkModel.Columns.ORGANISATION_UNIT +
                        " AND " + UserOrganisationUnitLinkModel.TABLE + "." + UserOrganisationUnitLinkModel.Columns.ORGANISATION_UNIT_SCOPE + " = '" + OrganisationUnitModel.Scope.SCOPE_DATA_CAPTURE +
                        "' AND UserOrganisationUnit.root = '1' " +
                        " ORDER BY " + OrganisationUnitModel.TABLE + "." + OrganisationUnitModel.Columns.DISPLAY_NAME + " ASC";
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, SELECT_ORG_UNITS)
                .mapToList(OrganisationUnitModel::create);
    }
}
