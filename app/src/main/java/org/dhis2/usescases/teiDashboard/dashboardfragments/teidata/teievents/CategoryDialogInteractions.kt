package org.dhis2.usescases.teiDashboard.dashboardfragments.teidata.teievents

import androidx.fragment.app.FragmentManager
import java.util.Date
import org.dhis2.utils.category.CategoryDialog

interface CategoryDialogInteractions {
    fun showDialog(
        categoryComboUid: String,
        dateControl: Date?,
        fragmentManager: FragmentManager,
        onItemSelected: (selectedCatOptComboUid: String) -> Unit
    ) {
        val categoryDialog = CategoryDialog(
            CategoryDialog.Type.CATEGORY_OPTION_COMBO,
            categoryComboUid,
            true,
            dateControl
        ) { selectedCatOptComboUid -> onItemSelected(selectedCatOptComboUid) }
        categoryDialog.isCancelable = false
        categoryDialog.show(fragmentManager, CategoryDialog.TAG)
    }
}
