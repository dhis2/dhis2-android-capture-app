package org.dhis2.usescases.programEventDetail;

import android.database.Cursor;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.tuples.Pair;
import org.dhis2.utils.DateUtils;
import org.dhis2.utils.ValueUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.event.Event;
import org.hisp.dhis.android.core.event.EventCollectionRepository;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.period.DatePeriod;
import org.hisp.dhis.android.core.program.Program;
import org.hisp.dhis.android.core.program.ProgramStageDataElement;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Observable;

import static android.text.TextUtils.isEmpty;

/**
 * QUADRAM. Created by ppajuelo on 02/11/2017.
 */

public class ProgramEventDetailRepositoryImpl implements ProgramEventDetailRepository {

    private final BriteDatabase briteDatabase;
    private final String programUid;
    private D2 d2;

    ProgramEventDetailRepositoryImpl(String programUid, BriteDatabase briteDatabase, D2 d2) {
        this.programUid = programUid;
        this.briteDatabase = briteDatabase;
        this.d2 = d2;
    }

    @NonNull
    @Override
    public LiveData<PagedList<ProgramEventViewModel>> filteredProgramEvents(List<DatePeriod> dateFilter, List<String> orgUnitFilter, List<CategoryOptionCombo> catOptCombList) {
        EventCollectionRepository eventRepo = d2.eventModule().events.byProgramUid().eq(programUid);
        if (!dateFilter.isEmpty())
            eventRepo = eventRepo.byEventDate().inDatePeriods(dateFilter);
        if (!orgUnitFilter.isEmpty())
            eventRepo = eventRepo.byOrganisationUnitUid().in(orgUnitFilter);
        if (!catOptCombList.isEmpty())
            for (CategoryOptionCombo catOptComb : catOptCombList)
                eventRepo = eventRepo.byAttributeOptionComboUid().eq(catOptComb.uid());
        DataSource dataSource = eventRepo.byState().notIn(State.TO_DELETE).orderByEventDate(RepositoryScope.OrderByDirection.DESC).withAllChildren().getDataSource().map(event -> transformToProgramEventModel(event));
        return new LivePagedListBuilder(new DataSource.Factory() {
            @Override
            public DataSource create() {
                return dataSource;
            }
        }, 20).build();
//        return Transformations.switchMap(eventRepo.byState().notIn(State.TO_DELETE).orderByEventDate(RepositoryScope.OrderByDirection.DESC).withAllChildren().getPaged(20), events -> transform(events));Transformations.switchMap(eventRepo.byState().notIn(State.TO_DELETE).orderByEventDate(RepositoryScope.OrderByDirection.DESC).withAllChildren().getPaged(20), events -> transform(events));
    }

    private ProgramEventViewModel transformToProgramEventModel(Event event) {
        String orgUnitName = getOrgUnitName(event.organisationUnit());
        List<String> showInReportsDataElements = new ArrayList<>();
        for (ProgramStageDataElement programStageDataElement : d2.programModule().programStages.uid(event.programStage()).withAllChildren().get().programStageDataElements()) {
            if (programStageDataElement.displayInReports())
                showInReportsDataElements.add(programStageDataElement.dataElement().uid());
        }
        List<Pair<String, String>> data = getData(event.trackedEntityDataValues(), showInReportsDataElements);
        boolean hasExpired = isExpired(event);
        boolean inOrgUnitRange = checkOrgUnitRange(event.organisationUnit(), event.eventDate());
        CategoryOptionCombo catOptComb = d2.categoryModule().categoryOptionCombos.uid(event.attributeOptionCombo()).get();
        String attributeOptionCombo = catOptComb != null && !catOptComb.displayName().equals("default") ? catOptComb.displayName() : "";

        return ProgramEventViewModel.create(
                event.uid(),
                event.organisationUnit(),
                orgUnitName,
                event.eventDate(),
                event.state(),
                data,
                event.status(),
                hasExpired || !inOrgUnitRange,
                attributeOptionCombo);
    }

    @NonNull
    @Override
    public Observable<Program> program() {
        return Observable.just(d2.programModule().programs.uid(programUid).withAllChildren().get());
    }

    private LiveData<PagedList<ProgramEventViewModel>> transform(PagedList<Event> events) {

        DataSource dataSource = events.getDataSource().map(this::transformToProgramEventModel);

        return new LivePagedListBuilder(new DataSource.Factory() {
            @Override
            public DataSource create() {
                return dataSource;
            }
        }, 20).build();
    }

