package org.dhis2.usescases.datasets.datasetDetail.datasetList.mapper

import android.content.Context
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.SyncDisabled
import androidx.compose.material.icons.outlined.SyncProblem
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.dhis2.R
import org.dhis2.commons.date.toDateSpan
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.commons.ui.model.ListCardUiModel
import org.dhis2.usescases.datasets.datasetDetail.DataSetDetailModel
import org.hisp.dhis.android.core.common.State
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItemColor
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor

class DatasetCardMapper(
    val context: Context,
    val resourceManager: ResourceManager,
) {

    fun map(
        dataset: DataSetDetailModel,
        editable: Boolean,
        onSyncIconClick: () -> Unit,
        onCardCLick: () -> Unit,
    ): ListCardUiModel {
        return ListCardUiModel(
            title = dataset.namePeriod(),
            lastUpdated = dataset.lastUpdated().toDateSpan(context),
            additionalInfo = getAdditionalInfoList(dataset, editable),
            actionButton = {
                ProvideSyncButton(
                    state = dataset.state(),
                    onSyncIconClick = onSyncIconClick,
                )
            },
            expandLabelText = resourceManager.getString(R.string.show_more),
            shrinkLabelText = resourceManager.getString(R.string.show_less),
            onCardCLick = onCardCLick,
        )
    }

    private fun getAdditionalInfoList(
        dataset: DataSetDetailModel,
        editable: Boolean,
    ): List<AdditionalInfoItem> {
        val list = mutableListOf<AdditionalInfoItem>()

        checkRegisteredIn(
            list = list,
            dataset = dataset,
        )

        checkCategoryCombination(
            list = list,
            dataset = dataset,
        )

        checkDatasetCompletion(
            list = list,
            completed = dataset.isComplete,
        )

        checkSyncStatus(
            list = list,
            state = dataset.state(),
        )

        checkViewOnly(
            list = list,
            editable = editable,
        )

        return list
    }

    private fun checkViewOnly(
        list: MutableList<AdditionalInfoItem>,
        editable: Boolean,
    ) {
        if (!editable) {
            list.add(
                AdditionalInfoItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Visibility,
                            contentDescription = resourceManager.getString(R.string.view_only),
                            tint = AdditionalInfoItemColor.DISABLED.color,
                        )
                    },
                    value = resourceManager.getString(R.string.view_only),
                    isConstantItem = true,
                    color = AdditionalInfoItemColor.DISABLED.color,
                ),
            )
        }
    }

    private fun checkCategoryCombination(
        list: MutableList<AdditionalInfoItem>,
        dataset: DataSetDetailModel,
    ) {
        if (dataset.nameCategoryOptionCombo() != "default") {
            list.add(
                AdditionalInfoItem(
                    key = "${dataset.nameCategoryOptionCombo()}:",
                    value = dataset.nameCatCombo(),
                    isConstantItem = true,
                ),
            )
        }
    }

    private fun checkSyncStatus(
        list: MutableList<AdditionalInfoItem>,
        state: State,
    ) {
        val item = when (state) {
            State.TO_POST,
            State.TO_UPDATE,
            -> {
                AdditionalInfoItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.SyncDisabled,
                            contentDescription = resourceManager.getString(R.string.not_synced),
                            tint = AdditionalInfoItemColor.DISABLED.color,
                        )
                    },
                    value = resourceManager.getString(R.string.not_synced),
                    color = AdditionalInfoItemColor.DISABLED.color,
                    isConstantItem = true,
                )
            }

            State.UPLOADING -> {
                AdditionalInfoItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Sync,
                            contentDescription = "Icon Button",
                            tint = SurfaceColor.Primary,
                        )
                    },
                    value = resourceManager.getString(R.string.syncing),
                    color = SurfaceColor.Primary,
                    isConstantItem = true,
                )
            }

            State.ERROR -> {
                AdditionalInfoItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.SyncProblem,
                            contentDescription = resourceManager.getString(R.string.sync_error_title),
                            tint = AdditionalInfoItemColor.ERROR.color,
                        )
                    },
                    value = resourceManager.getString(R.string.sync_error_title),
                    color = AdditionalInfoItemColor.ERROR.color,
                    isConstantItem = true,
                )
            }

            State.WARNING -> {
                AdditionalInfoItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.SyncProblem,
                            contentDescription = resourceManager.getString(R.string.sync_dialog_title_warning),
                            tint = AdditionalInfoItemColor.WARNING.color,
                        )
                    },
                    value = resourceManager.getString(R.string.sync_dialog_title_warning),
                    color = AdditionalInfoItemColor.WARNING.color,
                    isConstantItem = true,
                )
            }

            else -> null
        }
        item?.let { list.add(it) }
    }

    private fun checkDatasetCompletion(list: MutableList<AdditionalInfoItem>, completed: Boolean) {
        if (completed) {
            list.add(
                AdditionalInfoItem(
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = resourceManager.getString(R.string.completed),
                            tint = AdditionalInfoItemColor.SUCCESS.color,
                        )
                    },
                    value = resourceManager.getString(R.string.completed),
                    isConstantItem = true,
                    color = AdditionalInfoItemColor.SUCCESS.color,
                ),
            )
        }
    }

    private fun checkRegisteredIn(
        list: MutableList<AdditionalInfoItem>,
        dataset: DataSetDetailModel,
    ) {
        if (dataset.displayOrgUnitName()) {
            list.add(
                AdditionalInfoItem(
                    key = resourceManager.getString(R.string.registered_in),
                    value = dataset.nameOrgUnit(),
                    isConstantItem = true,
                ),
            )
        }
    }

    @Composable
    private fun ProvideSyncButton(state: State?, onSyncIconClick: () -> Unit) {
        val buttonText = when (state) {
            State.TO_POST,
            State.TO_UPDATE,
            -> {
                resourceManager.getString(R.string.sync)
            }

            State.ERROR,
            State.WARNING,
            -> {
                resourceManager.getString(R.string.sync_retry)
            }

            else -> null
        }
        buttonText?.let {
            Button(
                style = ButtonStyle.TONAL,
                text = it,
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Sync,
                        contentDescription = it,
                        tint = TextColor.OnPrimaryContainer,
                    )
                },
                onClick = { onSyncIconClick() },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
