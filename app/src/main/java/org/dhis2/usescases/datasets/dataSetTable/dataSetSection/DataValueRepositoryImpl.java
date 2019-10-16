package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import android.content.ContentValues;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.dataapproval.DataApproval;
import org.hisp.dhis.android.core.dataapproval.DataApprovalState;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.dataelement.DataElementOperand;
import org.hisp.dhis.android.core.dataset.DataInputPeriod;
import org.hisp.dhis.android.core.dataset.DataSet;
import org.hisp.dhis.android.core.dataset.DataSetCompleteRegistration;
import org.hisp.dhis.android.core.dataset.DataSetElement;
import org.hisp.dhis.android.core.dataset.Section;
import org.hisp.dhis.android.core.datavalue.DataValueObjectRepository;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.hisp.dhis.android.core.period.Period;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Flowable;

public class DataValueRepositoryImpl implements DataValueRepository {

    private final D2 d2;
    private BriteDatabase briteDatabase;
    private String dataSetUid;

    public DataValueRepositoryImpl(D2 d2, BriteDatabase briteDatabase, String dataSetUid) {
        this.d2 = d2;
        this.briteDatabase = briteDatabase;
        this.dataSetUid = dataSetUid;
    }

    @Override
    public Flowable<Period> getPeriod(String periodId) {
        return Flowable.fromCallable(() -> d2.periodModule().periods().byPeriodId().eq(periodId).one().blockingGet());
    }

    @Override
    public Flowable<List<DataInputPeriod>> getDataInputPeriod() {
        return Flowable.fromCallable(() -> d2.dataSetModule().dataSets.withDataInputPeriods().byUid().eq(dataSetUid).one().blockingGet().dataInputPeriods());
    }

    public Flowable<List<CategoryCombo>> getCatCombo(String sectionName) {

        List<String> categoryCombos = new ArrayList<>();
        List<DataSetElement> dataSetElements = d2.dataSetModule().dataSets.withDataSetElements().uid(dataSetUid).blockingGet().dataSetElements();

        if (!sectionName.equals("NO_SECTION")) {
            List<DataElement> dataElements = d2.dataSetModule().sections.withDataElements().byDataSetUid().eq(dataSetUid).byName().eq(sectionName).one().blockingGet().dataElements();
            for (DataSetElement dataSetElement : dataSetElements) {
                for (DataElement dataElement : dataElements) {

                    if (dataSetElement.dataElement().uid().equals(dataElement.uid()))

                        if (dataSetElement.categoryCombo() != null && !categoryCombos.contains(dataSetElement.categoryCombo().uid()))
                            categoryCombos.add(dataSetElement.categoryCombo().uid());

                        else if (dataElement.categoryCombo() != null && !categoryCombos.contains(dataElement.categoryComboUid()))
                            categoryCombos.add(dataElement.categoryComboUid());
                }
            }
        } else {
            for (DataSetElement dataSetElement : dataSetElements) {
                if (dataSetElement.categoryCombo() != null)
                    categoryCombos.add(dataSetElement.categoryCombo().uid());
                else {
                    DataElement dataElement = d2.dataElementModule().dataElements().uid(dataSetElement.dataElement().uid()).blockingGet();
                    categoryCombos.add(dataElement.categoryCombo().uid());
                }
            }
        }
        return d2.categoryModule().categoryCombos().byUid().in(categoryCombos).withCategories().withCategoryOptionCombos().orderByDisplayName(RepositoryScope.OrderByDirection.ASC).get().toFlowable();

}

    @Override
    public Flowable<DataSet> getDataSet() {
        return Flowable.fromCallable(() -> d2.dataSetModule().dataSets.byUid().eq(dataSetUid).one().blockingGet());

    }