    private boolean isExpired(Event event) {
        Program program = d2.programModule().programs.uid(event.program()).get();
        return DateUtils.getInstance().isEventExpired(event.eventDate(),
                event.completedDate(),
                event.status(),
                program.completeEventsExpiryDays(),
                program.expiryPeriodType(),
                program.expiryDays());
    }

    private boolean checkOrgUnitRange(String orgUnitUid, Date eventDate) {
        boolean inRange = true;
        OrganisationUnit orgUnit = d2.organisationUnitModule().organisationUnits.uid(orgUnitUid).get();
        if (orgUnit.openingDate() != null && eventDate.before(orgUnit.openingDate()))
            inRange = false;
        if (orgUnit.closedDate() != null && eventDate.after(orgUnit.closedDate()))
            inRange = false;


        return inRange;
    }

    private String getOrgUnitName(String orgUnitUid) {
        return d2.organisationUnitModule().organisationUnits.uid(orgUnitUid).get().displayName();
    }

    private List<Pair<String, String>> getData(List<TrackedEntityDataValue> dataValueList, List<String> showInReportsDataElements) {
        List<Pair<String, String>> data = new ArrayList<>();

        if (dataValueList != null)
            for (TrackedEntityDataValue dataValue : dataValueList) {
                DataElement de = d2.dataElementModule().dataElements.uid(dataValue.dataElement()).get();
                if (de != null && showInReportsDataElements.contains(de.uid())) {
                    String displayName = !isEmpty(de.displayFormName()) ? de.displayFormName() : de.displayName();
                    String value = dataValue.value();
                    if (de.optionSet() != null)
                        value = ValueUtils.optionSetCodeToDisplayName(briteDatabase, de.optionSet().uid(), value);
                    else if (de.valueType().equals(ValueType.ORGANISATION_UNIT))
                        value = ValueUtils.orgUnitUidToDisplayName(briteDatabase, value);

                    //TODO: Would be good to check other value types to render value (coordinates)
                    data.add(Pair.create(displayName, value));
                }
            }

        return data;
    }


    @NonNull
    @Override
    public Observable<List<OrganisationUnitModel>> orgUnits() {
        String SELECT_ORG_UNITS = "SELECT * FROM " + OrganisationUnitModel.TABLE + " " +
                "WHERE uid IN (SELECT UserOrganisationUnit.organisationUnit FROM UserOrganisationUnit " +
                "WHERE UserOrganisationUnit.organisationUnitScope = 'SCOPE_DATA_CAPTURE')";
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, SELECT_ORG_UNITS)
                .mapToList(OrganisationUnitModel::create);
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
    public Observable<List<Category>> catCombo() {
        Program program = d2.programModule().programs.uid(programUid).withAllChildren().get();
        CategoryCombo categoryCombo = d2.categoryModule().categoryCombos.byUid().eq(program.categoryCombo().uid()).withAllChildren().one().get();
        if (categoryCombo.isDefault())
            return Observable.just(new ArrayList<>());
        List<String> categoriesUids = new ArrayList<>();
        for (Category category : categoryCombo.categories())
            categoriesUids.add(category.uid());
        List<Category> categories = d2.categoryModule().categories.byUid().in(categoriesUids).withAllChildren().get();
        return Observable.just(categories);
    }

    @Override
    public boolean getAccessDataWrite() {
        boolean canWrite;
        canWrite = d2.programModule().programs.uid(programUid).get().access().data().write();
        if (canWrite && d2.programModule().programStages.byProgramUid().eq(programUid).one().get() != null)
            canWrite = d2.programModule().programStages.byProgramUid().eq(programUid).one().get().access().data().write();
        else if (d2.programModule().programStages.byProgramUid().eq(programUid).one().get() == null)
            canWrite = false;

        return canWrite;
    }

    @Override
    public List<CategoryOptionCombo> catOptionCombo(List<CategoryOption> selectedOptions) {
        List<CategoryOptionCombo> categoryOptionComboList = d2.categoryModule().categoryOptionCombos.byCategoryComboUid().eq(
                d2.programModule().programs.uid(programUid).get().categoryCombo().uid()
        ).withAllChildren().get();


        List<CategoryOptionCombo> finalCatOptComb = new ArrayList<>();
        if (categoryOptionComboList != null)
            for (CategoryOptionCombo categoryOptionCombo : categoryOptionComboList) {
                for (CategoryOption catOpt : categoryOptionCombo.categoryOptions()) {
                    if (selectedOptions.contains(catOpt) && !finalCatOptComb.contains(categoryOptionCombo))
                        finalCatOptComb.add(categoryOptionCombo);
                }
            }

        return finalCatOptComb;

    }
}