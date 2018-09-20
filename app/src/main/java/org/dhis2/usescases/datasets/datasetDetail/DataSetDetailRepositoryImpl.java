package org.dhis2.usescases.datasets.datasetDetail;

import android.database.Cursor;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import org.dhis2.data.forms.dataentry.fields.orgUnit.OrgUnitViewModel;
import org.dhis2.utils.Period;
import com.squareup.sqlbrite2.BriteDatabase;

import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryComboModel;
import org.hisp.dhis.android.core.category.CategoryOptionComboModel;
import org.hisp.dhis.android.core.dataset.DataSetDataElementLinkModel;
import org.hisp.dhis.android.core.dataset.DataSetModel;
import org.hisp.dhis.android.core.dataset.DataSetOrganisationUnitLinkModel;
import org.hisp.dhis.android.core.datavalue.DataValueModel;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnitModel;
import org.hisp.dhis.android.core.period.PeriodModel;
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.Observable;

public class DataSetDetailRepositoryImpl implements DataSetDetailRepository {

    private final BriteDatabase briteDatabase;

    public DataSetDetailRepositoryImpl(BriteDatabase briteDatabase) {
        this.briteDatabase = briteDatabase;
    }

    @NonNull
    @Override
    public Observable<List<DataSetDetailModel>> filteredDataSet(String uidDataSet, String fromDate, String toDate, CategoryOptionComboModel categoryOptionComboModel) {
        String SELECT_ORGUNIT = "SELECT " + DataSetModel.TABLE +"." +DataSetModel.Columns.UID +", "
                +OrganisationUnitModel.TABLE + "."+ OrganisationUnitModel.Columns.NAME +","
                + CategoryComboModel.TABLE + "."+ CategoryComboModel.Columns.NAME +","
                + DataSetModel.TABLE + "."+ DataSetModel.Columns.PERIOD_TYPE

                +" FROM "+ DataSetModel.TABLE +
                " JOIN "+ OrganisationUnitModel.TABLE + " ON "+ OrganisationUnitModel.TABLE + "." + OrganisationUnitModel.Columns.UID + " = "+ DataSetOrganisationUnitLinkModel.TABLE + "." + DataSetOrganisationUnitLinkModel.Columns.ORGANISATION_UNIT
                +" JOIN "+ DataSetOrganisationUnitLinkModel.TABLE + " ON " + DataSetOrganisationUnitLinkModel.TABLE + "."+ DataSetOrganisationUnitLinkModel.Columns.DATA_SET + " = " + DataSetModel.TABLE + "."+ DataSetModel.Columns.UID


                +" JOIN "+ CategoryComboModel.TABLE + " ON " + CategoryComboModel.TABLE + "." + CategoryComboModel.Columns.UID + " = " + DataSetModel.TABLE + "." + DataSetModel.Columns.CATEGORY_COMBO;
        if(!TextUtils.isEmpty(uidDataSet)) {
            SELECT_ORGUNIT += " WHERE " + DataSetModel.TABLE + "." + DataSetModel.Columns.UID + " = '" + uidDataSet + "'";
        }
        SELECT_ORGUNIT += " GROUP BY "+DataSetModel.TABLE + "."+ DataSetModel.Columns.PERIOD_TYPE;
        return briteDatabase.createQuery(DataSetModel.TABLE, SELECT_ORGUNIT)
                .mapToList( dataSet -> new DataSetDetailModel(dataSet.getString(0),
                        dataSet.getString(1),
                        dataSet.getString(2),
                        dataSet.getString(3)));
    }

    @NonNull
    @Override
    public Observable<List<DataSetDetailModel>> filteredDataSet( List<Date> dates, Period period, CategoryOptionComboModel categoryOptionComboModel) {
        return null;
    }

    @NonNull
    @Override
    public Observable<List<OrganisationUnitModel>> orgUnits() {
        String SELECT_ORG_UNITS = "SELECT * FROM " + OrganisationUnitModel.TABLE;
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, SELECT_ORG_UNITS)
                .mapToList(OrganisationUnitModel::create);
    }

    private Observable<CategoryComboModel> catComboDataSet(String uidCatCombo) {
        String SELECT_CATEGORY_COMBO = "SELECT " + CategoryComboModel.TABLE + ".* FROM " + CategoryComboModel.TABLE +
                 " WHERE " + CategoryComboModel.TABLE + "." + CategoryComboModel.Columns.UID + " = '" + uidCatCombo + "'";
        return briteDatabase.createQuery(CategoryComboModel.TABLE, SELECT_CATEGORY_COMBO)
                .mapToOne(CategoryComboModel::create);
    }

    private Observable<List<PeriodModel>> periodsDataSet(String periodType){
        String SELECT_PERIOD = "SELECT " + PeriodModel.TABLE + ".* FROM " + PeriodModel.TABLE +
                " WHERE " + PeriodModel.TABLE + "." + PeriodModel.Columns.PERIOD_TYPE + " = '" + periodType + "'";
        return briteDatabase.createQuery(PeriodModel.TABLE, SELECT_PERIOD)
                .mapToList(PeriodModel::create);
    }

    private Observable<OrganisationUnitModel> orgUnitDataSet(String periodType){
        String SELECT_ORGUNIT = "SELECT " + OrganisationUnitModel.TABLE + ".* FROM " + OrganisationUnitModel.TABLE +
                ", "+ DataSetOrganisationUnitLinkModel.TABLE + ", "+ DataSetModel.TABLE +
                " WHERE " + OrganisationUnitModel.TABLE + "." + OrganisationUnitModel.Columns.UID + " = "+ DataSetOrganisationUnitLinkModel.TABLE + "." + OrganisationUnitModel.Columns.UID
                + " AND " + DataSetOrganisationUnitLinkModel.TABLE + "."+ DataSetOrganisationUnitLinkModel.Columns.DATA_SET + " = " + DataSetModel.TABLE + "."+ DataSetModel.Columns.UID;
        return briteDatabase.createQuery(OrganisationUnitModel.TABLE, SELECT_ORGUNIT)
                .mapToOne(OrganisationUnitModel::create);
    }

    @NonNull
    @Override
    public Observable<List<TrackedEntityDataValueModel>> dataSetDataValues(DataSetModel eventModel) {
        return null;
    }

    @Override
    public Observable<List<String>> dataSetValuesNew(DataSetDetailModel eventModel) {
        return null;
    }

    @Override
    public Observable<Boolean> writePermission(String programId) {
        return null;
    }
}