    public Completable updateValue(DataSetTableModel dataValue) {

        DataValueObjectRepository dataValueObject = d2.dataValueModule().dataValues.value(
                dataValue.period(),
                dataValue.organisationUnit(),
                dataValue.dataElement(),
                dataValue.categoryOptionCombo(),
                dataValue.attributeOptionCombo()
        );

        if (dataValue.value() != null && !dataValue.value().isEmpty())
            return dataValueObject.set(dataValue.value());
        else
            return dataValueObject.delete();

    }

    @Override
    public Flowable<Map<String, List<List<Pair<CategoryOption, Category>>>>> getCatOptions(String sectionName, String catCombo) {
        Map<String, List<List<Pair<CategoryOption, Category>>>> map = new HashMap<>();
        map.put(catCombo, getMap(catCombo));
        return Flowable.just(map);
    }


    private List<List<Pair<CategoryOption, Category>>> getMap(String catCombo) {
        List<List<Pair<CategoryOption, Category>>> finalList = new ArrayList<>();
        List<Category> categories = d2.categoryModule().categoryCombos().withCategories().withCategoryOptionCombos().byUid().eq(catCombo).one().blockingGet().categories();

        for (Category category : categories) {
            List<CategoryOption> catOptions = d2.categoryModule().categories().withCategoryOptions().byUid().eq(category.uid()).one().blockingGet().categoryOptions();
            for (CategoryOption catOption : catOptions) {
                boolean add = true;
                for (List<Pair<CategoryOption, Category>> catComboList : finalList) {
                    if (finalList.contains(Pair.create(catOption, category)))
                        add = false;
                }
                if (add) {

                    if (finalList.size() != 0 && finalList.get(finalList.size() - 1).get(0).val1().uid().equals(category.uid())) {
                        finalList.get(finalList.size() - 1).add(Pair.create(catOption, category));
                    } else {
                        List<Pair<CategoryOption, Category>> list = new ArrayList<>();
                        list.add(Pair.create(catOption, category));
                        finalList.add(list);
                    }
                }
            }
        }
        return finalList;
    }


    private DataElement transformDataElement(DataElement dataElement, List<DataSetElement> override) {
        for (DataSetElement dataSetElement : override)
            if (dataSetElement.dataElement().uid().equals(dataElement.uid()) && dataSetElement.categoryCombo() != null)
                return DataElement.builder()
                        .uid(dataElement.uid())
                        .code(dataElement.code())
                        .name(dataElement.name())
                        .displayName(dataElement.displayName())
                        .shortName(dataElement.shortName())
                        .displayShortName(dataElement.displayShortName())
                        .description(dataElement.description())
                        .displayDescription(dataElement.displayDescription())
                        .valueType(dataElement.valueType())
                        .zeroIsSignificant(dataElement.zeroIsSignificant())
                        .aggregationType(dataElement.aggregationType())
                        .formName(dataElement.formName())
                        .domainType(dataElement.domainType())
                        .displayFormName(dataElement.displayFormName())
                        .optionSet(dataElement.optionSet())
                        .categoryCombo(dataSetElement.categoryCombo()).build();

        return dataElement;
    }

