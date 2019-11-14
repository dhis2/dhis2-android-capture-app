package org.dhis2.usescases.datasets.datasetInitial

import java.util.Date
import org.dhis2.usescases.general.AbstractActivityContracts
import org.hisp.dhis.android.core.category.CategoryOption
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.period.PeriodType

interface DataSetInitialView : AbstractActivityContracts.View {

    var selectedPeriod: Date?

    fun setData(dataSetInitialModel: DataSetInitialModel)

    fun showOrgUnitDialog(orgUnits: List<OrganisationUnit>)

    fun showPeriodSelector(
        periodType: PeriodType,
        periods: List<DateRangeInputPeriodModel>,
        openFuturePeriods: Int?
    )

    fun showCatComboSelector(catOptionUid: String, categoryOptions: List<CategoryOption>)

    fun setOrgUnit(organisationUnit: OrganisationUnit)

    fun navigateToDataSetTable(catOptionCombo: String, periodId: String)

    fun getSelectedCatOptions(): List<String>
}
