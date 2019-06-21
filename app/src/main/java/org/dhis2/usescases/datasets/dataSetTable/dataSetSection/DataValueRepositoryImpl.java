package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import android.content.ContentValues;
import android.util.Log;

import com.google.common.collect.Lists;
import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryModel;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionCombo;
import org.hisp.dhis.android.core.category.CategoryOptionComboCategoryOptionLinkModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionModel;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.dataelement.DataElementModel;
import org.hisp.dhis.android.core.dataset.DataInputPeriod;
import org.hisp.dhis.android.core.dataset.DataInputPeriodModel;
import org.hisp.dhis.android.core.dataset.DataSet;
import org.hisp.dhis.android.core.dataset.DataSetCompleteRegistration;
import org.hisp.dhis.android.core.dataset.DataSetDataElementLinkModel;
import org.hisp.dhis.android.core.dataset.DataSetElement;
import org.hisp.dhis.android.core.dataset.DataSetModel;
import org.hisp.dhis.android.core.dataset.Section;
import org.hisp.dhis.android.core.dataset.SectionGreyedFieldsLinkModel;
import org.hisp.dhis.android.core.dataset.SectionModel;
import org.hisp.dhis.android.core.datavalue.DataValue;
import org.hisp.dhis.android.core.datavalue.DataValueModel;
import org.hisp.dhis.android.core.period.Period;
import org.hisp.dhis.android.core.period.PeriodModel;
import org.hisp.dhis.android.core.period.PeriodType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;

public class DataValueRepositoryImpl implements DataValueRepository {

    private final D2 d2;
    private BriteDatabase briteDatabase;
    private String dataSetUid;

    private final String DATA_ELEMENTS = "SELECT " +
            "DataElement.*, " +
            "DataSetDataElementLink.categoryCombo as CategoryComboOverride " +
            "FROM DataElement " +
            "JOIN DataSetDataElementLink ON " +
            "DataSetDataElementLink.dataElement = DataElement.uid " +
            "LEFT JOIN SectionDataElementLink ON SectionDataElementLink.dataElement = DataElement.uid " +
            "LEFT JOIN Section ON Section.uid = SectionDataElementLink.section " +
            "LEFT JOIN Section s2 ON s2.dataSet = DataSetDataElementLink.dataSet ";


    private final String CAT_COMBO = "SELECT " +
            "   CategoryCombo.*, " +
            "   DataSetDataElementLink.categoryCombo " +
            " FROM DataSetDataElementLink " +
            " JOIN DataElement ON DataElement.uid = DataSetDataElementLink.dataElement " +
            " JOIN CategoryCombo ON CategoryCombo.uid = case when dataSetDataElementLink.categoryCombo IS NOT NULL then dataSetDataElementLink.categoryCombo else dataElement.categoryCombo end " +
            " LEFT JOIN SectionDataElementLink ON SectionDataElementLink.dataElement = DataElement.uid " +
            " LEFT JOIN Section ON Section.uid = SectionDataElementLink.section " +
            " WHERE DataSetDataElementLink.dataSet = ?  ";

    private final String CATEGORY_OPTION = "SELECT CategoryOption.*, Category.uid AS category, section.displayName as SectionName, CategoryCombo.uid as catCombo,CategoryCategoryComboLink.sortOrder as sortOrder " +
            "FROM DataSetDataElementLink " +
            "JOIN DataElement ON DataElement.uid = DataSetDataElementLink.dataElement " +
            "JOIN CategoryCombo ON CategoryCombo.uid = case when dataSetDataElementLink.categoryCombo IS NOT NULL then dataSetDataElementLink.categoryCombo else dataElement.categoryCombo end  " +
            "JOIN CategoryCategoryComboLink ON CategoryCategoryComboLink.CategoryCombo = CategoryCombo.uid " +
            "JOIN Category ON Category.uid = CategoryCategoryComboLink.category " +
            "JOIN CategoryCategoryOptionLink ON CategoryCategoryOptionLink.category = Category.uid " +
            "JOIN CategoryOption ON CategoryOption.uid = CategoryCategoryOptionLink.categoryOption " +
            "LEFT JOIN ( " +
            "SELECT Section.dataSet as sectionDataSet, section.displayName, Section.name, Section.uid, SectionDataElementLink.dataElement " +
            "FROM Section JOIN SectionDataElementLink ON SectionDataElementLink.section = Section.uid ) " +
            "AS section ON section.sectionDataSet = DataSetDataElementLink.dataSet " +
            "WHERE DataSetDataElementLink.dataSet = ? ";


