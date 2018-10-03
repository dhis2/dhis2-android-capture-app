package org.dhis2.usescases.datasets.dataSetTable;

import com.squareup.sqlbrite2.BriteDatabase;

import org.dhis2.utils.DateUtils;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.dataelement.DataElementModel;
import org.hisp.dhis.android.core.dataset.DataSetModel;
import org.hisp.dhis.android.core.datavalue.DataValueModel;
import org.hisp.dhis.android.core.period.PeriodModel;
import org.hisp.dhis.android.core.period.PeriodType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;

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
            "       SectionDataElementLink.dataElement AS sectionDataElement " +
            "   FROM Section " +
            "   JOIN SectionDataElementLink ON SectionDataElementLink.section = Section.uid " +
            ") AS DataSetSection ON DataSetSection.sectionDataElement = DataElement.uid " +
            "JOIN DataSetDataElementLink ON DataSetDataElementLink.dataElement = DataElement.uid " +
            "WHERE DataSetDataElementLink.dataSet = ? " +
            "ORDER BY DataSetSection.sectionOrder";

    private final String PERIOD_CODE = "SELECT Period.* FROM Period WHERE Period.periodType = ? AND Period.startDate = ? LIMIT 1";
    private final String DATA_VALUES = "SELECT * FROM DataValue " +
            "WHERE DataValue.organisationUnit = ? " +
            "AND DataValue.categoryOptionCombo = ? " +
            "AND DataValue.period = ?";
    private final String DATA_SET = "SELECT DataSet.* FROM DataSet WHERE DataSet.uid = ?";

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
    public Flowable<Map<String, List<CategoryOptionComboModel>>> getCatOptions() {
        String query = "SELECT CategoryOptionCombo.* FROM CategoryOptionCombo " +
                "JOIN DataElement ON DataElement.categoryCombo = CategoryOptionCombo.categoryCombo " +
                "JOIN DataSetDataElementLink ON DataSetDataElementLink.dataElement = DataElement.uid " +
                "WHERE DataSetDataElementLink.dataSet = ? " +
                "GROUP BY CategoryOptionCombo.uid";
        Map<String, List<CategoryOptionComboModel>> map = new HashMap<>();

        return briteDatabase.createQuery(CategoryOptionComboModel.TABLE, query, dataSetUid)
                .mapToList(cursor -> {
                    CategoryOptionComboModel catOptionCombo = CategoryOptionComboModel.create(cursor);
                    if (map.get(catOptionCombo.categoryCombo()) == null) {
                        map.put(catOptionCombo.categoryCombo(), new ArrayList<>());
                    }
                    map.get(catOptionCombo.categoryCombo()).add(catOptionCombo);
                    return catOptionCombo;
                }).flatMap(categoryOptionComboModels -> Observable.just(map)).toFlowable(BackpressureStrategy.LATEST);
    }

    @Override
    public Flowable<List<DataValueModel>> getDataValues(String orgUnitUid, String periodType, String initPeriodType, String catOptionComb) {
        return briteDatabase.createQuery(PeriodModel.TABLE, PERIOD_CODE, periodType, initPeriodType)
                .mapToOne(PeriodModel::create)
                .flatMap(periodModel -> briteDatabase.createQuery(DataValueModel.TABLE, DATA_VALUES, periodModel.periodId())
                        .mapToList(cursor -> {
                            return DataValueModel.builder()
                                    .build();
                        })).toFlowable(BackpressureStrategy.LATEST);
    }
}
