package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import android.content.ContentValues;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.arch.helpers.UidsHelper;
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
import org.hisp.dhis.android.core.period.Period;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import timber.log.Timber;

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
        return Flowable.fromCallable(() -> d2.periodModule().periods.byPeriodId().eq(periodId).one().blockingGet());
    }

    @Override
    public Flowable<List<DataInputPeriod>> getDataInputPeriod() {
        return Flowable.fromCallable(() -> d2.dataSetModule().dataSets.withDataInputPeriods().byUid().eq(dataSetUid).one().blockingGet().dataInputPeriods());
    }

    /*@Override
    public Flowable<Map<String, List<String>>> getCategoryOptionComboCatOption() {

        Map<String, List<String>> map = new HashMap<>();
        return Flowable.fromCallable(() ->{
            List<DataElement> dataElements = d2.dataSetModule().sections.withDataElements().byDataSetUid().eq(dataSetUid).byName().eq(sectionName).one().blockingGet().dataElements();
                //List<DataSetElement> override = d2.dataSetModule().dataSets.byUid().eq(dataSetUid).withDataSetElements().one().blockingGet().dataSetElements();

                for(DataElement dataElement: dataElements){
                    //DataElement dataElement = d2.dataElementModule().dataElements.byUid().eq(dataSetElement.dataElement().uid()).one().blockingGet();
                    //DataElement dElement = transformDataElement(dataElement, override);
                    List<String> catOptions = UidsHelper.getUidsList(d2.categoryModule().categoryOptionCombos.withCategoryOptions()
                            .byCategoryComboUid().eq(dataElement.categoryComboUid()).one().blockingGet().categoryOptions());

                    if (map.containsKey(dataElement.uid())) {
                        map.get(dataElement.uid()).addAll(catOptions);
                    } else {
                        map.put(dataElement.uid(), catOptions);
                    }
                }
                Timber.tag("BREAKPOINT").d("getCategoryOptionComboCatOption()");

                return map;
        } );

    }*/

    @Override
    public Flowable<List<DataElement>> getDataElements(String sectionName) {
        if (!sectionName.equals("NO_SECTION"))
            return Flowable.fromCallable(() -> {
                List<String> dataElementsUid = UidsHelper.getUidsList(d2.dataSetModule().sections.withDataElements().byDataSetUid().eq(dataSetUid).byName().eq(sectionName).one().blockingGet().dataElements());
                List<DataElement> transformedDataElements = new ArrayList<>();
                List<DataElement> dataElements = d2.dataElementModule().dataElements.withStyle().byUid().in(dataElementsUid).orderByName(RepositoryScope.OrderByDirection.ASC).blockingGet();
                for (DataElement dataElement : dataElements) {
                    transformedDataElements.add(transformDataElement(dataElement, d2.dataSetModule().dataSets.withDataSetElements().byUid().eq(dataSetUid).one().blockingGet().dataSetElements()));
                }
                Timber.tag("BREAKPOINT").d("getDataElements()");
                return transformedDataElements;
            });

        return Flowable.fromCallable(() -> {
            List<DataSetElement> dataSetElements = d2.dataSetModule().dataSets.withDataSetElements().byUid().eq(dataSetUid).one().blockingGet().dataSetElements();
            List<DataElement> transformedDataElements = new ArrayList<>();
            List<String> uids = new ArrayList<>();
            for (DataSetElement dataSetElement : dataSetElements)
                uids.add(dataSetElement.dataElement().uid());

            List<DataElement> dataElements = d2.dataElementModule().dataElements.byUid().in(uids).orderByName(RepositoryScope.OrderByDirection.ASC).blockingGet();

            for (DataElement dataElement : dataElements) {
                transformedDataElements.add(transformDataElement(dataElement, dataSetElements));
            }
            Timber.tag("BREAKPOINT").d("getDataElements()");
            return transformedDataElements;
        });
    }

    public Flowable<List<CategoryCombo>> getCatCombo(String sectionName) {
        if (!sectionName.equals("NO_SECTION")) {
            List<String> dataElements = UidsHelper.getUidsList(d2.dataSetModule().sections.withDataElements().byDataSetUid().eq(dataSetUid).byName().eq(sectionName).one().blockingGet().dataElements());
            List<String> categoryCombos = new ArrayList<>();
            List<DataSetElement> dataSetElements = d2.dataSetModule().dataSets.withDataSetElements().byUid().eq(dataSetUid).one().blockingGet().dataSetElements();

            for (DataSetElement dataSetElement : dataSetElements){
                if(dataElements.contains(dataSetElement.dataElement().uid()))
                    if(dataSetElement.categoryCombo() != null)
                        categoryCombos.add(dataSetElement.categoryCombo().uid());
                    else
                        categoryCombos.add(d2.dataElementModule().dataElements.uid(dataSetElement.dataElement().uid()).blockingGet().categoryComboUid());
            }

            Timber.tag("BREAKPOINT").d("getCatCombo()");
            return d2.categoryModule().categoryCombos.byUid().in(categoryCombos).withCategories().withCategoryOptionCombos().get().toFlowable();
        }
        return Flowable.fromCallable(() -> {
            List<DataSetElement> dataSetElements = d2.dataSetModule().dataSets.byUid().eq(dataSetUid).withDataSetElements().one().blockingGet().dataSetElements();
            List<CategoryCombo> categoryCombos = new ArrayList<>();
            for (DataSetElement dataSetElement : dataSetElements) {
                if (dataSetElement.categoryCombo() != null)
                    categoryCombos.add(d2.categoryModule().categoryCombos.byUid().eq(dataSetElement.categoryCombo().uid()).withCategories().withCategoryOptionCombos().one().blockingGet());
                else {
                    DataElement dataElement = d2.dataElementModule().dataElements.byUid().eq(dataSetElement.dataElement().uid()).one().blockingGet();
                    categoryCombos.add(d2.categoryModule().categoryCombos.byUid().eq(dataElement.categoryCombo().uid()).withCategories().withCategoryOptionCombos().one().blockingGet());
                }
            }
            Timber.tag("BREAKPOINT").d("getCatCombo()");
            return categoryCombos;
        });
    }

    @Override
    public Flowable<List<Category>> getCategories(CategoryCombo categoryCombo){
        return d2.categoryModule().categories.withCategoryOptions().byUid().in(UidsHelper.getUids(categoryCombo.categories())).get().toFlowable();
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
    public Flowable<Map<String, List<CategoryOptionCombo>>> getCatOptionCombo() {

        return Flowable.fromCallable(() -> {
            Map<String, List<CategoryOptionCombo>> map = new HashMap<>();
            List<Section> sectionsList = d2.dataSetModule().sections.withDataElements().byDataSetUid().eq(dataSetUid).blockingGet();

            for (Section section : sectionsList) {
                List<DataElement> dataElements = section.dataElements();
                List<DataElement> dataElementOverrides = new ArrayList<>();

                List<DataSetElement> overrides = d2.dataSetModule().dataSets.withDataSetElements().byUid().eq(dataSetUid).one().blockingGet().dataSetElements();
                for (DataElement dataElement : dataElements)
                    dataElementOverrides.add(transformDataElement(dataElement, overrides));

                if (map.get(section.name()) == null)
                    map.put(section.name(), new ArrayList<>());

                for (DataElement dataElement : dataElementOverrides) {
                    boolean exist = false;
                    List<CategoryOptionCombo> listCatOption = d2.categoryModule().categoryOptionCombos.byCategoryComboUid().eq(dataElement.categoryCombo().uid()).blockingGet();
                    for (CategoryOptionCombo catOptionCombo : listCatOption) {
                        for (CategoryOptionCombo catOptionComboMap : map.get(section.name())) {
                            if (catOptionComboMap.uid().equals(catOptionCombo.uid()))
                                exist = true;
                        }

                        if (!exist)
                            map.get(section.name()).add(catOptionCombo);
                    }
                }
            }
            Timber.tag("BREAKPOINT").d("getCatOptionCombo()");
            return map;
        });
    }

    @Override
    public Flowable<Map<String, List<List<Pair<CategoryOption, Category>>>>> getCatOptions(String sectionName) {
        List<String> catCombos = new ArrayList<>();
        if (sectionName.equals("NO_SECTION"))
            return Flowable.fromCallable(() -> {
                List<String> dataElementUids = new ArrayList<>();
                List<DataElement> dataElements;
                for (DataSetElement dataSetElement : d2.dataSetModule().dataSets.withDataSetElements().byUid().eq(dataSetUid).one().blockingGet().dataSetElements()) {
                    dataElementUids.add(dataSetElement.dataElement().uid());
                    if(dataSetElement.categoryCombo() != null)
                        catCombos.add(dataSetElement.categoryCombo().uid());
                }
                dataElements = d2.dataElementModule().dataElements.withStyle().byUid().in(dataElementUids).orderByName(RepositoryScope.OrderByDirection.ASC).blockingGet();
                return getMap(catCombos, dataElements);
            });
        return Flowable.fromCallable(() -> {
            List<DataElement> dataElements = d2.dataSetModule().sections.withDataElements()
                    .byDataSetUid().eq(dataSetUid)
                    .byName().eq(sectionName)
                    .one().blockingGet()
                    .dataElements();

            return getMap(catCombos, dataElements);
        });
    }


    private Map<String, List<List<Pair<CategoryOption, Category>>>> getMap(List<String> catCombos, List<DataElement> dataElements) {
        Map<String, List<List<Pair<CategoryOption, Category>>>> map = new HashMap<>();
        for (DataElement dataElement : dataElements) {
            if(!catCombos.contains(dataElement.categoryCombo().uid()))
                catCombos.add(dataElement.categoryCombo().uid());
        }
        for (String catCombo : catCombos) {
            List<Category> categories = d2.categoryModule().categoryCombos.withCategories().withCategoryOptionCombos().byUid().eq(catCombo).one().blockingGet().categories();

            for (Category category : categories) {
                List<CategoryOption> catOptions = d2.categoryModule().categories.withCategoryOptions().byUid().eq(category.uid()).one().blockingGet().categoryOptions();
                for (CategoryOption catOption : catOptions) {
                    boolean add = true;
                    for(List<Pair<CategoryOption, Category>> catComboList : map.get(catCombo)){
                        if(catComboList.contains(Pair.create(catOption, category)))
                            add = false;
                    }
                    if(add){

                    if (map.get(catCombo).size() != 0 && map.get(catCombo).get(map.get(catCombo).size() - 1).get(0).val1().uid().equals(category.uid())) {
                        map.get(catCombo).get(map.get(catCombo).size() - 1).add(Pair.create(catOption, category));
                    } else {
                        List<Pair<CategoryOption, Category>> list = new ArrayList<>();
                        list.add(Pair.create(catOption, category));
                        map.get(catCombo).add(list);
                    }
                    }
                }
            }
        }
        Timber.tag("BREAKPOINT").d("getCatOptions()");
        return map;
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

        return Flowable.just(d2.dataSetModule().dataSets.withSections().withDataSetElements().byUid().eq(dataSetUid).one().blockingGet())
                .flatMapIterable(dataSet -> {
                    List<DataSetElement> dataElements = new ArrayList<>();
                    if (!sectionName.equals("NO_SECTION")) {
                        List<DataElement> dataElementSection = d2.dataSetModule().sections.withDataElements().byName().eq(sectionName).one().blockingGet().dataElements();
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
                                        d2.dataElementModule().dataElements.byUid().eq(dataSetElement.dataElement().uid()).one().blockingGet().categoryCombo().uid());

                            return d2.dataValueModule().dataValues.byDataElementUid().eq(dataSetElement.dataElement().uid())
                                    .byAttributeOptionComboUid().eq(catOptionComb)
                                    .byPeriod().eq(initPeriodType)
                                    .byOrganisationUnitUid().eq(orgUnitUid)
                                    .blockingGet();
                        }
                ).map(dataValue -> {
                    List<CategoryOption> categoryOptions = d2.categoryModule().categoryOptionCombos.withCategoryOptions()
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
    public Flowable<Map<String, Map<String, List<String>>>> getGreyedFields(String section) {

        Map<String, Map<String, List<String>>> mapData = new HashMap<>();

        return Flowable.fromCallable(() -> {
            List<DataElementOperand> operands;
            if(!section.isEmpty() && !section.equals("NO_SECTION")) {
                operands = d2.dataSetModule().sections.withGreyedFields().withDataElements().byDataSetUid().eq(dataSetUid).byName().eq(section).one().blockingGet().greyedFields();

                for (DataElementOperand operand : operands) {
                    List<String> catOptions;
                    if (operand.categoryOptionCombo() == null) {
                        List<DataSetElement> override = d2.dataSetModule().dataSets.byUid().eq(dataSetUid).withDataSetElements().one().blockingGet().dataSetElements();
                        DataElement dataElement = d2.dataElementModule().dataElements.byUid().eq(operand.dataElement().uid()).one().blockingGet();
                        DataElement dataElementOverride = transformDataElement(dataElement, override);

                        List<CategoryOptionCombo> catOptionCombos = d2.categoryModule().categoryOptionCombos.byCategoryComboUid().eq(dataElementOverride.categoryCombo().uid()).withCategoryOptions().blockingGet();
                        HashMap<String, List<String>> mapCatOptions = new HashMap<>();

                        for (CategoryOptionCombo catOptionCombo : catOptionCombos) {
                            mapCatOptions.put(catOptionCombo.uid(), UidsHelper.getUidsList(catOptionCombo.categoryOptions()));
                        }

                        mapData.put(operand.dataElement().uid(), mapCatOptions);
                    } else {

                        if (mapData.containsKey(operand.dataElement().uid())) {
                            catOptions = UidsHelper.getUidsList(d2.categoryModule().categoryOptionCombos.byUid().eq(operand.categoryOptionCombo().uid()).withCategoryOptions().one().blockingGet().categoryOptions());
                            mapData.get(operand.dataElement().uid()).put(operand.categoryOptionCombo().uid(), catOptions);
                        } else
                            catOptions = UidsHelper.getUidsList(d2.categoryModule().categoryOptionCombos.byUid().eq(operand.categoryOptionCombo().uid()).withCategoryOptions().one().blockingGet().categoryOptions());
                        HashMap<String, List<String>> mapCatOptions = new HashMap<>();
                        mapCatOptions.put(operand.categoryOptionCombo().uid(), catOptions);
                        mapData.put(operand.dataElement().uid(), mapCatOptions);
                    }
                }
            }
            Timber.tag("BREAKPOINT").d("getGreyedFields()");
            return mapData;

        });

    }

    @Override
    public Flowable<Map<String, List<String>>> getMandatoryDataElement() {
        Map<String, List<String>> mapData = new HashMap<>();
        return Flowable.fromCallable(() -> {
            DataSet dataSet = d2.dataSetModule().dataSets.withCompulsoryDataElementOperands().withDataSetElements().byUid().eq(dataSetUid).one().blockingGet();
            for(DataElementOperand operand : dataSet.compulsoryDataElementOperands()){
                List<String> catOptions = UidsHelper.getUidsList(d2.categoryModule().categoryOptionCombos.withCategoryOptions().byUid().eq(operand.categoryOptionCombo().uid()).one().blockingGet().categoryOptions());
                if (mapData.containsKey(operand.dataElement().uid())) {
                    mapData.get(operand.dataElement().uid()).addAll(catOptions);
                } else {
                    mapData.put(operand.dataElement().uid(), catOptions);
                }
            }

            Timber.tag("BREAKPOINT").d("getMandatoryDataElement()");

            return  mapData;
        });

    }

    @Override
    public Flowable<List<DataElementOperand>> getCompulsoryDataElements() {
        return d2.dataSetModule().dataSets.withCompulsoryDataElementOperands().uid(dataSetUid).get()
                .map(DataSet::compulsoryDataElementOperands).toFlowable();
    }

    @Override
    public Flowable<List<DataElementOperand>> getGreyFields(String section) {
        return d2.dataSetModule().sections.withGreyedFields().byDataSetUid().eq(dataSetUid).byName().eq(section).one().get()
                .map(Section::greyedFields).toFlowable();
    }

    @Override
    public Flowable<Section> getSectionByDataSet(String section) {
        if(!section.isEmpty() && !section.equals("NO_SECTION"))
            return Flowable.just(d2.dataSetModule().sections.byDataSetUid().eq(dataSetUid).byName().eq(section).one().blockingGet());
        else
            return Flowable.just(Section.builder().uid("").build());

    }

    @Override
    public Flowable<Boolean> completeDataSet(String orgUnitUid, String periodInitialDate, String catCombo) {
        boolean updateOrInserted;
        String where = "period = ? AND dataSet = ? AND attributeOptionCombo = ?";

        ContentValues contentValues = new ContentValues();
        contentValues.put(DataSetCompleteRegistration.Columns.STATE, State.TO_UPDATE.name());
        String completeDate = DateUtils.databaseDateFormat().format(DateUtils.getInstance().getToday());
        contentValues.put("date", completeDate);
        String[] values = {periodInitialDate, dataSetUid, catCombo};

        updateOrInserted = briteDatabase.update(DataSetCompleteRegistration.class.getSimpleName(), contentValues, where, values) > 0;

        if (!updateOrInserted) {
            DataSetCompleteRegistration dataSetCompleteRegistration =
                    DataSetCompleteRegistration.builder().dataSet(dataSetUid)
                            .period(periodInitialDate)
                            .organisationUnit(orgUnitUid)
                            .attributeOptionCombo(catCombo)
                            .date(DateUtils.getInstance().getToday())
                            .state(State.TO_POST).build();

            updateOrInserted = briteDatabase.insert(DataSetCompleteRegistration.class.getSimpleName(), dataSetCompleteRegistration.toContentValues()) > 0;
        }

        return Flowable.just(updateOrInserted);

    }

    @Override
    public Flowable<Boolean> reopenDataSet(String orgUnitUid, String periodInitialDate, String catCombo) {
        String where = "period = ? AND dataSet = ? AND attributeOptionCombo = ? and organisationUnit = ? ";
        String[] values = {periodInitialDate, dataSetUid, catCombo, orgUnitUid};

        ContentValues contentValues = new ContentValues();
        contentValues.put(DataSetCompleteRegistration.Columns.DELETED, true);
        String completeDate = DateUtils.databaseDateFormat().format(DateUtils.getInstance().getToday());
        contentValues.put("date", completeDate);

        return Flowable.just(briteDatabase.update(DataSetCompleteRegistration.class.getSimpleName(), contentValues, where, values) > 0);
    }

    @Override
    public Flowable<Boolean> isCompleted(String orgUnitUid, String periodInitialDate, String catCombo) {

        return Flowable.fromCallable(() ->{
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
    public Flowable<Boolean> isApproval(String orgUnit, String period, String attributeOptionCombo){
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
        List<String> dataElements = new ArrayList<>();
        if (!sectionName.equals("NO_SECTION"))
           dataElements = UidsHelper.getUidsList(d2.dataSetModule().sections.withDataElements().byDataSetUid().eq(dataSetUid).byName().eq(sectionName).one().blockingGet().dataElements());
        else {
            List<DataSetElement> dataSetElements = d2.dataSetModule().dataSets.withDataSetElements().byUid().eq(dataSetUid).one().blockingGet().dataSetElements();
            for (DataSetElement dataSetElement : dataSetElements)
                dataElements.add(dataSetElement.dataElement().uid());
        }
        return d2.dataElementModule().dataElements
                .byUid().in(dataElements)
                .byCategoryComboUid().eq(categoryCombo.uid())
                .orderByName(RepositoryScope.OrderByDirection.ASC)
                .get().toFlowable();
    }

}