    private final String CATEGORY_OPTION_COMBO = "SELECT CategoryOptionCombo.*,section.displayName as SectionName " +
            " FROM DataSetDataElementLink " +
            " JOIN DataElement ON DataElement.uid = DataSetDataElementLink.dataElement " +
            " JOIN CategoryOptionCombo ON CategoryOptionCombo.categoryCombo = case when dataSetDataElementLink.categoryCombo IS NOT NULL then dataSetDataElementLink.categoryCombo else dataElement.categoryCombo end " +
            " JOIN CategoryCategoryComboLink ON CategoryCategoryComboLink.categoryCombo = categoryOptionCombo.categoryCombo " +
            " JOIN CategoryOptionComboCategoryOptionLink ON CategoryOptionComboCategoryOptionLink.categoryOptionCombo = CategoryOptionCombo.uid " +
            " JOIN CategoryCategoryOptionLink ON CategoryCategoryOptionLink.categoryOption = CategoryOptionComboCategoryOptionLink.categoryOption " +
            " LEFT JOIN (SELECT section.displayName, section.uid, SectionDataElementLINK.dataElement as dataelement FROM Section " +
            " JOIN SectionDataElementLINK ON SectionDataElementLink.section = Section.uid) as section on section.dataelement = DataElement.uid " +
            " WHERE DataSetDataElementLink.dataSet = ? " +
            " GROUP BY section.uid, CategoryOptionCombo.uid  ORDER BY section.uid, CategoryCategoryComboLink.sortOrder, CategoryCategoryOptionLink.sortOrder";


    private final String DATA_SET = "SELECT DataSet.* FROM DataSet WHERE DataSet.uid = ?";

    private final String SECTION_GREYED_FIELDS = "select Section.name as section, DataElement.uid as dataElement, CategoryOptionCombo.uid as CatOptionCombo, CategoryOptionComboCategoryOptionLink.categoryOption as categoryOption " +
            "from SectionGreyedFieldsLink " +
            "join DataElementOperand on SectionGreyedFieldsLink.dataElementOperand = DataElementOperand.uid " +
            "join DataElement on DataElement.uid = DataElementOperand.dataElement " +
            "join CategoryOptionCombo on CategoryOptionCombo.uid = DataElementOperand.categoryOptionCombo " +
            "join CategoryOptionComboCategoryOptionLink on CategoryOptionComboCategoryOptionLink.categoryOptionCombo = CategoryOptionCombo.uid " +
            "join Section on Section.uid = SectionGreyedFieldsLink.section " +
            "where CategoryOptionComboCategoryOptionLink.categoryOptionCombo in (?) ";

    private static final String GET_COMPULSORY_DATA_ELEMENT = "select DataElementOperand.dataElement as dataElement, CategoryOptionComboCategoryOptionLink.categoryOption as categoryOption " +
            "from DataSetCompulsoryDataElementOperandsLink " +
            "JOIN DataElementOperand ON DataElementOperand.uid = DataSetCompulsoryDataElementOperandsLink.dataElementOperand " +
            "JOIN CategoryOptionCombo on CategoryOptionCombo.uid = DataElementOperand.categoryOptionCombo " +
            "JOIN CategoryOptionComboCategoryOptionLink on CategoryOptionComboCategoryOptionLink.categoryOptionCombo = CategoryOptionCombo.uid " +
            "where CategoryOptionComboCategoryOptionLink.categoryOptionCombo in (?) " +
            "GROUP BY dataElement, categoryOption";

    private static final String SECTION_TOTAL_ROW_COLUMN = "SELECT Section.* " +
            "FROM Section " +
            "JOIN DataSet ON DataSet.uid = Section.dataSet " +
            "WHERE DataSet.uid = ? " +
            "AND Section.name = ?";

