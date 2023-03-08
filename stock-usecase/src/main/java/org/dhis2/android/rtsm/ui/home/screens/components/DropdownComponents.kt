@file:Suppress("PreviewAnnotationInFunctionWithParameters")

package org.dhis2.android.rtsm.ui.home.screens.components

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.zIndex
import androidx.fragment.app.FragmentManager
import org.dhis2.android.rtsm.R
import org.dhis2.android.rtsm.data.TransactionType
import org.dhis2.android.rtsm.data.models.TransactionItem
import org.dhis2.android.rtsm.ui.home.model.DataEntryStep
import org.dhis2.android.rtsm.ui.home.model.DataEntryUiState
import org.dhis2.android.rtsm.ui.home.model.EditionDialogResult
import org.dhis2.android.rtsm.ui.home.model.SettingsUiState
import org.dhis2.android.rtsm.utils.Utils.Companion.capitalizeText
import org.dhis2.commons.orgunitselector.OUTreeFragment
import org.dhis2.commons.orgunitselector.OrgUnitSelectorScope
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit

var orgUnitData: OrganisationUnit? = null
var orgUnitName: String? = null

@Composable
fun DropdownComponentTransactions(
    settingsUiState: SettingsUiState,
    onTransitionSelected: (transition: TransactionType) -> Unit,
    hasUnsavedData: Boolean,
    themeColor: Color = colorResource(R.color.colorPrimary),
    data: MutableList<TransactionItem>,
    launchDialog: (msg: Int, (result: EditionDialogResult) -> Unit) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val itemIcon = data.find {
        it.transactionType == settingsUiState.transactionType
    }?.icon ?: data.first().icon

    var selectedIndex by remember { mutableStateOf(0) }
    val paddingValue = if (selectedIndex >= 0) {
        4.dp
    } else {
        0.dp
    }

    var textFieldSize by remember { mutableStateOf(Size.Zero) }

    val icon = if (isExpanded) {
        painterResource(id = R.drawable.ic_arrow_drop_up)
    } else {
        painterResource(id = R.drawable.ic_arrow_drop_down)
    }

    val interactionSource = remember { MutableInteractionSource() }
    if (interactionSource.collectIsPressedAsState().value) {
        isExpanded = !isExpanded
    }

    Column(Modifier.padding(horizontal = 16.dp)) {
        OutlinedTextField(
            value = capitalizeText(settingsUiState.transactionType.name),
            onValueChange = { },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    textFieldSize = coordinates.size.toSize()
                }
                .shadow(
                    elevation = 8.dp,
                    ambientColor = Color.Black.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(30.dp),
                    clip = false
                )
                .offset(0.dp, 0.dp)
                .background(color = Color.White, shape = RoundedCornerShape(30.dp)),
            readOnly = true,
            singleLine = true,
            leadingIcon = {
                Icon(
                    painter = painterResource(itemIcon),
                    contentDescription = null,
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp),
                    tint = themeColor
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        isExpanded = !isExpanded
                    }
                ) {
                    Icon(icon, contentDescription = null, tint = themeColor)
                }
            },
            shape = RoundedCornerShape(30.dp),
            placeholder = {
                Text(text = capitalizeText(data.first().transactionType.name))
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White
            ),
            interactionSource = interactionSource,
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color.Black)
        )

        MaterialTheme(shapes = MaterialTheme.shapes.copy(medium = RoundedCornerShape(16.dp))) {
            DropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false },
                modifier = Modifier
                    .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
                    .background(shape = RoundedCornerShape(16.dp), color = Color.White),
                offset = DpOffset(x = 0.dp, y = 2.dp)
            ) {
                data.forEachIndexed { index, item ->
                    DropdownMenuItem(
                        onClick = {
                            if (selectedIndex != index && hasUnsavedData) {
                                launchDialog.invoke(R.string.transaction_discarted) { result ->
                                    when (result) {
                                        EditionDialogResult.DISCARD -> {
                                            // Perform the transaction change and clear data
                                            onTransitionSelected.invoke(item.transactionType)
                                            selectedIndex = index
                                            isExpanded = false
                                        }
                                        EditionDialogResult.KEEP -> {
                                            // Leave it as it was
                                            isExpanded = false
                                        }
                                    }
                                }
                            } else {
                                onTransitionSelected.invoke(item.transactionType)
                                selectedIndex = index
                                isExpanded = false
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    color = if (selectedIndex == index) {
                                        colorResource(R.color.bg_gray_f1f)
                                    } else {
                                        Color.White
                                    },
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(paddingValue),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Icon(
                                painter = painterResource(item.icon),
                                contentDescription = null,
                                Modifier.padding(6.dp),
                                tint = themeColor
                            )
                            Text(text = capitalizeText(item.transactionType.name))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DropdownComponentFacilities(
    settingsUiState: SettingsUiState,
    onFacilitySelected: (facility: OrganisationUnit) -> Unit,
    hasUnsavedData: Boolean,
    themeColor: Color = colorResource(R.color.colorPrimary),
    supportFragmentManager: FragmentManager,
    data: List<OrganisationUnit>,
    launchDialog: (msg: Int, (result: EditionDialogResult) -> Unit) -> Unit
) {
    var selectedText by remember { mutableStateOf("") }

    var textFieldSize by remember { mutableStateOf(Size.Zero) }

    if (data.size == 1) {
        onFacilitySelected.invoke(data.get(0))
        orgUnitData = data.get(0)
    }

    val interactionSource = remember { MutableInteractionSource() }
    if (interactionSource.collectIsPressedAsState().value) {
        openOrgUnitTreeSelector(
            supportFragmentManager,
            settingsUiState,
            hasUnsavedData,
            onFacilitySelected,
            launchDialog
        )
    }

    Column(Modifier.padding(horizontal = 16.dp)) {
        OutlinedTextField(
            value = settingsUiState.facilityName(),
            onValueChange = { selectedText = it },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    textFieldSize = coordinates.size.toSize()
                }
                .shadow(
                    elevation = 8.dp,
                    ambientColor = Color.Black.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(30.dp),
                    clip = false
                )
                .offset(0.dp, 0.dp)
                .background(color = Color.White, shape = RoundedCornerShape(30.dp)),
            readOnly = true,
            singleLine = true,
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_org_unit_chart),
                    contentDescription = null,
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp),
                    tint = themeColor
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        openOrgUnitTreeSelector(
                            supportFragmentManager,
                            settingsUiState,
                            hasUnsavedData,
                            onFacilitySelected,
                            launchDialog
                        )
                    }
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_arrow_drop_down),
                        contentDescription = null,
                        tint = themeColor
                    )
                }
            },
            shape = RoundedCornerShape(30.dp),
            placeholder = {
                Text(text = capitalizeText("${stringResource(R.string.from)}..."))
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White
            ),
            interactionSource = interactionSource,
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color.Black)
        )
    }
}

