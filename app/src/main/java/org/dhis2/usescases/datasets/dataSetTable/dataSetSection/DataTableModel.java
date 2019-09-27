package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import com.gabrielittner.auto.value.cursor.ColumnName;
import com.google.auto.value.AutoValue;

import org.dhis2.data.tuples.Pair;
import org.dhis2.usescases.datasets.dataSetTable.DataSetTableModel;
import org.hisp.dhis.android.core.category.Category;
import org.hisp.dhis.android.core.category.CategoryCombo;
import org.hisp.dhis.android.core.category.CategoryOption;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.dataelement.DataElementOperand;
import org.hisp.dhis.android.core.dataset.DataSet;
import org.hisp.dhis.android.core.dataset.Section;

import java.util.List;
import java.util.Map;

import androidx.annotation.Nullable;

@AutoValue
public abstract class DataTableModel {

    public static class Columns {
        public static final String ROWS = "rows";
        public static final String HEADER = "header";
        public static final String DATA_VALUES = "dataValues";
        public static final String DATA_ELEMENTS_DISABLED = "dataElementDisabled";
        public static final String COMPULSORY_CELLS = "compulsoryCells";
        public static final String CAT_COMBOS = "catCombos";
    }

    public static DataTableModel create(List<DataElement> rows, List<DataSetTableModel> dataValues, List<DataElementOperand> dataElementDisabled,
                                        List<DataElementOperand> compulsoryCells, CategoryCombo catCombo, List<List<CategoryOption>> header) {
        return new AutoValue_DataTableModel(rows,  dataValues, dataElementDisabled, compulsoryCells, catCombo, header);
    }


    @Nullable
    @ColumnName(Columns.ROWS)
    public abstract List<DataElement> rows();

    @Nullable
    @ColumnName(Columns.DATA_VALUES)
    public abstract List<DataSetTableModel> dataValues();

    @Nullable
    @ColumnName(Columns.DATA_ELEMENTS_DISABLED)
    public abstract List<DataElementOperand> dataElementDisabled();

    @Nullable
    @ColumnName(Columns.COMPULSORY_CELLS)
    public abstract List<DataElementOperand> compulsoryCells();

    @Nullable
    @ColumnName(Columns.CAT_COMBOS)
    public abstract CategoryCombo catCombo();

    @Nullable
    @ColumnName(Columns.HEADER)
    public abstract List<List<CategoryOption>> header();

}
