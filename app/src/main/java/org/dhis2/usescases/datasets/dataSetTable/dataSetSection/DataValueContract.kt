package org.dhis2.usescases.datasets.dataSetTable.dataSetSection

import org.dhis2.composetable.model.TableCell
import org.dhis2.data.forms.dataentry.tablefields.spinner.SpinnerViewModel
import org.dhis2.usescases.general.AbstractActivityContracts
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

class DataValueContract {
    interface View : AbstractActivityContracts.View {
        fun onValueProcessed()
        fun showCalendar(
            dataElement: DataElement,
            cell: TableCell,
            showTimePicker: Boolean,
            updateCellValue: (TableCell) -> Unit
        )

        fun showTimePicker(
            dataElement: DataElement,
            cell: TableCell,
            updateCellValue: (TableCell) -> Unit
        )

        fun showBooleanDialog(
            dataElement: DataElement,
            cell: TableCell,
            updateCellValue: (TableCell) -> Unit
        )

        fun showAgeDialog(
            dataElement: DataElement,
            cell: TableCell,
            updateCellValue: (TableCell) -> Unit
        )

        fun showCoordinatesDialog(
            dataElement: DataElement,
            cell: TableCell,
            updateCellValue: (TableCell) -> Unit
        )

        fun showOtgUnitDialog(
            dataElement: DataElement,
            cell: TableCell,
            orgUnits: List<OrganisationUnit>,
            updateCellValue: (TableCell) -> Unit
        )

        fun showOptionSetDialog(
            dataElement: DataElement,
            cell: TableCell,
            spinnerViewModel: SpinnerViewModel,
            updateCellValue: (TableCell) -> Unit
        )
    }
}
