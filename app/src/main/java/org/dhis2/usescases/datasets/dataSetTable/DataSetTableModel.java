package org.dhis2.usescases.datasets.dataSetTable;

import android.database.Cursor;

import androidx.annotation.Nullable;

import com.gabrielittner.auto.value.cursor.ColumnName;
import com.google.auto.value.AutoValue;

import org.hisp.dhis.android.core.common.BaseModel;

import java.util.List;

import static org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel.Columns.CATEGORY_OPTION;
import static org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel.Columns.LIST_CATEGORY_OPTION;

@AutoValue
public abstract class DataSetTableModel{
    public static class Columns extends BaseModel.Columns {
        static final String DATA_ELEMENT = "dataElement";
        static final String PERIOD = "period";
        static final String ORGANISATION_UNIT = "organisationUnit";
        static final String CATEGORY_OPTION_COMBO = "categoryOptionCombo";
        static final String ATTRIBUTE_OPTION_COMBO = "attributeOptionCombo";
        static final String VALUE = "value";
        static final String STORED_BY = "storedBy";
        static final String CATEGORY_OPTION = "catOption";
        static final String LIST_CATEGORY_OPTION = "listCategory";
        static final String CATEGORY_COMBO = "catComboDataElement";
    }

    public static DataSetTableModel fromCursor(Cursor cursor){
        return AutoValue_DataSetTableModel.createFromCursor(cursor);
    }
    public static DataSetTableModel create(Long id, String dataElement, String period, String organisationUnit,
                                           String categoryOptionCombo, String attributeOptionCombo, String value, String storedBy,
                                           String catOption, List<String> listCategory, String catCombo) {
        return new AutoValue_DataSetTableModel(id, dataElement, period, organisationUnit, categoryOptionCombo, attributeOptionCombo, value, storedBy, catOption, listCategory, catCombo);
    }

    public DataSetTableModel setValue(String value) {
        return new AutoValue_DataSetTableModel(id(), dataElement(), period(), organisationUnit(), categoryOptionCombo(),
                attributeOptionCombo(), value, storedBy(), catOption(), listCategoryOption(),catCombo());
    }

    public static final String TABLE = "DataValue";

    @ColumnName(Columns.ID)
    public abstract Long id();

    @Nullable
    @ColumnName(Columns.DATA_ELEMENT)
    public abstract String dataElement();

    @Nullable
    @ColumnName(Columns.PERIOD)
    public abstract String period();

    @Nullable
    @ColumnName(Columns.ORGANISATION_UNIT)
    public abstract String organisationUnit();

    @Nullable
    @ColumnName(Columns.CATEGORY_OPTION_COMBO)
    public abstract String categoryOptionCombo();

    @Nullable
    @ColumnName(Columns.ATTRIBUTE_OPTION_COMBO)
    public abstract String attributeOptionCombo();

    @Nullable
    @ColumnName(Columns.VALUE)
    public abstract String value();

    @Nullable
    @ColumnName(Columns.STORED_BY)
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
