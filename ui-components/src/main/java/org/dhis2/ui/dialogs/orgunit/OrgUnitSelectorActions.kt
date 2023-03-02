package org.dhis2.ui.dialogs.orgunit

interface OrgUnitSelectorActions {
    val onSearch: (String) -> Unit
    val onOrgUnitChecked: (orgUnitUid: String, isChecked: Boolean) -> Unit
    val onOpenOrgUnit: (orgUnitUid: String) -> Unit
    val onDoneClick: () -> Unit
    val onCancelClick: () -> Unit
    val onClearClick: () -> Unit
}
