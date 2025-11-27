package org.dhis2.mobile.login.accounts.ui.screen

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.dhis2.mobile.commons.resources.getDrawableResource
import org.dhis2.mobile.login.accounts.domain.model.AccountModel
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.Avatar
import org.hisp.dhis.mobile.ui.designsystem.component.AvatarStyleData
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardDescriptionModel
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberAdditionalInfoColumnState
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberListCardState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun AccountItem(
    modifier: Modifier = Modifier,
    account: AccountModel,
    onItemClicked: (AccountModel) -> Unit,
) {
    val listCardState =
        rememberListCardState(
            title =
                ListCardTitleModel(
                    text = account.serverName,
                ),
            description =
                ListCardDescriptionModel(
                    text = account.serverUrl,
                ),
            additionalInfoColumnState =
                rememberAdditionalInfoColumnState(
                    additionalInfoList =
                        listOf(
                            AdditionalInfoItem(
                                icon = {
                                    Icon(
                                        imageVector = Icons.Outlined.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurface,
                                    )
                                },
                                value = account.name,
                            ),
                        ),
                    syncProgressItem = AdditionalInfoItem(value = ""),
                ),
        )

    val flag = account.serverFlag?.let { getDrawableResource(it) }

    ListCard(
        modifier =
            modifier
                .fillMaxWidth(),
        listCardState = listCardState,
        onCardClick = { onItemClicked(account) },
        listAvatar = {
            Avatar(
                style =
                    flag?.let { painter ->
                        AvatarStyleData.Image(painter)
                    } ?: run {
                        AvatarStyleData.Text(account.serverName.first().toString())
                    },
                onImageClick = { onItemClicked(account) },
            )
        },
    )
}
