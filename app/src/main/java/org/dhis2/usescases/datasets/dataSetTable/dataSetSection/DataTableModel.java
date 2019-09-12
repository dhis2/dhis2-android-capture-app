package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import com.gabrielittner.auto.value.cursor.ColumnName;
import com.google.auto.value.AutoValue;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.dataset.DataSet;
import org.hisp.dhis.android.core.dataset.Section;

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
        public static final String APPROVAL = "approval";
    }

    public static DataTableModel create(Section section, Map<String, List<List<CategoryOption>>> headers, List<DataElement> rows,
                                        List<DataSetTableModel> dataValues, List<Pair<String, List<String>>> dataElementDisabled,
                                        Map<String, List<String>> compulsoryCells, Map<String, List<String>> catOptionComboCatOption,
                                        Map<String, List<List<Pair<CategoryOption, Category>>>> listCatOptionsCatComboOptions,
                                        DataSet dataSet, Map<String, String> catCombos, List<CategoryOption> catOptions, Boolean approval) {
        return new AutoValue_DataTableModel(section, headers, rows, dataValues, dataElementDisabled, compulsoryCells, catOptionComboCatOption, listCatOptionsCatComboOptions, dataSet,
                catCombos, catOptions, approval);
    }

    @Nullable
    @ColumnName(Columns.SECTION)
    public abstract Section section();

    @Nullable
    @ColumnName(Columns.HEADERS)
    public abstract Map<String, List<List<CategoryOption>>> headers();

    @Nullable
    @ColumnName(Columns.ROWS)
    public abstract List<DataElement> rows();

    @Nullable
    @ColumnName(Columns.DATA_VALUES)
    public abstract List<DataSetTableModel> dataValues();

    @Nullable
    @ColumnName(Columns.DATA_ELEMENTS_DISABLED)
    public abstract List<Pair<String, List<String>>> dataElementDisabled();

    @Nullable
    @ColumnName(Columns.COMPULSORY_CELLS)
    public abstract Map<String, List<String>> compulsoryCells();

    @Nullable
    @ColumnName(Columns.CAT_OPTION_COMBO_CAT_OPTION)
    public abstract Map<String, List<String>> catOptionComboCatOption();

    @Nullable
    @ColumnName(Columns.LIST_CAT_OPTIONS_CAT_COMBO_OPTIONS)
    public abstract Map<String, List<List<Pair<CategoryOption, Category>>>> listCatOptionsCatComboOptions();

    @Nullable
    @ColumnName(Columns.DATA_SET)
    public abstract DataSet dataSet();

    @Nullable
    @ColumnName(Columns.CAT_COMBOS)
    public abstract Map<String, String> catCombos();

    @Nullable
    @ColumnName(Columns.CAT_OPTIONS)
    public abstract List<CategoryOption> catOptions();

    @Nullable
    @ColumnName(Columns.APPROVAL)
    public abstract Boolean approval();

}