    @Override
    public Flowable<List<DataSetTableModel>> getDataValues(String orgUnitUid, String periodType, String initPeriodType, String catOptionComb, String sectionName) {

        Map<String, String> mapDataElementCatCombo = new HashMap<>();

        return Flowable.just(d2.dataSetModule().dataSets.withDataSetElements().byUid().eq(dataSetUid).one().blockingGet())
                .flatMapIterable(dataSet -> {
                    List<DataSetElement> dataElements = new ArrayList<>();
                    if (!sectionName.equals("NO_SECTION")) {
                        List<DataElement> dataElementSection = d2.dataSetModule().sections.withDataElements()
                                .byDataSetUid().eq(dataSetUid).byName().eq(sectionName).one().blockingGet().dataElements();
                        for (DataElement dataElement : dataElementSection) {
                            for (DataSetElement dataSetElement : dataSet.dataSetElements())
                                if (dataSetElement.dataElement().uid().equals(dataElement.uid()))
                                    dataElements.add(dataSetElement);
                        }
                    } else
                        dataElements = dataSet.dataSetElements();

                    return dataElements;
                })
                .flatMapIterable(dataSetElement -> {
                            if (dataSetElement.categoryCombo() != null)
                                mapDataElementCatCombo.put(dataSetElement.dataElement().uid(), dataSetElement.categoryCombo().uid());
                            else
                                mapDataElementCatCombo.put(dataSetElement.dataElement().uid(),
                                        d2.dataElementModule().dataElements().byUid().eq(dataSetElement.dataElement().uid()).one().blockingGet().categoryCombo().uid());

                            return d2.dataValueModule().dataValues.byDataElementUid().eq(dataSetElement.dataElement().uid())
                                    .byAttributeOptionComboUid().eq(catOptionComb)
                                    .byPeriod().eq(initPeriodType)
                                    .byOrganisationUnitUid().eq(orgUnitUid)
                                    .byDeleted().isFalse()
                                    .blockingGet();
                        }
                ).map(dataValue -> {
                    List<CategoryOption> categoryOptions = d2.categoryModule().categoryOptionCombos().withCategoryOptions()
                            .byUid().eq(dataValue.categoryOptionCombo()).one().blockingGet().categoryOptions();
                    List<String> uidCatOptions = new ArrayList<>();
                    for (CategoryOption catOption : categoryOptions)
                        uidCatOptions.add(catOption.uid());

                    return DataSetTableModel.create(dataValue.id(), dataValue.dataElement(), dataValue.period(),
                            dataValue.organisationUnit(), dataValue.categoryOptionCombo(), dataValue.attributeOptionCombo(),
                            dataValue.value(), dataValue.storedBy(), "",//no used anywhere, remove this field
                            uidCatOptions, mapDataElementCatCombo.get(dataValue.dataElement()));

                }).toList().toFlowable();
    }

    @Override
    public Flowable<List<DataElementOperand>> getCompulsoryDataElements() {
        return d2.dataSetModule().dataSets.withCompulsoryDataElementOperands().uid(dataSetUid).get()
                .map(DataSet::compulsoryDataElementOperands).toFlowable();
    }

    @Override
    public Flowable<List<DataElementOperand>> getGreyFields(String sectionName) {
        if (!sectionName.isEmpty() && !sectionName.equals("NO_SECTION"))
            return d2.dataSetModule().sections.withGreyedFields().byDataSetUid().eq(dataSetUid).byName().eq(sectionName).one().get()
                .map(Section::greyedFields).toFlowable();
        else
            return Flowable.just(new ArrayList<>());
    }

    @Override
    public Flowable<Section> getSectionByDataSet(String section) {
        if (!section.isEmpty() && !section.equals("NO_SECTION"))
            return Flowable.just(d2.dataSetModule().sections.byDataSetUid().eq(dataSetUid).byName().eq(section).one().blockingGet());
        else
            return Flowable.just(Section.builder().uid("").build());

    }