@Composable
fun DropdownComponentDistributedTo(
    onDestinationSelected: (destination: Option) -> Unit,
    dataEntryUiState: DataEntryUiState,
    themeColor: Color = colorResource(R.color.colorPrimary),
    data: List<Option>,
    isDestinationSelected: (value: String) -> Unit = { },
    launchDialog: (msg: Int, (result: EditionDialogResult) -> Unit) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    var selectedText by remember { mutableStateOf("") }

    var textFieldSize by remember { mutableStateOf(Size.Zero) }

    var selectedIndex by remember { mutableStateOf(-1) }
    val paddingValue = if (selectedIndex >= 0) {
        4.dp
    } else {
        0.dp
    }

    val icon = if (isExpanded) {
        painterResource(id = R.drawable.ic_arrow_drop_up)
    } else {
        painterResource(id = R.drawable.ic_arrow_drop_down)
    }

    val interactionSource = remember { MutableInteractionSource() }
    if (interactionSource.collectIsPressedAsState().value) {
        isExpanded = !isExpanded
    }

    isDestinationSelected(selectedText)

    dataEntryUiState.step
    when (dataEntryUiState.step) {
        DataEntryStep.COMPLETED -> {
            selectedText = ""
            selectedIndex = -1
        }
        else -> {}
    }

    Column(Modifier.padding(horizontal = 16.dp)) {
        OutlinedTextField(
            value = selectedText,
            onValueChange = { selectedText = it },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    textFieldSize = coordinates.size.toSize()
                }
                .zIndex(1f)
                .shadow(
                    elevation = 8.dp,
                    ambientColor = Color.Black.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(30.dp),
                    clip = false
                )
                .offset(0.dp, 1.dp)
                .background(color = Color.White, shape = RoundedCornerShape(30.dp)),
            readOnly = true,
            singleLine = true,
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_location_on),
                    contentDescription = null,
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp),
                    tint = themeColor
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = {
                        isExpanded = !isExpanded
                    }
                ) {
                    Icon(icon, contentDescription = null, tint = themeColor)
                }
            },
            shape = RoundedCornerShape(30.dp),
            placeholder = {
                Text(text = capitalizeText(stringResource(R.string.to)))
            },
            colors = TextFieldDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White
            ),
            interactionSource = interactionSource,
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color.Black)
        )

        MaterialTheme(shapes = MaterialTheme.shapes.copy(medium = RoundedCornerShape(16.dp))) {
            DropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false },
                modifier = Modifier
                    .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
                    .background(Color.White, RoundedCornerShape(30.dp)),
                offset = DpOffset(x = 0.dp, y = 2.dp)
            ) {
                data.forEachIndexed { index, item ->
                    DropdownMenuItem(
                        onClick = {
                            if (selectedIndex != index && dataEntryUiState.hasUnsavedData) {
                                launchDialog.invoke(R.string.transaction_discarted) { result ->
                                    when (result) {
                                        EditionDialogResult.DISCARD -> {
                                            // Perform the transaction change and clear data
                                            selectedText =
                                                capitalizeText(item.displayName().toString())
                                            isExpanded = false
                                            selectedIndex = index
                                            onDestinationSelected.invoke(item)
                                        }
                                        EditionDialogResult.KEEP -> {
                                            // Leave it as it was
                                            isExpanded = false
                                        }
                                    }
                                }
                            } else {
                                selectedText = capitalizeText(item.displayName().toString())
                                isExpanded = false
                                selectedIndex = index
                                onDestinationSelected.invoke(item)
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    color = if (selectedIndex == index) {
                                        colorResource(R.color.bg_gray_f1f)
                                    } else {
                                        Color.White
                                    },
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(
                                    start = 8.dp,
                                    top = paddingValue,
                                    end = 8.dp,
                                    bottom = paddingValue
                                ),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start
                        ) {
                            Text(text = capitalizeText(item.displayName().toString()))
                        }
                    }
                }
            }
        }
    }
}

