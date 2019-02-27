package org.dhis2.usescases.datasets.dataSetTable;

import android.content.ContentValues;
import android.service.autofill.Dataset;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.data.tuples.Pair;
import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.category.CategoryModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionModel;
import org.hisp.dhis.android.core.common.BaseDataModel;
import org.hisp.dhis.android.core.common.State;
import org.hisp.dhis.android.core.dataelement.DataElementModel;
import org.hisp.dhis.android.core.dataset.DataSet;
import org.hisp.dhis.android.core.dataset.DataSetCompleteRegistration;
import org.hisp.dhis.android.core.dataset.DataSetModel;
import org.hisp.dhis.android.core.dataset.SectionGreyedFieldsLinkModel;
import org.hisp.dhis.android.core.dataset.SectionModel;
import org.hisp.dhis.android.core.datavalue.DataValueModel;
import org.hisp.dhis.android.core.event.EventModel;
import org.hisp.dhis.android.core.event.EventStatus;
import org.hisp.dhis.android.core.period.PeriodType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import nl.jqno.equalsverifier.internal.lib.bytebuddy.description.annotation.AnnotationValue;

public class DataSetTableRepositoryImpl implements DataSetTableRepository {

    private final String DATA_ELEMENTS = "SELECT " +
            "DataElement.*," +
            "DataSetSection.sectionName," +
            "DataSetSection.sectionOrder " +
            "FROM DataElement " +
            "LEFT JOIN (" +
            "   SELECT " +
            "       Section.sortOrder AS sectionOrder," +
            "       Section.displayName AS sectionName," +
            "       Section.uid AS sectionId," +
            "       SectionDataElementLink.dataElement AS sectionDataElement, " +
            "       SectionDataElementLink.sortOrder AS sortOrder " +
            "   FROM Section " +
            "   JOIN SectionDataElementLink ON SectionDataElementLink.section = Section.uid " +
            ") AS DataSetSection ON DataSetSection.sectionDataElement = DataElement.uid " +
            "JOIN DataSetDataElementLink ON DataSetDataElementLink.dataElement = DataElement.uid " +
            "WHERE DataSetDataElementLink.dataSet = ? " +
            "ORDER BY DataSetSection.sectionOrder,DataSetSection.sortOrder";

    private final String PERIOD_CODE = "SELECT Period.* FROM Period WHERE Period.periodType = ? AND Period.startDate = ? LIMIT 1";
    private final String DATA_VALUES = "SELECT DataValue.*, CategoryOptionComboCategoryOptionLink.categoryOption as catOption FROM DataValue " +
            "JOIN CategoryOptionComboCategoryOptionLink ON CategoryOptionComboCategoryOptionLink.categoryOptionCombo = DataValue.categoryOptionCombo " +
            "JOIN DataSetDataElementLink ON DataSetDataElementLink.dataElement = DataValue.dataElement " +
            "WHERE DataValue.organisationUnit = ? " +
            "AND DataValue.attributeOptionCombo = ? " +
            "AND DataSetDataElementLink.dataSet = ? " +
            "AND DataValue.period = ?";
    private final String DATA_SET = "SELECT DataSet.* FROM DataSet WHERE DataSet.uid = ?";
    private final String CATEGORY_OPTION = "SELECT CategoryOption.*, Category.uid AS category, section.displayName as SectionName," +
            "CategoryCategoryComboLink.sortOrder as sortOrder FROM CategoryOption " +
            "JOIN CategoryCategoryOptionLink ON CategoryCategoryOptionLink.categoryOption = CategoryOption.uid " +
            "JOIN Category ON CategoryCategoryOptionLink.category = Category.uid " +
            "JOIN CategoryCategoryComboLink ON CategoryCategoryComboLink.category = Category.uid " +
            "JOIN CategoryOptionComboCategoryOptionLink ON CategoryOptionComboCategoryOptionLink.categoryOption = CategoryOption.uid " +
            "JOIN DataElement ON DataElement.categoryCombo = CategoryCategoryComboLink.categoryCombo " +
            "JOIN DataSetDataElementLink ON DataSetDataElementLink.dataElement = DataElement.uid " +
            "JOIN CategoryCombo ON CategoryCombo.uid = DataElement.categoryCombo " +
            "LEFT JOIN (SELECT section.displayName, section.uid, SectionDataElementLINK.dataElement as dataelement FROM Section " +
            "JOIN SectionDataElementLINK ON SectionDataElementLink.section = Section.uid) as section on section.dataelement = DataElement.uid " +
            "WHERE DataSetDataElementLink.dataSet = ? " +
            "GROUP BY CategoryOption.uid, section.uid ORDER BY section.uid, CategoryCategoryComboLink.sortOrder, CategoryCategoryOptionLink.sortOrder";