    @Override
    public Flowable<Boolean> completeDataSet(String orgUnitUid, String periodInitialDate, String catCombo) {
        boolean updateOrInserted;
        DataSetCompleteRegistration completeRegistration = d2.dataSetModule().dataSetCompleteRegistrations
                .byAttributeOptionComboUid().eq(catCombo).byOrganisationUnitUid().eq(orgUnitUid)
                .byPeriod().eq(periodInitialDate).byDataSetUid().eq(dataSetUid).one().blockingGet();

        String where = "period = ? AND dataSet = ? AND attributeOptionCombo = ? AND organisationUnit = ?";

        ContentValues contentValues = new ContentValues();
        contentValues.put(DataSetCompleteRegistration.Columns.STATE,
                completeRegistration!= null? State.TO_UPDATE.name(): State.TO_POST.name());
        contentValues.put(DataSetCompleteRegistration.Columns.DELETED, false);
        String completeDate = DateUtils.databaseDateFormat().format(Calendar.getInstance().getTime());
        contentValues.put("date", completeDate);
        String[] values = {periodInitialDate, dataSetUid, catCombo, orgUnitUid};

        updateOrInserted = briteDatabase.update(DataSetCompleteRegistration.class.getSimpleName(), contentValues, where, values) > 0;

        if (!updateOrInserted) {
            DataSetCompleteRegistration dataSetCompleteRegistration =
                    DataSetCompleteRegistration.builder().dataSet(dataSetUid)
                            .period(periodInitialDate)
                            .organisationUnit(orgUnitUid)
                            .attributeOptionCombo(catCombo)
                            .date(Calendar.getInstance().getTime())
                            .state(State.TO_POST).build();

            updateOrInserted = briteDatabase.insert(DataSetCompleteRegistration.class.getSimpleName(), dataSetCompleteRegistration.toContentValues()) > 0;
        }

        return Flowable.just(updateOrInserted);

    }

    @Override
    public Flowable<Boolean> reopenDataSet(String orgUnitUid, String periodInitialDate, String catCombo) {
        DataSetCompleteRegistration completeRegistration = d2.dataSetModule().dataSetCompleteRegistrations
                .byAttributeOptionComboUid().eq(catCombo)
                .byPeriod().eq(periodInitialDate)
                .byOrganisationUnitUid().eq(orgUnitUid)
                .byDataSetUid().eq(dataSetUid)
                .one().blockingGet();

        if(completeRegistration != null && (completeRegistration.state().equals(State.SYNCED) ||
                completeRegistration.state().equals(State.TO_UPDATE))) {
            String where = "period = ? AND dataSet = ? AND attributeOptionCombo = ? and organisationUnit = ? ";
            String[] values = {periodInitialDate, dataSetUid, catCombo, orgUnitUid};

            ContentValues contentValues = new ContentValues();
            contentValues.put(DataSetCompleteRegistration.Columns.DELETED, true);
            contentValues.put(DataSetCompleteRegistration.Columns.STATE, State.TO_UPDATE.name());
            String completeDate = DateUtils.databaseDateFormat().format(DateUtils.getInstance().getToday());
            contentValues.put("date", completeDate);

            return Flowable.just(briteDatabase.update(DataSetCompleteRegistration.class.getSimpleName(), contentValues, where, values) > 0);
        }else {
            String where = "period = ? AND dataSet = ? AND attributeOptionCombo = ? and organisationUnit = ? ";
            String[] values = {periodInitialDate, dataSetUid, catCombo, orgUnitUid};

            return Flowable.just(briteDatabase.delete(DataSetCompleteRegistration.class.getSimpleName(), where, values) > 0);
        }
    }

    @Override
    public Flowable<Boolean> isCompleted(String orgUnitUid, String periodInitialDate, String catCombo) {

        return Flowable.fromCallable(() -> {
            DataSetCompleteRegistration completeRegistration = d2.dataSetModule().dataSetCompleteRegistrations
                    .byDataSetUid().eq(dataSetUid)
                    .byAttributeOptionComboUid().eq(catCombo)
                    .byPeriod().eq(periodInitialDate)
                    .byOrganisationUnitUid().eq(orgUnitUid)
                    .one().blockingGet();
            return completeRegistration != null && !completeRegistration.deleted();
        });
    }

    @Override
    public Flowable<Boolean> isApproval(String orgUnit, String period, String attributeOptionCombo) {
        return Flowable.fromCallable(() -> {
            DataApproval dataApproval = d2.dataSetModule().dataApprovals
                    .byOrganisationUnitUid().eq(orgUnit)
                    .byPeriodId().eq(period)
                    .byAttributeOptionComboUid().eq(attributeOptionCombo)
                    .one().blockingGet();
            return dataApproval != null && dataApproval.state().equals(DataApprovalState.APPROVED_HERE);
        });
    }

