package org.dhis2.usescases.datasets.dataSetTable;

import com.gabrielittner.auto.value.cursor.ColumnAdapter;
import com.gabrielittner.auto.value.cursor.ColumnName;
import com.google.auto.value.AutoValue;

import android.database.Cursor;
import android.support.annotation.Nullable;

import org.hisp.dhis.android.core.common.BaseIdentifiableObjectModel;
import org.hisp.dhis.android.core.common.BaseNameableObjectModel;
import org.hisp.dhis.android.core.data.database.DbDateColumnAdapter;
import org.hisp.dhis.android.core.data.database.DbPeriodTypeColumnAdapter;
import org.hisp.dhis.android.core.dataset.DataSetModel;
import org.hisp.dhis.android.core.datavalue.DataValueModel;

import static org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel.Columns.CATEGORY_OPTION;

@AutoValue
public abstract class DataSetTableModel{
    public static class Columns extends DataSetModel.Columns {
        public static final String CATEGORY_OPTION = "catOption";
    }

    public static DataSetTableModel fromCursor(Cursor cursor){
        return AutoValue_DataSetTableModel.createFromCursor(cursor);
    }
    public static DataSetTableModel create(Long id, String dataElement, String period, String organisationUnit,
                                           String categoryOptionCombo, String attributeOptionCombo, String value, String storedBy,
                                           String catOption) {
        return new AutoValue_DataSetTableModel(id, dataElement, period, organisationUnit, categoryOptionCombo, attributeOptionCombo, value, storedBy, catOption);
    }


    public static final String TABLE = "DataValue";

    @ColumnName(DataValueModel.Columns.ID)
    public abstract Long id();

    @Nullable
    @ColumnName(DataValueModel.Columns.DATA_ELEMENT)
    public abstract String dataElement();

    @Nullable
    @ColumnName(DataValueModel.Columns.PERIOD)
    public abstract String period();

    @Nullable
    @ColumnName(DataValueModel.Columns.ORGANISATION_UNIT)
    public abstract String organisationUnit();

    @Nullable
    @ColumnName(DataValueModel.Columns.CATEGORY_OPTION_COMBO)
    public abstract String categoryOptionCombo();

    @Nullable
    @ColumnName(DataValueModel.Columns.ATTRIBUTE_OPTION_COMBO)
    public abstract String attributeOptionCombo();

    @Nullable
    @ColumnName(DataValueModel.Columns.VALUE)
    public abstract String value();

    @Nullable
    @ColumnName(DataValueModel.Columns.STORED_BY)
    public abstract String storedBy();

    @Nullable
    @ColumnName(CATEGORY_OPTION)
    public abstract String catOption();

}