    private final String CATEGORY_OPTION_COMBO = "SELECT CategoryOptionCombo.*,section.displayName as SectionName FROM CategoryOptionCombo " +
            "JOIN DataElement ON DataElement.categoryCombo = CategoryOptionCombo.categoryCombo " +
            "JOIN DataSetDataElementLink ON DataSetDataElementLink.dataElement = DataElement.uid " +
            "JOIN CategoryCategoryComboLink ON CategoryCategoryComboLink.categoryCombo = categoryOptionCombo.categoryCombo " +
            "JOIN CategoryOptionComboCategoryOptionLink ON CategoryOptionComboCategoryOptionLink.categoryOptionCombo = CategoryOptionCombo.uid " +
            "JOIN CategoryCategoryOptionLink ON CategoryCategoryOptionLink.categoryOption = CategoryOptionComboCategoryOptionLink.categoryOption " +
            "LEFT JOIN (SELECT section.displayName, section.uid, SectionDataElementLINK.dataElement as dataelement FROM Section " +
            "JOIN SectionDataElementLINK ON SectionDataElementLink.section = Section.uid) as section on section.dataelement = DataElement.uid " +
            "WHERE DataSetDataElementLink.dataSet = ? " +
            "GROUP BY section.uid, CategoryOptionCombo.uid  ORDER BY section.uid, CategoryCategoryComboLink.sortOrder, CategoryCategoryOptionLink.sortOrder";

    private final String SECTION_GREYED_FIELDS = "select Section.name as section, DataElement.uid as dataElement, CategoryOptionComboCategoryOptionLink.categoryOption as categoryOption " +
            "from SectionGreyedFieldsLink " +
            "join DataElementOperand on SectionGreyedFieldsLink.dataElementOperand = DataElementOperand.uid " +
            "join DataElement on DataElement.uid = DataElementOperand.dataElement " +
            "join CategoryOptionCombo on CategoryOptionCombo.uid = DataElementOperand.categoryOptionCombo " +
            "join CategoryOptionComboCategoryOptionLink on CategoryOptionComboCategoryOptionLink.categoryOptionCombo = CategoryOptionCombo.uid " +
            "join Section on Section.uid = SectionGreyedFieldsLink.section " +
            "where CategoryOptionComboCategoryOptionLink.categoryOptionCombo in (?) " +
            "GROUP BY section, dataElement, categoryOption";

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
            "WHERE DataSet.uid = ?";

    private final BriteDatabase briteDatabase;
    private final String dataSetUid;

