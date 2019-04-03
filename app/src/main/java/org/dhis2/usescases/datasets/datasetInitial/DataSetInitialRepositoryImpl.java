package org.dhis2.usescases.datasets.datasetInitial;

import android.database.Cursor;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.category.CategoryModel;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.dataset.DataInputPeriodModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;

public class DataSetInitialRepositoryImpl implements DataSetInitialRepository {

    private final BriteDatabase briteDatabase;
    private final String dataSetUid;
    private final D2 d2;

    public DataSetInitialRepositoryImpl(D2 d2, BriteDatabase briteDatabase, String dataSetUid) {
        this.d2 = d2;
        this.briteDatabase = briteDatabase;
        this.dataSetUid = dataSetUid;
    }

    @Override
    public Flowable<List<DateRangeInputPeriodModel>> getDataInputPeriod() {
        String GET_DATA_INPUT_PERIOD = "SELECT DataInputPeriod.*, Period.startDate as initialPeriodDate, Period.endDate as endPeriodDate " +
                "FROM DataInputPeriod " +
                "JOIN Period ON Period.periodId = DataInputPeriod.period " +
                "WHERE dataset = ? ORDER BY initialPeriodDate DESC";

        return briteDatabase.createQuery(DataInputPeriodModel.TABLE, GET_DATA_INPUT_PERIOD, dataSetUid)
                .mapToList(DateRangeInputPeriodModel::fromCursor)
                .toFlowable(BackpressureStrategy.LATEST);
    }

    @NonNull
    @Override
    public Observable<DataSetInitialModel> dataSet() {
        return Observable.just(d2.dataSetModule().dataSets.byUid().eq(dataSetUid).one().get())
                .map(dataSet -> {
                    String categoryComboDisplayName =
                            d2.categoryModule().categoryCombos.byUid().eq(dataSet.categoryCombo().uid()).one().get().displayName();

                    List<CategoryModel> categoryModels = getCategoryModels(dataSet.categoryCombo().uid());

                    return DataSetInitialModel.create(
                            dataSet.displayName(),
                            dataSet.description(),
                            dataSet.categoryCombo().uid(),
                            categoryComboDisplayName,
                            dataSet.periodType(),
                            categoryModels
                    );
                });
    }

    private List<CategoryModel> getCategoryModels(String categoryComboUid) {
        String GET_CATEGORIES = "SELECT Category.* FROM Category " +
                "JOIN CategoryCategoryComboLink ON CategoryCategoryComboLink.category = Category.uid " +
                "WHERE CategoryCategoryComboLink.categoryCombo = ?";

        List<CategoryModel> categoryModelList = new ArrayList<>();
        try (Cursor cursor = briteDatabase.query(GET_CATEGORIES, categoryComboUid)) {
            if (cursor != null && cursor.moveToFirst()) {
                for (int i = 0; i < cursor.getCount(); i++) {
                    categoryModelList.add(CategoryModel.create(cursor));
                    cursor.moveToNext();
                }
            }
        }

        return categoryModelList;
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnitModel>> orgUnits() {
        String GET_ORG_UNITS = "SELECT OrganisationUnit.* FROM OrganisationUnit " +
                "JOIN DataSetOrganisationUnitLink ON DataSetOrganisationUnitLink.organisationUnit = OrganisationUnit.uid " +
                "WHERE DataSetOrganisationUnitLink.dataSet = ?";

        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, GET_ORG_UNITS, dataSetUid)
                .mapToList(OrganisationUnitModel::create);
    }

    @NonNull
    @Override
    public Observable<List<CategoryOption>> catCombo(String categoryUid) {
        return Observable.just(d2.categoryModule().categories.withCategoryOptions().byUid().eq(categoryUid).one().get())
                .map(category -> category.categoryOptions());
    }

    @NonNull
    @Override
    public Flowable<String> getCategoryOptionCombo(String catOptions, String catCombo) {
        String GET_CAT_OPTION_COMBO = " SELECT CategoryOptionCombo.* " +
                " FROM CategoryOptionCombo " +
                " JOIN CategoryCategoryComboLink ON CategoryCategoryComboLink.categoryCombo = CategoryOptionCombo.categoryCombo " +
                " JOIN CategoryCategoryOptionLink ON CategoryCategoryOptionLink.categoryOption = CategoryOptionComboCategoryOptionLink.categoryOption " +
                " JOIN CategoryOptionComboCategoryOptionLink ON CategoryOptionComboCategoryOptionLink.categoryOptionCombo = CategoryOptionCombo.uid " +
                " WHERE CategoryOptionCombo.categoryCombo = ? ";

        String query = GET_CAT_OPTION_COMBO;
        query = query + " AND CategoryOptionComboCategoryOptionLink.categoryOption IN (" + catOptions + ")";
        return briteDatabase.createQuery(CategoryOptionComboModel.TABLE, query, catCombo)
                .mapToOne(cursor ->
                        cursor.getString(cursor.getColumnIndex("uid"))
                ).toFlowable(BackpressureStrategy.LATEST);
    }
}
