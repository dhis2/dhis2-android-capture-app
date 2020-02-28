package org.dhis2.usescases.datasets.dataSetTable;

import java.util.List;

import com.google.auto.value.AutoValue;

import android.database.Cursor;

import androidx.annotation.Nullable;

@AutoValue
public abstract class DataSetTableModel{

    public abstract Long id();

    @Nullable
    public abstract String dataElement();

    @Nullable
    public abstract String period();

    @Nullable
    public abstract String organisationUnit();

    @Nullable
    public abstract String categoryOptionCombo();

    @Nullable
    public abstract String attributeOptionCombo();

    @Nullable
    public abstract String value();

    @Nullable
    public abstract String storedBy();

    @Nullable
    public abstract String catOption();

    @Nullable
    public abstract List<String> listCategoryOption();

    @Nullable
    public abstract String catCombo();

    public static DataSetTableModel create(Long id, String dataElement, String period, String organisationUnit,
                                           String categoryOptionCombo, String attributeOptionCombo, String value, String storedBy,
                                           String catOption, List<String> listCategory, String catCombo) {
        return new AutoValue_DataSetTableModel(id, dataElement, period, organisationUnit, categoryOptionCombo, attributeOptionCombo, value, storedBy, catOption, listCategory, catCombo);
    }

    public DataSetTableModel setValue(String value) {
        return new AutoValue_DataSetTableModel(id(), dataElement(), period(), organisationUnit(), categoryOptionCombo(),
                attributeOptionCombo(), value, storedBy(), catOption(), listCategoryOption(),catCombo());
    }

}