    private static final String SELECT_CATEGORY_OPTION_COMBO = "SELECT CategoryOptionCombo.uid as categoryOptionCombo, CategoryOptionComboCategoryOptionLink.categoryOption as categoryOption " +
            " FROM DataSetDataElementLink " +
            " JOIN DataElement ON DataElement.uid = DataSetDataElementLink.dataElement " +
            "            JOIN CategoryOptionCombo ON CategoryOptionCombo.categoryCombo = case when dataSetDataElementLink.categoryCombo IS NOT NULL then dataSetDataElementLink.categoryCombo else dataElement.categoryCombo end\n" +
            "            JOIN CategoryCategoryComboLink ON CategoryCategoryComboLink.categoryCombo = categoryOptionCombo.categoryCombo " +
            "            JOIN CategoryOptionComboCategoryOptionLink ON CategoryOptionComboCategoryOptionLink.categoryOptionCombo = CategoryOptionCombo.uid " +
            "            JOIN CategoryCategoryOptionLink ON CategoryCategoryOptionLink.categoryOption = CategoryOptionComboCategoryOptionLink.categoryOption " +
            " WHERE DataSetDataElementLink.dataSet = ? " +
            " GROUP BY CategoryOptionCombo.uid, CategoryOptionComboCategoryOptionLink.categoryOption " +
            " ORDER BY CategoryCategoryComboLink.sortOrder, CategoryCategoryOptionLink.sortOrder";

    private static final String SELECT_PERIOD = "SELECT * FROM Period WHERE periodId = ?";

    private static final String SELECT_DATA_INPUT_PERIOD = "SELECT * FROM DataInputPeriod WHERE dataset = ?";/* AND period = ?";*/

    private static final String SELECT_COMPLETE_DATASET = "SELECT * FROM DataSetCompleteRegistration WHERE period = ? AND dataSet = ? AND attributeOptionCombo = ? and organisationUnit = ? " +
            "AND state in ('TO_UPDATE', 'SYNCED', 'TO_POST')";

    public DataValueRepositoryImpl(D2 d2, BriteDatabase briteDatabase, String dataSetUid) {
        this.d2 = d2;
        this.briteDatabase = briteDatabase;
        this.dataSetUid = dataSetUid;
    }

    @Override
    public Flowable<Period> getPeriod(String periodId) {
        return Flowable.fromCallable(() -> d2.periodModule().periods.byPeriodId().eq(periodId).one().get());
    }//d2.dataSetModule().dataSets.withDataSetElements().byUid().eq(dataSetUid).one().get().dataSetElements()

    @Override
    public Flowable<List<DataInputPeriod>> getDataInputPeriod() {
        return Flowable.fromCallable(()-> d2.dataSetModule().dataSets.withDataInputPeriods().byUid().eq(dataSetUid).one().get().dataInputPeriods());
    }

