package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import android.content.ContentValues;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboCategoryOptionLinkModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionModel;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.common.ValueType;
import org.hisp.dhis.android.core.dataelement.DataElementModel;
import org.hisp.dhis.android.core.dataset.DataInputPeriodModel;
import org.hisp.dhis.android.core.dataset.DataSetCompleteRegistration;
import org.hisp.dhis.android.core.dataset.DataSetDataElementLinkModel;
import org.hisp.dhis.android.core.dataset.DataSetModel;
import org.hisp.dhis.android.core.dataset.SectionGreyedFieldsLinkModel;
import org.hisp.dhis.android.core.dataset.SectionModel;
import org.hisp.dhis.android.core.datavalue.DataValueModel;
import org.hisp.dhis.android.core.period.PeriodModel;
import org.hisp.dhis.android.core.period.PeriodType;

import java.util.ArrayList;
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
            "LEFT JOIN Section s2 ON s2.dataSet = DataSetDataElementLink.dataSet " ;


    private final String CAT_COMBO = "SELECT " +
            "   CategoryCombo.*, " +
            "   DataSetDataElementLink.categoryCombo " +
            " FROM DataSetDataElementLink " +
            " JOIN DataElement ON DataElement.uid = DataSetDataElementLink.dataElement " +
            " JOIN CategoryCombo ON CategoryCombo.uid = case when dataSetDataElementLink.categoryCombo IS NOT NULL then dataSetDataElementLink.categoryCombo else dataElement.categoryCombo end " +
            " LEFT JOIN SectionDataElementLink ON SectionDataElementLink.dataElement = DataElement.uid " +
            " LEFT JOIN Section ON Section.uid = SectionDataElementLink.section " +
            " WHERE DataSetDataElementLink.dataSet = ?  ";

    private final String DATA_VALUES = "SELECT DataValue.*, CategoryOptionComboCategoryOptionLink.categoryOption as catOption, " +
            "DataElement.categoryCombo as catComboDataElement, DataSetDataElementLink.categoryCombo as catComboLink FROM DataValue " +
            "JOIN CategoryOptionComboCategoryOptionLink ON CategoryOptionComboCategoryOptionLink.categoryOptionCombo = DataValue.categoryOptionCombo " +
            "JOIN DataSetDataElementLink ON DataSetDataElementLink.dataElement = DataValue.dataElement " +
            "JOIN DataElement ON DataElement.uid = DataSetDataElementLink.dataElement " +
            "LEFT JOIN Section ON Section.dataSet = DataSetDataElementLink.dataSet " +
            "WHERE DataValue.organisationUnit = ? " +
            "AND DataValue.attributeOptionCombo = ? " +
            "AND DataSetDataElementLink.dataSet = ? " +
            "AND DataValue.period = ? ";

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
            "where CategoryOptionComboCategoryOptionLink.categoryOptionCombo in (?) " ;

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
    public Flowable<PeriodModel> getPeriod(String periodId) {
        return briteDatabase.createQuery(PeriodModel.TABLE, SELECT_PERIOD, periodId)
                .mapToOne(PeriodModel::create)
                .toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Flowable<List<DataInputPeriodModel>> getDataInputPeriod() {
        return briteDatabase.createQuery(DataInputPeriodModel.TABLE, SELECT_DATA_INPUT_PERIOD, dataSetUid/*, periodId*/)
                .mapToList(DataInputPeriodModel::create)
                .toFlowable(BackpressureStrategy.LATEST);
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
    public Flowable<List<DataElementModel>> getDataElements(String section) {
        String query = DATA_ELEMENTS;
        if (!section.equals("NO_SECTION")) {
            query = query + "WHERE Section.dataSet = ? AND Section.name = ? and DataSetDataElementLink.dataSet = ? GROUP BY DataElement.uid ";
            query = query + " ORDER BY SectionDataElementLink.sortOrder ";
            return briteDatabase.createQuery(DataElementModel.TABLE, query, dataSetUid, section, dataSetUid)
                    .mapToList(cursor -> {
                        String catComboOverride = cursor.getString(cursor.getColumnIndex("CategoryComboOverride"));

                        DataElementModel dataElementModel = DataElementModel.builder()
                                .uid(cursor.getString(cursor.getColumnIndex(DataElementModel.Columns.UID)))
                                .code(cursor.getString(cursor.getColumnIndex(DataElementModel.Columns.CODE)))
                                .name(cursor.getString(cursor.getColumnIndex(DataElementModel.Columns.NAME)))
                                .displayName(cursor.getString(cursor.getColumnIndex(DataElementModel.Columns.DISPLAY_NAME)))
                                .shortName(cursor.getString(cursor.getColumnIndex(DataElementModel.Columns.SHORT_NAME)))
                                .displayShortName(cursor.getString(cursor.getColumnIndex(DataElementModel.Columns.DISPLAY_SHORT_NAME)))
                                .description(cursor.getString(cursor.getColumnIndex(DataElementModel.Columns.DESCRIPTION)))
                                .displayDescription(cursor.getString(cursor.getColumnIndex(DataElementModel.Columns.DISPLAY_DESCRIPTION)))
                                .valueType(ValueType.valueOf(cursor.getString(cursor.getColumnIndex(DataElementModel.Columns.VALUE_TYPE))))
                                .zeroIsSignificant(cursor.getInt(cursor.getColumnIndex(DataElementModel.Columns.ZERO_IS_SIGNIFICANT)) == 0 ? false : true)
                                .aggregationType(cursor.getString(cursor.getColumnIndex(DataElementModel.Columns.AGGREGATION_TYPE)))
                                .formName(cursor.getString(cursor.getColumnIndex(DataElementModel.Columns.FORM_NAME)))
                                .domainType(cursor.getString(cursor.getColumnIndex(DataElementModel.Columns.DOMAIN_TYPE)))
                                .displayFormName(cursor.getString(cursor.getColumnIndex(DataElementModel.Columns.DISPLAY_FORM_NAME)))
                                .optionSet(cursor.getString(cursor.getColumnIndex(DataElementModel.Columns.OPTION_SET)))
                                .categoryCombo(catComboOverride != null && !catComboOverride.isEmpty() ? catComboOverride : cursor.getString(cursor.getColumnIndex(DataElementModel.Columns.CATEGORY_COMBO)))
                                .build();

                        return dataElementModel;
                    }).toFlowable(BackpressureStrategy.LATEST);
        }
        query = query + "WHERE DataSetDataElementLink.dataSet = ? GROUP BY DataElement.uid  ORDER BY SectionDataElementLink.sortOrder";
        return briteDatabase.createQuery(DataElementModel.TABLE, query, dataSetUid)
                .mapToList(DataElementModel::create).toFlowable(BackpressureStrategy.LATEST);
    }

    public Flowable<List<CategoryCombo>> getCatCombo(String section){

        String query = CAT_COMBO;
        if (!section.equals("NO_SECTION")) {
            query = query + " AND Section.name = ? ";
            query = query + " GROUP BY CategoryCombo.uid ORDER BY SectionDataElementLink.sortOrder";
            return briteDatabase.createQuery(DataElementModel.TABLE, query, dataSetUid, section)
                    .mapToList(CategoryCombo::create).toFlowable(BackpressureStrategy.LATEST);
        }

        query = query + "GROUP BY CategoryCombo.uid ORDER BY SectionDataElementLink.sortOrder";
        return briteDatabase.createQuery(DataElementModel.TABLE, query, dataSetUid)
                .mapToList(CategoryCombo::create).toFlowable(BackpressureStrategy.LATEST);
    }
    @Override
    public Flowable<DataSetModel> getDataSet() {
        return briteDatabase.createQuery(DataSetModel.TABLE, DATA_SET, dataSetUid)
                .mapToOne(cursor -> DataSetModel.builder()
                        .uid(cursor.getString(cursor.getColumnIndex(DataSetModel.Columns.UID)))
                        .code(cursor.getString(cursor.getColumnIndex(DataSetModel.Columns.CODE)))
                        .name(cursor.getString(cursor.getColumnIndex(DataSetModel.Columns.NAME)))
                        .displayName(cursor.getString(cursor.getColumnIndex(DataSetModel.Columns.DISPLAY_NAME)))
                        .created(DateUtils.databaseDateFormat().parse(cursor.getString(cursor.getColumnIndex(DataSetModel.Columns.CREATED))))
                        .lastUpdated(DateUtils.databaseDateFormat().parse(cursor.getString(cursor.getColumnIndex(DataSetModel.Columns.LAST_UPDATED))))
                        .shortName(cursor.getString(cursor.getColumnIndex(DataSetModel.Columns.SHORT_NAME)))
                        .displayShortName(cursor.getString(cursor.getColumnIndex(DataSetModel.Columns.DISPLAY_SHORT_NAME)))
                        .description(cursor.getString(cursor.getColumnIndex(DataSetModel.Columns.DESCRIPTION)))
                        .displayDescription(cursor.getString(cursor.getColumnIndex(DataSetModel.Columns.DISPLAY_DESCRIPTION)))
                        .periodType(PeriodType.valueOf(cursor.getString(cursor.getColumnIndex(DataSetModel.Columns.PERIOD_TYPE))))
                        .categoryCombo(cursor.getString(cursor.getColumnIndex(DataSetModel.Columns.CATEGORY_COMBO)))
                        .mobile(cursor.getInt(cursor.getColumnIndex(DataSetModel.Columns.MOBILE)) == 1)
                        .version(cursor.getInt(cursor.getColumnIndex(DataSetModel.Columns.VERSION)))
                        .expiryDays(cursor.getInt(cursor.getColumnIndex(DataSetModel.Columns.EXPIRY_DAYS)))
                        .timelyDays(cursor.getInt(cursor.getColumnIndex(DataSetModel.Columns.TIMELY_DAYS)))
                        .notifyCompletingUser(cursor.getInt(cursor.getColumnIndex(DataSetModel.Columns.NOTIFY_COMPLETING_USER)) == 1)
                        .openFuturePeriods(cursor.getInt(cursor.getColumnIndex(DataSetModel.Columns.OPEN_FUTURE_PERIODS)))
                        .fieldCombinationRequired(cursor.getInt(cursor.getColumnIndex(DataSetModel.Columns.FIELD_COMBINATION_REQUIRED)) == 1)
                        .validCompleteOnly(cursor.getInt(cursor.getColumnIndex(DataSetModel.Columns.VALID_COMPLETE_ONLY)) == 1)
                        .noValueRequiresComment(cursor.getInt(cursor.getColumnIndex(DataSetModel.Columns.NO_VALUE_REQUIRES_COMMENT)) == 1)
                        .skipOffline(cursor.getInt(cursor.getColumnIndex(DataSetModel.Columns.SKIP_OFFLINE)) == 1)
                        .dataElementDecoration(cursor.getInt(cursor.getColumnIndex(DataSetModel.Columns.DATA_ELEMENT_DECORATION)) == 1)
                        .renderAsTabs(cursor.getInt(cursor.getColumnIndex(DataSetModel.Columns.RENDER_AS_TABS)) == 1)
                        .renderHorizontally(cursor.getInt(cursor.getColumnIndex(DataSetModel.Columns.RENDER_HORIZONTALLY)) == 1)
                        .accessDataWrite(cursor.getInt(cursor.getColumnIndex(DataSetModel.Columns.ACCESS_DATA_WRITE)) == 1)
                        .build()).toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Flowable<Long> insertDataValue(DataValueModel dataValue) {

        String where = DataValueModel.Columns.DATA_ELEMENT + " = '" + dataValue.dataElement() + "' AND " + DataValueModel.Columns.PERIOD + " = '" + dataValue.period() +
                "' AND " + DataValueModel.Columns.ATTRIBUTE_OPTION_COMBO + " = '" + dataValue.attributeOptionCombo() +
                "' AND " + DataValueModel.Columns.CATEGORY_OPTION_COMBO + " = '" + dataValue.categoryOptionCombo() + "'";
        briteDatabase.delete(DataValueModel.TABLE, where);

        return Flowable.just(briteDatabase.insert(DataValueModel.TABLE, dataValue.toContentValues()));
    }

    @Override
    public Flowable<Map<String, List<CategoryOptionComboModel>>> getCatOptionCombo() {
        Map<String, List<CategoryOptionComboModel>> map = new HashMap<>();

        return briteDatabase.createQuery(CategoryOptionModel.TABLE, CATEGORY_OPTION_COMBO, dataSetUid)
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
                }).flatMap(categoryOptionComboModels -> Observable.just(map)).toFlowable(BackpressureStrategy.LATEST);
    }


    @Override
    public Flowable<Map<String, List<List<Pair<CategoryOptionModel, CategoryModel>>>>> getCatOptions(String section) {

        Map<String, List<List<Pair<CategoryOptionModel, CategoryModel>>>> map = new HashMap<>();
        String query = CATEGORY_OPTION;

        if (!section.equals("NO_SECTION")) {
            query = query + "AND section.name = '" + section + "' ";
        }
        query = query + "GROUP BY  CategoryOption.uid,Category.uid, SectionName,catCombo, CategoryCategoryOptionLink.sortOrder " +
                "ORDER BY section.uid, CategoryCategoryComboLink.sortOrder, CategoryCategoryOptionLink.sortOrder";

        return briteDatabase.createQuery(CategoryOptionModel.TABLE, query, dataSetUid)
                .mapToList(cursor -> {
                    CategoryOptionModel catOption = CategoryOptionModel.create(cursor);
                    CategoryModel category = CategoryModel.builder().uid(cursor.getString(cursor.getColumnIndex("category"))).build();
                    String catCombo = cursor.getString(cursor.getColumnIndex("catCombo"));

                    if (map.get(catCombo) == null) {
                        map.put(catCombo, new ArrayList<>());
                    }
                    if (map.get(catCombo).size() == 0) {
                        List<Pair<CategoryOptionModel, CategoryModel>> list = new ArrayList<>();
                        list.add(Pair.create(catOption, category));
                        map.get(catCombo).add(list);
                    } else {

                        if (map.get(catCombo).get(map.get(catCombo).size() - 1).get(0).val1().uid().equals(cursor.getString(cursor.getColumnIndex("category")))) {
                            map.get(catCombo).get(map.get(catCombo).size() - 1).add(Pair.create(catOption, category));
                        } else {
                            List<Pair<CategoryOptionModel, CategoryModel>> list = new ArrayList<>();
                            list.add(Pair.create(catOption, category));
                            map.get(catCombo).add(list);
                        }

                    }

                    return catOption;
                }).flatMap(categoryOptionComboModels -> Observable.just(map)).toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Flowable<List<DataSetTableModel>> getDataValues(String orgUnitUid, String periodType, String initPeriodType, String catOptionComb, String section) {
        List<DataSetTableModel> listData = new ArrayList<>();
        String query = DATA_VALUES;
        if (!section.equals("NO_SECTION"))
            query = query + "AND Section.name = '" + section + "' ";
        return briteDatabase.createQuery(DataValueModel.TABLE, query, orgUnitUid, catOptionComb, dataSetUid, initPeriodType)
                .mapToList(cursor -> {

                    for (DataSetTableModel dataValue : listData) {
                        if (dataValue.dataElement().equals(cursor.getString(cursor.getColumnIndex(DataValueModel.Columns.DATA_ELEMENT)))
                                && dataValue.categoryOptionCombo().equals(cursor.getString(cursor.getColumnIndex(DataValueModel.Columns.CATEGORY_OPTION_COMBO)))) {
                            dataValue.listCategoryOption().add(cursor.getString(cursor.getColumnIndex(DataSetTableModel.Columns.CATEGORY_OPTION)));

                            return dataValue;
                        }
                    }

                    List<String> listCatOptions = new ArrayList<>();
                    listCatOptions.add(cursor.getString(cursor.getColumnIndex(DataSetTableModel.Columns.CATEGORY_OPTION)));
                    DataSetTableModel dataValue = DataSetTableModel.create(
                            cursor.getLong(cursor.getColumnIndex(DataValueModel.Columns.ID)),
                            cursor.getString(cursor.getColumnIndex(DataValueModel.Columns.DATA_ELEMENT)),
                            cursor.getString(cursor.getColumnIndex(DataValueModel.Columns.PERIOD)),
                            cursor.getString(cursor.getColumnIndex(DataValueModel.Columns.ORGANISATION_UNIT)),
                            cursor.getString(cursor.getColumnIndex(DataValueModel.Columns.CATEGORY_OPTION_COMBO)),
                            cursor.getString(cursor.getColumnIndex(DataValueModel.Columns.ATTRIBUTE_OPTION_COMBO)),
                            cursor.getString(cursor.getColumnIndex(DataValueModel.Columns.VALUE)),
                            cursor.getString(cursor.getColumnIndex(DataValueModel.Columns.STORED_BY)),
                            cursor.getString(cursor.getColumnIndex(DataSetTableModel.Columns.CATEGORY_OPTION)),
                            listCatOptions,  cursor.getString(cursor.getColumnIndex(DataSetTableModel.Columns.CATEGORY_COMBO_LINK)) != null &&
                                    !cursor.getString(cursor.getColumnIndex(DataSetTableModel.Columns.CATEGORY_COMBO_LINK)).isEmpty() ?
                                    cursor.getString(cursor.getColumnIndex(DataSetTableModel.Columns.CATEGORY_COMBO_LINK)) :
                                    cursor.getString(cursor.getColumnIndex(DataSetTableModel.Columns.CATEGORY_COMBO)));
                    listData.add(dataValue);
                    return dataValue;

                }).map(data -> listData).toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Flowable<Map<String, Map<String, List<String>>>> getGreyedFields(List<String> categoryOptionCombos, String section) {

        Map<String, Map<String, List<String>>> mapData = new HashMap<>();

        String query = SECTION_GREYED_FIELDS.replace("?", categoryOptionCombos.toString().substring(1, categoryOptionCombos.toString().length()-1));
        if(!section.isEmpty() && !section.equals("NO_SECTION"))
            query = query + "and Section.name = '" + section +"' ";

        query = query + "GROUP BY section, dataElement,CatOptionCombo, categoryOption";
        return briteDatabase.createQuery(SectionGreyedFieldsLinkModel.TABLE, query)
                .mapToList(cursor -> {
                    String dataElement = cursor.getString(cursor.getColumnIndex("dataElement"));
                    String catOptionCombo = cursor.getString(cursor.getColumnIndex("CatOptionCombo"));
                    String catOption = cursor.getString(cursor.getColumnIndex("categoryOption"));

                    if(mapData.containsKey(dataElement)){

                        if(mapData.get(dataElement).get(catOptionCombo) != null){

                            mapData.get(dataElement).get(catOptionCombo)
                                    .add(catOption);
                        }else{
                            List<String> options = new ArrayList<>();
                            options.add(catOption);
                            mapData.get(dataElement).put(catOptionCombo, options);
                        }
                    }else{
                        Map<String, List<String>> mapOptions =  new HashMap<>();
                        List<String> options = new ArrayList<>();
                        options.add(catOption);
                        mapOptions.put(catOptionCombo, options);
                        mapData.put(dataElement, mapOptions);

                    }

                    return mapData;
                }).map(data->mapData).toFlowable(BackpressureStrategy.LATEST);
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
