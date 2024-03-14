@file:OptIn(ExperimentalFoundationApi::class)

package org.dhis2.ui.dialogs.orgunit

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dhis2.ui.R
import org.dhis2.ui.buttons.Dhis2Button
import org.dhis2.ui.buttons.Dhis2TextButton
import org.dhis2.ui.model.ButtonUiModel
import org.dhis2.ui.theme.defaultFontFamily

@Composable
fun OrgUnitSelectorDialog(
    title: String?,
    items: List<OrgUnitTreeItem>,
    actions: OrgUnitSelectorActions,
) {
    Surface(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth(),
        color = Color.White,
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            title?.let {
                DialogTitle(title)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = spacedBy(8.dp),
            ) {
                Search(
                    modifier = Modifier.weight(1f),
                    onValueChangeListener = actions.onSearch,
                )
                Dhis2TextButton(
                    modifier = Modifier.testTag(CLEAR_TEST_TAG),
                    model = ButtonUiModel(
                        text = stringResource(id = R.string.action_clear_all),
                        onClick = actions.onClearClick,
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(
                                id = R.drawable.ic_tree_node_clear,
                            ),
                            contentDescription = "",
                        )
                    },
                )
            }
            Divider()
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                OrgUnitTree(
                    modifier = Modifier.weight(1f),
                    items = items,
                    onOrgUnitChecked = actions.onOrgUnitChecked,
                    onOpenOrgUnit = actions.onOpenOrgUnit,
                )
            }
            Divider()
            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(
                        vertical = 8.dp,
                        horizontal = 16.dp,
                    ),
                horizontalArrangement = spacedBy(16.dp),
            ) {
                Dhis2TextButton(
                    modifier = Modifier.testTag(CANCEL_TEST_TAG),
                    model = ButtonUiModel(
                        text = stringResource(id = R.string.action_cancel),
                        onClick = actions.onCancelClick,
                    ),
                )
                Dhis2Button(
                    modifier = Modifier.testTag(DONE_TEST_TAG),
                    model = ButtonUiModel(
                        text = stringResource(id = R.string.action_done),
                        onClick = actions.onDoneClick,
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "",
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun DialogTitle(title: String) {
    Text(text = title)
}

@Composable
private fun Search(
    modifier: Modifier = Modifier,
    hint: String = stringResource(id = R.string.hint_search),
    onValueChangeListener: (String) -> Unit,
) {
    var currentValue by remember {
        mutableStateOf<String?>(null)
    }
    Row(
        modifier = modifier
            .background(
                color = Color(0x0A000000),
                shape = RoundedCornerShape(16.dp),
            )
            .border(
                width = 1.dp,
                color = Color(0x61000000),
                shape = RoundedCornerShape(16.dp),
            )
            .padding(
                horizontal = 8.dp,
                vertical = 4.dp,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "",
            tint = Color(0x8A333333),
        )
        BasicTextField(
            modifier = Modifier.testTag(SEARCH_TEST_TAG),
            value = currentValue ?: "",
            onValueChange = {
                currentValue = it
                onValueChangeListener(it)
            },
            maxLines = 1,
            textStyle = TextStyle(
                color = Color(0x8A333333),
                fontSize = 16.sp,
                fontFamily = defaultFontFamily,
                lineHeight = 24.sp,
            ),
            decorationBox = { innerTextField ->
                if (currentValue.isNullOrEmpty()) {
                    Text(
                        text = hint,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.LightGray,
                        style = TextStyle(
                            color = Color(0x8A333333),
                            fontSize = 16.sp,
                            fontFamily = defaultFontFamily,
                            lineHeight = 24.sp,

                        ),
                    )
                }
                innerTextField()
            },
        )
    }
}

@Composable
fun OrgUnitTree(
    modifier: Modifier = Modifier,
    items: List<OrgUnitTreeItem>,
    onOrgUnitChecked: (orgUnitUid: String, isChecked: Boolean) -> Unit,
    onOpenOrgUnit: (orgUnitUid: String) -> Unit,
) {
    LazyColumn(
        modifier = modifier
            .testTag(ITEM_LIST_TEST_TAG)
            .fillMaxWidth(),
    ) {
        items(items = items, key = { it.uid }) { orgUnitItem ->
            OrgUnitSelectorItem(
                modifier = Modifier.animateItemPlacement(),
                higherLevel = items.minBy { it.level }.level,
                orgUnitItem = orgUnitItem,
                onOpenOrgUnit = onOpenOrgUnit,
                onOrgUnitChecked = onOrgUnitChecked,
            )
        }
    }
}

@Preview(device = Devices.PIXEL_3A)
@Composable
fun OrgUnitSelectorDialogPreview() {
    val items = listOf(
        OrgUnitTreeItem(
            "uid",
            "orgUnit1",
            true,
            hasChildren = true,
        ),
        OrgUnitTreeItem(
            "uid",
            "orgUnit2",
            false,
            level = 1,
            hasChildren = true,
        ),
        OrgUnitTreeItem(
            "uid",
            "orgUnit2",
            false,
            level = 1,
            hasChildren = false,
        ),
    )
    OrgUnitSelectorDialog(
        null,
        items,
        object : OrgUnitSelectorActions {
            override val onSearch: (String) -> Unit
                get() = { }
            override val onOrgUnitChecked: (orgUnitUid: String, isChecked: Boolean) -> Unit
                get() = { _, _ -> }
            override val onOpenOrgUnit: (orgUnitUid: String) -> Unit
                get() = { }
            override val onDoneClick: () -> Unit
                get() = { }
            override val onCancelClick: () -> Unit
                get() = { }
            override val onClearClick: () -> Unit
                get() = { }
        },
    )
}

@Composable
fun OrgUnitSelectorItem(
    modifier: Modifier = Modifier,
    higherLevel: Int,
    orgUnitItem: OrgUnitTreeItem,
    onOpenOrgUnit: (orgUnitUid: String) -> Unit,
    onOrgUnitChecked: (orgUnitUid: String, checked: Boolean) -> Unit,
) {
    Row(
        modifier = modifier
            .testTag("$ITEM_TEST_TAG${orgUnitItem.label}")
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .background(Color.White)
            .clickable(
                enabled = orgUnitItem.hasChildren,
                interactionSource = remember {
                    MutableInteractionSource()
                },
                indication = rememberRipple(bounded = true),
            ) {
                onOpenOrgUnit(orgUnitItem.uid)
            }
            .padding(start = ((orgUnitItem.level - higherLevel + 1) * 16).dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = spacedBy(8.dp),
    ) {
        Icon(
            imageVector = when {
                !orgUnitItem.hasChildren ->
                    ImageVector.vectorResource(id = R.drawable.ic_tree_node_default)
                orgUnitItem.isOpen ->
                    ImageVector.vectorResource(id = R.drawable.ic_tree_node_close)
                else ->
                    ImageVector.vectorResource(id = R.drawable.ic_tree_node_open)
            },
            tint = MaterialTheme.colorScheme.primary,
            contentDescription = "",
        )
        Text(
            modifier = Modifier.weight(1f),
            text = orgUnitItem.formattedLabel(),
            style = TextStyle(
                color = Color(0xDE333333),
                fontSize = 12.sp,
                fontFamily = defaultFontFamily,
                lineHeight = 16.sp,
                fontWeight = if (orgUnitItem.selectedChildrenCount > 0) {
                    FontWeight.Bold
                } else {
                    FontWeight.Normal
                },
            ),
        )
        if (orgUnitItem.canBeSelected) {
            Checkbox(
                modifier = Modifier.testTag("$ITEM_CHECK_TEST_TAG${orgUnitItem.label}"),
                checked = orgUnitItem.selected,
                onCheckedChange = { isChecked ->
                    onOrgUnitChecked(orgUnitItem.uid, isChecked)
                },
            )
        }
    }
}

const val DONE_TEST_TAG = "ORG_UNIT_DIALOG_DONE"
const val CANCEL_TEST_TAG = "ORG_UNIT_DIALOG_CANCEL"
const val CLEAR_TEST_TAG = "ORG_UNIT_DIALOG_CLEAR"
const val SEARCH_TEST_TAG = "ORG_UNIT_DIALOG_SEARCH"
const val ITEM_LIST_TEST_TAG = "ORG_UNIT_ITEM_LIST"
const val ITEM_TEST_TAG = "ORG_UNIT_DIALOG_ITEM_"
const val ITEM_CHECK_TEST_TAG = "ORG_UNIT_DIALOG_ITEM_CHECK_"
