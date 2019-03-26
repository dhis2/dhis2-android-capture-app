package org.dhis2.usescases.datasets.datasetInitial;

import android.database.Cursor;
import androidx.annotation.NonNull;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.category.CategoryModel;
import org.hisp.dhis.android.core.category.CategoryOptionModel;
import org.hisp.dhis.android.core.dataset.DataSetModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.period.PeriodType;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

public class DataSetInitialRepositoryImpl implements DataSetInitialRepository {

    private static final String GET_DATA_SET_INFO = "SELECT " +
            "DataSet.displayName, " +
            "DataSet.description, " +
            "DataSet.categoryCombo, " +
            "DataSet.periodType, " +
            "CategoryCombo.displayName " +
            "FROM DataSet JOIN CategoryCombo ON CategoryCombo.uid = DataSet.categoryCombo " +
            "WHERE DataSet.uid = ? LIMIT 1";

    private static final String GET_ORG_UNITS = "SELECT OrganisationUnit.* FROM OrganisationUnit " +
            "JOIN DataSetOrganisationUnitLink ON DataSetOrganisationUnitLink.organisationUnit = OrganisationUnit.uid " +
            "WHERE DataSetOrganisationUnitLink.dataSet = ?";

    private static final String GET_CATEGORIES = "SELECT Category.* FROM Category " +
            "JOIN CategoryCategoryComboLink ON CategoryCategoryComboLink.category = Category.uid " +
            "WHERE CategoryCategoryComboLink.categoryCombo = ?";
    private static final String GET_CATEGORY_OPTION = "SELECT CategoryOption.* FROM CategoryOption " +
            "JOIN CategoryCategoryOptionLink ON CategoryCategoryOptionLink.categoryOption = CategoryOption.uid " +
            "WHERE CategoryCategoryOptionLink.category = ? ORDER BY CategoryOption.displayName ASC";


    private final BriteDatabase briteDatabase;
    private final String dataSetUid;

    public DataSetInitialRepositoryImpl(com.squareup.sqlbrite2.BriteDatabase briteDatabase, String dataSetUid) {
        this.briteDatabase = briteDatabase;
        this.dataSetUid = dataSetUid;
    }

    @NonNull
    @Override
    public Observable<DataSetInitialModel> dataSet() {
        return briteDatabase.createQuery(DataSetModel.TABLE, GET_DATA_SET_INFO, dataSetUid)
                .mapToOne(cursor -> {

                    String displayName = cursor.getString(0);
                    String description = cursor.getString(1);
                    String categoryComboUid = cursor.getString(2);
                    PeriodType periodType = PeriodType.valueOf(cursor.getString(3));
                    String categoryComboName = cursor.getString(4);

                    List<CategoryModel> categoryModels = getCategoryModels(categoryComboUid);

                    return DataSetInitialModel.create(
                            displayName,
                            description,
                            categoryComboUid,
                            categoryComboName,
                            periodType,
                            categoryModels
                    );
                });
    }

    private List<CategoryModel> getCategoryModels(String categoryComboUid) {
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
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, GET_ORG_UNITS, dataSetUid)
                .mapToList(OrganisationUnitModel::create);
    }

    @NonNull
    @Override
    public Observable<List<CategoryOptionModel>> catCombo(String categoryUid) {
        return briteDatabase.createQuery(CategoryOptionModel.TABLE, GET_CATEGORY_OPTION, categoryUid)
                .mapToList(CategoryOptionModel::create);
    }
}
