package org.dhis2.usescases.datasets.dataSetTable.dataSetSection;

import org.dhis2.composetable.model.TableCell;
import org.dhis2.data.forms.dataentry.tablefields.RowAction;
import org.dhis2.data.forms.dataentry.tablefields.spinner.SpinnerViewModel;
import org.dhis2.usescases.general.AbstractActivityContracts;
import org.hisp.dhis.android.core.dataelement.DataElement;
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.SortedMap;

public class DataValueContract {

    public interface View extends AbstractActivityContracts.View {
        void showSnackBar();

        void update(boolean modified);

        void setTableData(TableData tableData);

        void updateTabLayout(int count);

        void renderIndicators(@NotNull SortedMap<String, String> indicators);

        void updateData(RowAction rowAction, @Nullable String catCombo);

        void onValueProcessed();

        void clearTables();

        void updateProgressVisibility();

        void showCalendar(DataElement dataElement, TableCell cell, Boolean showTimePicker);

        void showTimePicker(DataElement dataElement, TableCell cell);

        void showBooleanDialog(DataElement dataElement, TableCell cell);

        void showAgeDialog(DataElement dataElement, TableCell cell);

        void showCoordinatesDialog(DataElement dataElement, TableCell cell);

        void showOtgUnitDialog(DataElement dataElement, TableCell cell, List<OrganisationUnit> orgUnits);

        void showOptionSetDialog(@NotNull DataElement dataElement, @NotNull TableCell cell, SpinnerViewModel spinnerViewModel);
    }
}
