package org.dhis2.usescases.datasets.datasetInitial;

import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.D2;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.common.BaseIdentifiableObject;
import org.hisp.dhis.android.core.dataset.DataInputPeriodModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;

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
                    String categoryComboDisplayName = d2.categoryModule().categoryCombos.byUid().eq(dataSet.categoryCombo().uid()).one().get().displayName();
                    CategoryCombo categoryCombos = d2.categoryModule().categoryCombos.withCategories().byUid().eq(dataSet.categoryCombo().uid()).one().get();

                    return DataSetInitialModel.create(
                            dataSet.displayName(),
                            dataSet.description(),
                            dataSet.categoryCombo().uid(),
                            categoryComboDisplayName,
                            dataSet.periodType(),
                            categoryCombos.categories()
                    );
                });
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
                .map(Category::categoryOptions);
    }

    @NonNull
    @Override
    public Flowable<String> getCategoryOptionCombo(List<String> catOptions, String catCombo) {
        return Flowable.just(d2.categoryModule().categoryOptionCombos.withCategoryOptions().byCategoryOptions(catOptions).byCategoryComboUid().eq(catCombo).one().get())
                .map(BaseIdentifiableObject::uid);
    }
}
