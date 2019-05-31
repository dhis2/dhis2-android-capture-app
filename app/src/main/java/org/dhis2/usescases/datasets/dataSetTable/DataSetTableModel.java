package org.dhis2.usescases.datasets.dataSetTable;

import com.gabrielittner.auto.value.cursor.ColumnName;
import com.google.auto.value.AutoValue;

import android.database.Cursor;
import androidx.annotation.Nullable;

import org.hisp.dhis.android.core.dataset.DataSetModel;
import org.hisp.dhis.android.core.datavalue.DataValueModel;

import java.util.List;

import static org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel.Columns.CATEGORY_OPTION;
import static org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel.Columns.LIST_CATEGORY_OPTION;

@AutoValue
public abstract class DataSetTableModel{
    public static class Columns extends DataSetModel.Columns {
        public static final String CATEGORY_OPTION = "catOption";
        public static final String LIST_CATEGORY_OPTION = "listCategory";
        public static final String CATEGORY_COMBO = "catComboDataElement";
        public static final String CATEGORY_COMBO_LINK = "catComboLink";
    }

    public static DataSetTableModel fromCursor(Cursor cursor){
        return AutoValue_DataSetTableModel.createFromCursor(cursor);
    }
    public static DataSetTableModel create(Long id, String dataElement, String period, String organisationUnit,
                                           String categoryOptionCombo, String attributeOptionCombo, String value, String storedBy,
                                           String catOption, List<String> listCategory, String catCombo) {
        return new AutoValue_DataSetTableModel(id, dataElement, period, organisationUnit, categoryOptionCombo, attributeOptionCombo, value, storedBy, catOption, listCategory, catCombo);
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

    @Nullable
    @ColumnName(LIST_CATEGORY_OPTION)
    public abstract List<String> listCategoryOption();

    @Nullable
    @ColumnName(DataSetTableModel.Columns.CATEGORY_COMBO)
    public abstract String catCombo();
}