    @Override
    public Flowable<List<DataElement>> getDataElements(CategoryCombo categoryCombo, String sectionName) {
        if (!sectionName.equals("NO_SECTION")) {
            List<DataElement> listDataElements = d2.dataSetModule().sections.withDataElements().byDataSetUid().eq(dataSetUid).byName().eq(sectionName).one().blockingGet().dataElements();
            List<DataElement> dataElementsOverride = new ArrayList<>();
            List<DataSetElement> dataSetElements = d2.dataSetModule().dataSets.withDataSetElements().uid(dataSetUid).blockingGet().dataSetElements();

            for(DataElement de: listDataElements) {
                DataElement override = transformDataElement(de, dataSetElements);
                if(override.categoryComboUid().equals(categoryCombo.uid()))
                    dataElementsOverride.add(override);
            }

            return Flowable.just(dataElementsOverride);
        }else {
            List<String> dataElementUids = new ArrayList<>();
            List<DataSetElement> dataSetElements = d2.dataSetModule().dataSets.withDataSetElements().byUid().eq(dataSetUid).one().blockingGet().dataSetElements();
            for (DataSetElement dataSetElement : dataSetElements) {
                if(dataSetElement.categoryCombo() != null && categoryCombo.uid().equals(dataSetElement.categoryCombo().uid()))
                    dataElementUids.add(dataSetElement.dataElement().uid());
                else{
                    String uid = d2.dataElementModule().dataElements().uid(dataSetElement.dataElement().uid()).blockingGet().categoryComboUid();
                    if(categoryCombo.uid().equals(uid))
                        dataElementUids.add(dataSetElement.dataElement().uid());
                }
            }
            return d2.dataElementModule().dataElements()
                        .byUid().in(dataElementUids)
                        .orderByName(RepositoryScope.OrderByDirection.ASC)
                        .get().toFlowable();
        }
    }

    @Override
    public List<CategoryOption> getCatOptionFromCatOptionCombo(CategoryOptionCombo categoryOptionCombo) {
        return d2.categoryModule().categoryOptionCombos().withCategoryOptions().uid(categoryOptionCombo.uid()).blockingGet().categoryOptions();
    }


    @Override
    public CategoryOption getCatOptionFromUid(String catOption) {
        return d2.categoryModule().categoryOptions().uid(catOption).blockingGet();
    }

    @Override
    public Flowable<Boolean> canWriteAny() {
        return d2.dataSetModule().dataSets.uid(dataSetUid).get().toFlowable()
                .flatMap(dataSet -> {
                    if (dataSet.access().data().write())
                        return d2.categoryModule().categoryOptionCombos().withCategoryOptions()
                                .byCategoryComboUid().eq(dataSet.categoryCombo().uid()).get().toFlowable()
                                .map(categoryOptionCombos -> {
                                    boolean canWriteCatOption = false;
                                    for (CategoryOptionCombo categoryOptionCombo : categoryOptionCombos) {
                                        for (CategoryOption categoryOption : categoryOptionCombo.categoryOptions())
                                            if (categoryOption.access().data().write()) {
                                                canWriteCatOption = true;
                                                break;
                                            }
                                    }
                                    boolean canWriteOrgUnit = false;

                                    if (canWriteCatOption) {
                                        List<OrganisationUnit> organisationUnits = d2.organisationUnitModule().organisationUnits.withDataSets()
                                                .byOrganisationUnitScope(OrganisationUnit.Scope.SCOPE_DATA_CAPTURE).blockingGet();

                                        for (OrganisationUnit organisationUnit : organisationUnits)
                                            for (DataSet dSet : organisationUnit.dataSets())
                                                if (dSet.uid().equals(dataSetUid)) {
                                                    canWriteOrgUnit = true;
                                                    break;
                                                }

                                    }

                                    return canWriteCatOption && canWriteOrgUnit;

                                });
                    else
                        return Flowable.just(false);
                });

    }
}