    public DataSetTableRepositoryImpl(BriteDatabase briteDatabase, String dataSetUid) {
        this.briteDatabase = briteDatabase;
        this.dataSetUid = dataSetUid;
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
    public Flowable<Map<String, List<DataElementModel>>> getDataElements() {
        Map<String, List<DataElementModel>> map = new HashMap<>();
        return briteDatabase.createQuery(DataElementModel.TABLE, DATA_ELEMENTS, dataSetUid)
                .mapToList(cursor -> {
                    DataElementModel dataElementModel = DataElementModel.create(cursor);
                    String section = cursor.getString(cursor.getColumnIndex("sectionName"));
                    if (section == null)
                        section = "NO_SECTION";
                    if (map.get(section) == null) {
                        map.put(section, new ArrayList<>());
                    }
                    map.get(section).add(dataElementModel);

                    return dataElementModel;
                })
                .flatMap(dataElementModels -> Observable.just(map)).toFlowable(BackpressureStrategy.LATEST);
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

    /**
     * returns Map key =
     */
    @Override
    public Flowable<Map<String, List<List<Pair<CategoryOptionModel, CategoryModel>>>>> getCatOptions() {
        Map<String, List<List<Pair<CategoryOptionModel, CategoryModel>>>> map = new HashMap<>();

        return briteDatabase.createQuery(CategoryOptionModel.TABLE, CATEGORY_OPTION, dataSetUid)
                .mapToList(cursor -> {
                    CategoryOptionModel catOption = CategoryOptionModel.create(cursor);
                    CategoryModel category = CategoryModel.builder().uid(cursor.getString(cursor.getColumnIndex("category"))).build();
                    String sectionName = cursor.getString(cursor.getColumnIndex("SectionName"));
                    if (sectionName == null)
                        sectionName = "NO_SECTION";
                    if (map.get(sectionName) == null) {
                        map.put(sectionName, new ArrayList<>());
                    }
                    if(map.get(sectionName).size() == 0){
                        List<Pair<CategoryOptionModel, CategoryModel>> list = new ArrayList<>();
                        list.add(Pair.create(catOption, category));
                        map.get(sectionName).add(list);
                    }else {

                        if (map.get(sectionName).get(map.get(sectionName).size()-1).get(0).val1().uid().equals(cursor.getString(cursor.getColumnIndex("category")))) {
                            map.get(sectionName).get(map.get(sectionName).size()-1).add(Pair.create(catOption, category));
                        } else {
                            List<Pair<CategoryOptionModel, CategoryModel>> list = new ArrayList<>();
                            list.add(Pair.create(catOption, category));
                            map.get(sectionName).add(list);
                        }

                    }

                    return catOption;
                }).flatMap(categoryOptionComboModels -> Observable.just(map)).toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Flowable<List<DataSetTableModel>> getDataValues(String orgUnitUid, String periodType, String initPeriodType, String catOptionComb) {
        List<DataSetTableModel> listData = new ArrayList<>();
        return briteDatabase.createQuery(DataValueModel.TABLE, DATA_VALUES, orgUnitUid, catOptionComb,dataSetUid, periodType)
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
                            listCatOptions);
                    listData.add(dataValue);
                    return dataValue;

                }).map(data->listData).toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Flowable<Map<String, Map<String, List<String>>>> getGreyedFields(List<String> categoryOptionCombo) {

        Map<String, Map<String, List<String>>> mapData = new HashMap<>();

        String query = SECTION_GREYED_FIELDS.replace("?", categoryOptionCombo.toString().substring(1, categoryOptionCombo.toString().length()-1));
        return briteDatabase.createQuery(SectionGreyedFieldsLinkModel.TABLE, query)
                .mapToList(cursor -> {
                    if(mapData.containsKey(cursor.getString(cursor.getColumnIndex("section")))) {
                        if(mapData.get(cursor.getString(cursor.getColumnIndex("section"))).containsKey(cursor.getString(cursor.getColumnIndex("dataElement")))){
                            mapData.get(cursor.getString(cursor.getColumnIndex("section")))
                                    .get(cursor.getString(cursor.getColumnIndex("dataElement")))
                                    .add(cursor.getString(cursor.getColumnIndex("categoryOption")));
                        }else{
                            List<String> listCatOptions =  new ArrayList<>();
                            listCatOptions.add(cursor.getString(cursor.getColumnIndex("categoryOption")));

                            mapData.get(cursor.getString(cursor.getColumnIndex("section")))
                                    .put(cursor.getString(cursor.getColumnIndex("dataElement")), listCatOptions);
                        }
                    }
                    else{
                        List<String> listCatOptions = new ArrayList<>();
                        listCatOptions.add(cursor.getString(cursor.getColumnIndex("categoryOption")));
                        Map<String, List<String>> mapDataElement = new HashMap<>();
                        mapDataElement.put(cursor.getString(cursor.getColumnIndex("dataElement")),listCatOptions);

                        mapData.put(cursor.getString(cursor.getColumnIndex("section")), mapDataElement);
                    }
                    return mapData;
                }).map(data->mapData).toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Flowable<Map<String, List<String>>> getMandatoryDataElement(List<String> categoryOptionCombo) {
        Map<String, List<String>> mapData = new HashMap<>();

        String query = GET_COMPULSORY_DATA_ELEMENT.replace("?", categoryOptionCombo.toString().substring(1, categoryOptionCombo.toString().length()-1));
        return briteDatabase.createQuery(SectionGreyedFieldsLinkModel.TABLE, query)
                .mapToList(cursor -> {
                    if(mapData.containsKey(cursor.getString(0))){
                        mapData.get(cursor.getString(0)).add(cursor.getString(1));
                    }
                    else{
                        List<String> listCatOp = new ArrayList<>();
                        listCatOp.add(cursor.getString(1));
                        mapData.put(cursor.getString(0), listCatOp);
                    }

                    return mapData ;
                }).map(data->mapData).toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Flowable<List<SectionModel>> getSectionByDataSet() {
       return briteDatabase.createQuery(SectionModel.TABLE, SECTION_TOTAL_ROW_COLUMN, dataSetUid)
               .mapToList(SectionModel::create).toFlowable(BackpressureStrategy.LATEST);
    }
}