    @Override
    public Flowable<Map<String, List<String>>> getCategoryOptionComboCatOption() {
        Map<String, List<String>> map = new HashMap<>();
        return briteDatabase.createQuery(CategoryOptionComboCategoryOptionLinkModel.TABLE, SELECT_CATEGORY_OPTION_COMBO, dataSetUid)
                .mapToList(cursor -> {
                    String categoryOptionCombo = cursor.getString(cursor.getColumnIndex("categoryOptionCombo"));

                    if (map.get(categoryOptionCombo) == null) {
                        map.put(categoryOptionCombo, new ArrayList<>());
                    }
                    map.get(categoryOptionCombo).add(cursor.getString(cursor.getColumnIndex("categoryOption")));

                    return categoryOptionCombo;
                })
                .flatMap(dataElementModels -> Observable.just(map)).toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Flowable<List<DataElement>> getDataElements(String sectionName) {
        if (!sectionName.equals("NO_SECTION"))
            return Flowable.just(d2.dataSetModule().sections.withDataElements().byDataSetUid().eq(dataSetUid).byName().eq(sectionName).one().get())
                    .flatMapIterable(section -> section.dataElements())
                    .flatMap(dataElement ->
                            Flowable.just(d2.dataSetModule().dataSets.withDataSetElements().byUid().eq(dataSetUid).one().get().dataSetElements())
                                    .map(dataElementOverrides -> transformDataElement(dataElement, dataElementOverrides))).toList().toFlowable();

        return Flowable.just(d2.dataSetModule().dataSets.withDataSetElements().byUid().eq(dataSetUid).one().get())
                .flatMapIterable(dataSet -> {
                    List<String> uids = new ArrayList<>();
                    for(DataSetElement dataSetElement: dataSet.dataSetElements())
                        uids.add(dataSetElement.dataElement().uid());

                    return d2.dataElementModule().dataElements.byUid().in(uids).get();
                })
                .flatMap(dataElement ->
                        Flowable.just(d2.dataSetModule().dataSets.withDataSetElements().byUid().eq(dataSetUid).one().get().dataSetElements())
                                .map(dataElementOverrides ->  transformDataElement(dataElement, dataElementOverrides))).toList().toFlowable();

    }

    public Flowable<List<CategoryCombo>> getCatCombo(String sectionName){
        if (!sectionName.equals("NO_SECTION"))
            return Flowable.just(d2.dataSetModule().sections.withDataElements().byDataSetUid().eq(dataSetUid).byName().eq(sectionName).one().get())
                    .flatMapIterable(section -> section.dataElements())
                    .flatMap(dataElement ->
                            Flowable.just(d2.dataSetModule().dataSets.withDataSetElements().byUid().eq(dataSetUid).one().get().dataSetElements())
                                    .map(dataElementOverrides -> {
                                        for(DataSetElement dataSetElement: dataElementOverrides)
                                            if(dataSetElement.dataElement().uid().equals(dataElement.uid()) && dataSetElement.categoryCombo() != null)
                                                return d2.categoryModule().categoryCombos.byUid().eq(dataSetElement.categoryCombo().uid()).one().get();

                                        return d2.categoryModule().categoryCombos.byUid().eq(dataElement.categoryCombo().uid()).one().withAllChildren().get();
                                    })).toList().toFlowable();


        return Flowable.just(d2.dataSetModule().dataSets.byUid().eq(dataSetUid).withDataSetElements().one().get())
                .flatMapIterable(dataSet -> dataSet.dataSetElements())
                .map(dataSetElement ->{
                    if(dataSetElement.categoryCombo() != null)
                        return d2.categoryModule().categoryCombos.byUid().eq(dataSetElement.categoryCombo().uid()).one().withAllChildren().get();

                    DataElement dataElement = d2.dataElementModule().dataElements.byUid().eq(dataSetElement.dataElement().uid()).one().get();

                    return d2.categoryModule().categoryCombos.byUid().eq(dataElement.categoryCombo().uid()).one().withAllChildren().get();
                })
                .toList().toFlowable();
    }

    @Override
    public Flowable<DataSet> getDataSet() {

        return Flowable.fromCallable(() -> d2.dataSetModule().dataSets.byUid().eq(dataSetUid).one().get());

    }

    @Override
    public Flowable<Long> insertDataValue(DataValueModel dataValue) {
        return Flowable.just(briteDatabase.insert(DataValueModel.TABLE, dataValue.toContentValues()));
    }

    public Flowable<Integer> updateValue(DataValueModel dataValue){
        String where = DataValueModel.Columns.DATA_ELEMENT + " = '" + dataValue.dataElement() + "' AND " + DataValueModel.Columns.PERIOD + " = '" + dataValue.period() +
                "' AND " + DataValueModel.Columns.ORGANISATION_UNIT + " = '" + dataValue.organisationUnit() +
                "' AND " + DataValueModel.Columns.ATTRIBUTE_OPTION_COMBO + " = '" + dataValue.attributeOptionCombo() +
                "' AND " + DataValueModel.Columns.CATEGORY_OPTION_COMBO + " = '" + dataValue.categoryOptionCombo() + "'";

        if(dataValue.value()!=null && !dataValue.value().isEmpty()) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DataValueModel.Columns.VALUE, dataValue.value());
            contentValues.put(DataValueModel.Columns.STATE, dataValue.state().name());
            contentValues.put(DataValueModel.Columns.LAST_UPDATED, DateUtils.databaseDateFormat().format(dataValue.lastUpdated()));

            return Flowable.just(briteDatabase.update(DataValueModel.TABLE, contentValues, where));
        }
        else
            return Flowable.just(briteDatabase.delete(DataValueModel.TABLE, where));

    }
    /**
     * SELECT CategoryOptionCombo.*,section.displayName as SectionName " +
     *             " FROM DataSetDataElementLink " +
     *             " JOIN DataElement ON DataElement.uid = DataSetDataElementLink.dataElement " +
     *             " JOIN CategoryOptionCombo ON CategoryOptionCombo.categoryCombo = case when dataSetDataElementLink.categoryCombo IS NOT NULL then dataSetDataElementLink.categoryCombo else dataElement.categoryCombo end " +
     *             " JOIN CategoryCategoryComboLink ON CategoryCategoryComboLink.categoryCombo = categoryOptionCombo.categoryCombo " +
     *             " JOIN CategoryOptionComboCategoryOptionLink ON CategoryOptionComboCategoryOptionLink.categoryOptionCombo = CategoryOptionCombo.uid " +
     *             " JOIN CategoryCategoryOptionLink ON CategoryCategoryOptionLink.categoryOption = CategoryOptionComboCategoryOptionLink.categoryOption " +
     *             " LEFT JOIN (SELECT section.displayName, section.uid, SectionDataElementLINK.dataElement as dataelement FROM Section " +
     *             " JOIN SectionDataElementLINK ON SectionDataElementLink.section = Section.uid) as section on section.dataelement = DataElement.uid " +
     *             " WHERE DataSetDataElementLink.dataSet = ? " +
     *             " GROUP BY section.uid, CategoryOptionCombo.uid  ORDER BY section.uid, CategoryCategoryComboLink.sortOrder, CategoryCategoryOptionLink.sortOrder
     * */
    @Override
    public Flowable<Map<String, List<CategoryOptionCombo>>> getCatOptionCombo() {
        Map<String, List<CategoryOptionCombo>> map = new HashMap<>();
        List<String> sections = new ArrayList<>();
        return Flowable.fromIterable(d2.dataSetModule().sections.withDataElements().byDataSetUid().eq(dataSetUid).get())
                .flatMap(section -> {
                    if (!sections.contains(section.name())) {
                        sections.add(section.name());
                    }

                    List<DataElement> dataElements = section.dataElements();
                    List<DataElement> dataElementOverrides = new ArrayList<>();

                    List<DataSetElement> overrides = d2.dataSetModule().dataSets.withDataSetElements().byUid().eq(dataSetUid).one().get().dataSetElements();
                    for (DataElement dataElement : dataElements) {
                        dataElementOverrides.add(transformDataElement(dataElement, overrides));
                    }

                    if (map.get(section.name()) == null) {
                        map.put(section.name(), new ArrayList<>());
                    }

                    List<CategoryOptionCombo> addCatOptionCombos = new ArrayList<>();
                    for (DataElement dataElement : dataElementOverrides) {
                        boolean exist = false;
                        List<CategoryOptionCombo> listCatOption = d2.categoryModule().categoryOptionCombos.byCategoryComboUid().eq(dataElement.categoryCombo().uid()).get();
                        for (CategoryOptionCombo catOptionCombo : listCatOption){
                            for (CategoryOptionCombo catOptionComboMap : map.get(section.name())) {
                                if (catOptionComboMap.uid().equals(catOptionCombo.uid()))
                                    exist = true;
                            }

                            if(!exist)
                                map.get(section.name()).add(catOptionCombo);
                        }

                    }

                    //map.get(section.name()).addAll(addCatOptionCombos);

                    return Flowable.just(map);
                });

        /*return briteDatabase.createQuery(CategoryOptionModel.TABLE, CATEGORY_OPTION_COMBO, dataSetUid)
                .mapToList(cursor -> {
                    CategoryOptionComboModel catOptionCombo = CategoryOptionComboModel.create(cursor);
                    String sectionName = cursor.getString(cursor.getColumnIndex("SectionName"));
                    if (sectionName == null)
                        sectionName = "NO_SECTION";
                    if (map.get(sectionName) == null) {
                        map.put(sectionName, new ArrayList<>());
                    }

                    map.get(sectionName).add(catOptionCombo);

                    return catOptionCombo;
                }).flatMap(categoryOptionComboModels -> Observable.just(map)).toFlowable(BackpressureStrategy.LATEST);*/
    }

    @Override
    public Flowable<Map<String, List<List<Pair<CategoryOption, Category>>>>> getCatOptions(String sectionName) {
        Map<String, List<List<Pair<CategoryOption, Category>>>> map = new HashMap<>();
        List<String> catCombos = new ArrayList<>();
        //TODO set NO_SECTION is not implementing
        return Flowable.just(d2.dataSetModule().sections.withDataElements().byDataSetUid().eq(dataSetUid).byName().eq(sectionName).one().get())
                .flatMapIterable(section -> section.dataElements())
                .flatMap(dataElement ->
                        Flowable.just(d2.dataSetModule().dataSets.withDataSetElements().byUid().eq(dataSetUid).one().get().dataSetElements())
                                .map(dataElementOverrides -> transformDataElement(dataElement, dataElementOverrides)))
                .flatMap(dataElement-> {
                    String catCombo = dataElement.categoryComboUid();
                    if(!catCombos.contains(dataElement.categoryComboUid())) {
                        catCombos.add(dataElement.categoryComboUid());

                        List<Category> categories = d2.categoryModule().categoryCombos.withCategories().withAllChildren().byUid().eq(catCombo).one().get().categories();

                        if (map.get(catCombo) == null) {
                            map.put(catCombo, new ArrayList<>());
                        }

                        for (Category category : categories) {
                            List<CategoryOption> catOptions = d2.categoryModule().categories.withCategoryOptions().byUid().eq(category.uid()).one().get().categoryOptions();
                            for (CategoryOption catOption : catOptions) {
                                if (map.get(catCombo).size() == 0) {
                                    List<Pair<CategoryOption, Category>> list = new ArrayList<>();
                                    list.add(Pair.create(catOption, category));
                                    map.get(catCombo).add(list);
                                } else {

                                    if (map.get(catCombo).get(map.get(catCombo).size() - 1).get(0).val1().uid().equals(category.uid())) {
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

                    return Flowable.just(map);
                });
    }

    private DataElement transformDataElement(DataElement dataElement, List<DataSetElement> override){
        for(DataSetElement dataSetElement: override)
            if(dataSetElement.dataElement().uid().equals(dataElement.uid()) && dataSetElement.categoryCombo() != null)
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

        return Flowable.just(d2.dataSetModule().dataSets.withSections().withDataSetElements().byUid().eq(dataSetUid).one().get())
                .flatMapIterable(dataSet ->{
                    List<DataSetElement> dataElements = new ArrayList<>();
                    if(!sectionName.equals("NO_SECTION")) {
                        List<DataElement> dataElementSection = d2.dataSetModule().sections.withDataElements().byName().eq(sectionName).one().get().dataElements();
                        for(DataElement dataElement: dataElementSection){
                            for(DataSetElement dataSetElement: dataSet.dataSetElements())
                                if(dataSetElement.dataElement().uid().equals(dataElement.uid()))
                                    dataElements.add(dataSetElement);
                        }
                    }else
                        dataElements = dataSet.dataSetElements();

                    return dataElements;
                })
                .flatMapIterable(dataSetElement ->{
                    if(dataSetElement.categoryCombo() != null)
                        mapDataElementCatCombo.put(dataSetElement.dataElement().uid(), dataSetElement.categoryCombo().uid());
                    else
                        mapDataElementCatCombo.put(dataSetElement.dataElement().uid(),
                                d2.dataElementModule().dataElements.byUid().eq(dataSetElement.dataElement().uid()).one().get().categoryCombo().uid());

                    return d2.dataValueModule().dataValues.byDataElementUid().eq(dataSetElement.dataElement().uid())
                            .byAttributeOptionComboUid().eq(catOptionComb)
                            .byPeriod().eq(initPeriodType)
                            .byOrganisationUnitUid().eq(orgUnitUid)
                            .get();}
                ).map(dataValue -> {
                    List<CategoryOption> categoryOptions = d2.categoryModule().categoryOptionCombos.withCategoryOptions()
                            .byUid().eq(dataValue.categoryOptionCombo()).one().get().categoryOptions();
                    List<String> uidCatOptions = new ArrayList<>();
                    for(CategoryOption catOption: categoryOptions)
                        uidCatOptions.add(catOption.uid());

                    return DataSetTableModel.create(dataValue.id(), dataValue.dataElement(), dataValue.period(),
                            dataValue.organisationUnit(), dataValue.categoryOptionCombo(), dataValue.attributeOptionCombo(),
                            dataValue.value(), dataValue.storedBy(), "",//no used anywhere, remove this field
                            uidCatOptions, mapDataElementCatCombo.get(dataValue.dataElement()));

                }).toList().toFlowable();
    }

    @Override
    public Flowable<Map<String, Map<String, List<String>>>> getGreyedFields(List<String> categoryOptionCombos, String section) {

        Map<String, Map<String, List<String>>> mapData = new HashMap<>();

        String query = SECTION_GREYED_FIELDS.replace("?", categoryOptionCombos.toString().substring(1, categoryOptionCombos.toString().length() - 1));
        if (!section.isEmpty() && !section.equals("NO_SECTION"))
            query = query + "and Section.name = '" + section + "' ";

        query = query + "GROUP BY section, dataElement,CatOptionCombo, categoryOption";
        return briteDatabase.createQuery(SectionGreyedFieldsLinkModel.TABLE, query)
                .mapToList(cursor -> {
                    String dataElement = cursor.getString(cursor.getColumnIndex("dataElement"));
                    String catOptionCombo = cursor.getString(cursor.getColumnIndex("CatOptionCombo"));
                    String catOption = cursor.getString(cursor.getColumnIndex("categoryOption"));

                    if (mapData.containsKey(dataElement)) {

                        if (mapData.get(dataElement).get(catOptionCombo) != null) {

                            mapData.get(dataElement).get(catOptionCombo)
                                    .add(catOption);
                        } else {
                            List<String> options = new ArrayList<>();
                            options.add(catOption);
                            mapData.get(dataElement).put(catOptionCombo, options);
                        }
                    } else {
                        Map<String, List<String>> mapOptions = new HashMap<>();
                        List<String> options = new ArrayList<>();
                        options.add(catOption);
                        mapOptions.put(catOptionCombo, options);
                        mapData.put(dataElement, mapOptions);

                    }

                    return mapData;
                }).map(data -> mapData).toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Flowable<Map<String, List<String>>> getMandatoryDataElement(List<String> categoryOptionCombo) {
        Map<String, List<String>> mapData = new HashMap<>();

        String query = GET_COMPULSORY_DATA_ELEMENT.replace("?", categoryOptionCombo.toString().substring(1, categoryOptionCombo.toString().length() - 1));
        return briteDatabase.createQuery(SectionGreyedFieldsLinkModel.TABLE, query)
                .mapToList(cursor -> {
                    if (mapData.containsKey(cursor.getString(0))) {
                        mapData.get(cursor.getString(0)).add(cursor.getString(1));
                    } else {
                        List<String> listCatOp = new ArrayList<>();
                        listCatOp.add(cursor.getString(1));
                        mapData.put(cursor.getString(0), listCatOp);
                    }

                    return mapData;
                }).map(data -> mapData).toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Flowable<SectionModel> getSectionByDataSet(String section) {
        return briteDatabase.createQuery(SectionModel.TABLE, SECTION_TOTAL_ROW_COLUMN, dataSetUid, section)
                .mapToOneOrDefault(SectionModel::create, SectionModel.builder().build()).toFlowable(BackpressureStrategy.LATEST);
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
        contentValues.put(DataSetCompleteRegistration.Columns.STATE, State.TO_DELETE.name());
        String completeDate = DateUtils.databaseDateFormat().format(DateUtils.getInstance().getToday());
        contentValues.put("date", completeDate);

        return Flowable.just(briteDatabase.update(DataSetCompleteRegistration.class.getSimpleName(), contentValues, where, values) > 0);
    }

    @Override
    public Flowable<Boolean> isCompleted(String orgUnitUid, String periodInitialDate, String catCombo) {
        return briteDatabase.createQuery(DataSetCompleteRegistration.class.getSimpleName(), SELECT_COMPLETE_DATASET, periodInitialDate, dataSetUid, catCombo, orgUnitUid)
                .mapToOneOrDefault(data -> true, false).toFlowable(BackpressureStrategy.LATEST);
    }
}
