package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import com.gabrielittner.auto.value.cursor.ColumnName;
import com.google.auto.value.AutoValue;

import org.dhis2.data.forms.dataentry.tablefields.FieldViewModel;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel;
import org.hisp.dhis.android.core.category.CategoryOptionModel;
import org.hisp.dhis.android.core.dataelement.DataElementModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;

@AutoValue
public abstract class DataTableModel {

    public static class Columns {
        public static final String SECTION = "section";
        public static final String HEADERS = "headers";
        public static final String ROWS = "rows";
        public static final String CELLS = "cells";
        public static final String VALUES = "values";
        public static final String DATA_VALUES = "dataValues";
        public static final String DATA_ELEMENTS_DISABLED = "dataElementDisabled";
        public static final String COMPULSORY_CELLS = "compulsoryCells";
        public static final String CAT_OPTION_COMBO_CAT_OPTION = "catOptionComboCatOption";
        public static final String ORDER_LIST_CAT_OPTIONS = "orderListCatOptions";
    }

    @Nullable
    @ColumnName(DataTableModel.Columns.SECTION)
    public abstract String section();

    @Nullable
    @ColumnName(DataTableModel.Columns.HEADERS)
    public abstract Map<String, List<DataElementModel>> headers();

    @Nullable
    @ColumnName(DataTableModel.Columns.ROWS)
    public abstract Map<String, List<List<CategoryOptionModel>>> rows();

    @Nullable
    @ColumnName(DataTableModel.Columns.CELLS)
    public abstract List<List<FieldViewModel>> cells();

    @Nullable
    @ColumnName(DataTableModel.Columns.VALUES)
    public abstract ArrayList<List<String>> values();

    @Nullable
    @ColumnName(DataTableModel.Columns.DATA_VALUES)
    public abstract List<DataSetTableModel> dataValues();

    @Nullable
    @ColumnName(DataTableModel.Columns.DATA_ELEMENTS_DISABLED)
    public abstract Map<String, Map<String, List<String>>> dataElementDisabled();

    @Nullable
    @ColumnName(DataTableModel.Columns.COMPULSORY_CELLS)
    public abstract Map<String, List<String>> compulsoryCells();

    @Nullable
    @ColumnName(DataTableModel.Columns.CAT_OPTION_COMBO_CAT_OPTION)
    public abstract Map<String, List<String>> catOptionComboCatOption();

    @Nullable
    @ColumnName(DataTableModel.Columns.ORDER_LIST_CAT_OPTIONS)
    public abstract Map<String, List<List<CategoryOptionModel>>> orderListCatOptions();


}
