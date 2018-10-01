package org.dhis2.usescases.datasets.dataSetTable;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.dataelement.DataElementModel;
import org.hisp.dhis.android.core.datavalue.DataValueModel;
import org.hisp.dhis.android.core.period.PeriodModel;

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

    private final BriteDatabase briteDatabase;
    private final String dataSetUid;

    public DataSetTableRepositoryImpl(BriteDatabase briteDatabase, String dataSetUid) {
        this.briteDatabase = briteDatabase;
        this.dataSetUid = dataSetUid;
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