fun openOrgUnitTreeSelector(
    supportFragmentManager: FragmentManager,
    settingsUiState: SettingsUiState,
    hasUnsavedData: Boolean,
    onFacilitySelected: (facility: OrganisationUnit) -> Unit,
    launchDialog: (msg: Int, (result: EditionDialogResult) -> Unit) -> Unit
) {
    OUTreeFragment.Builder()
        .showAsDialog()
        .singleSelection()
        .orgUnitScope(OrgUnitSelectorScope.ProgramCaptureScope(settingsUiState.programUid))
        .withPreselectedOrgUnits(orgUnitData?.let { listOf(it.uid()) } ?: emptyList())
        .onSelection { selectedOrgUnits ->
            val selectedOrgUnit = selectedOrgUnits.firstOrNull()
            if (selectedOrgUnit != null) {
                if (orgUnitData != selectedOrgUnit && hasUnsavedData) {
                    launchDialog.invoke(R.string.transaction_discarted) { result ->
                        when (result) {
                            EditionDialogResult.DISCARD -> {
                                // Perform the transaction change and clear data
                                onFacilitySelected.invoke(selectedOrgUnit)
                                orgUnitData = selectedOrgUnit
                            }
                            EditionDialogResult.KEEP -> {
                                // Leave it as it was
                            }
                        }
                    }
                } else {
                    onFacilitySelected.invoke(selectedOrgUnit)
                    orgUnitData = selectedOrgUnit
                }
            }
        }
        .build()
        .show(supportFragmentManager, "")
}
