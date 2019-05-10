package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import android.database.Cursor;

import com.gabrielittner.auto.value.cursor.ColumnName;
import com.google.auto.value.AutoValue;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel;
import org.hisp.dhis.android.core.category.CategoryModel;
import org.hisp.dhis.android.core.category.CategoryOptionModel;
import org.hisp.dhis.android.core.dataelement.DataElementModel;
import org.hisp.dhis.android.core.dataset.DataSetModel;
import org.hisp.dhis.android.core.dataset.SectionModel;

import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;

@AutoValue
public abstract class DataTableModel {

    public static class Columns {
        public static final String SECTION = "section";
        public static final String HEADERS = "headers";
        public static final String ROWS = "rows";
        public static final String DATA_VALUES = "dataValues";
        public static final String DATA_ELEMENTS_DISABLED = "dataElementDisabled";
        public static final String COMPULSORY_CELLS = "compulsoryCells";
        public static final String CAT_OPTION_COMBO_CAT_OPTION = "catOptionComboCatOption";
        public static final String LIST_CAT_OPTIONS_CAT_COMBO_OPTIONS = "listCatOptionsCatComboOptions";
        public static final String DATA_SET = "dataSet";
        public static final String CAT_COMBOS = "catCombos";
        public static final String CAT_OPTIONS = "catOptions";
    }

    public static DataTableModel create(SectionModel section, Map<String, List<List<CategoryOptionModel>>> headers, List<DataElementModel> rows,
                                        List<DataSetTableModel> dataValues,List<Pair<String, List<String>>> dataElementDisabled,
                                        Map<String, List<String>> compulsoryCells, Map<String, List<String>> catOptionComboCatOption,
                                        Map<String, List<List<Pair<CategoryOptionModel, CategoryModel>>>> listCatOptionsCatComboOptions,
                                        DataSetModel dataSet, Map<String, String> catCombos, List<CategoryOptionModel> catOptions) {
        return new AutoValue_DataTableModel(section, headers, rows, dataValues, dataElementDisabled, compulsoryCells, catOptionComboCatOption, listCatOptionsCatComboOptions, dataSet,
                catCombos, catOptions);
    }

    @Nullable
    @ColumnName(DataTableModel.Columns.SECTION)
    public abstract SectionModel section();

    @Nullable
    @ColumnName(DataTableModel.Columns.HEADERS)
    public abstract Map<String, List<List<CategoryOptionModel>>> headers();

    @Nullable
    @ColumnName(DataTableModel.Columns.ROWS)
    public abstract List<DataElementModel> rows();

    @Nullable
    @ColumnName(DataTableModel.Columns.DATA_VALUES)
    public abstract List<DataSetTableModel> dataValues();

    @Nullable
    @ColumnName(DataTableModel.Columns.DATA_ELEMENTS_DISABLED)
    public abstract List<Pair<String, List<String>>> dataElementDisabled();

    @Nullable
    @ColumnName(DataTableModel.Columns.COMPULSORY_CELLS)
    public abstract Map<String, List<String>> compulsoryCells();

    @Nullable
    @ColumnName(DataTableModel.Columns.CAT_OPTION_COMBO_CAT_OPTION)
    public abstract Map<String, List<String>> catOptionComboCatOption();

    @Nullable
    @ColumnName(DataTableModel.Columns.LIST_CAT_OPTIONS_CAT_COMBO_OPTIONS)
    public abstract Map<String, List<List<Pair<CategoryOptionModel, CategoryModel>>>> listCatOptionsCatComboOptions();

    @Nullable
    @ColumnName(DataTableModel.Columns.DATA_SET)
    public abstract DataSetModel dataSet();

    @Nullable
    @ColumnName(Columns.CAT_COMBOS)
    public abstract Map<String, String> catCombos();

    @Nullable
    @ColumnName(Columns.CAT_OPTIONS)
    public abstract List<CategoryOptionModel> catOptions();

}
