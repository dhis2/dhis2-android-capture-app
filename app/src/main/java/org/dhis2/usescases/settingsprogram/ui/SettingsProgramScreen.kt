package org.dhis2.usescases.settingsprogram.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import org.dhis2.R
import org.dhis2.usescases.settingsprogram.SettingsProgramViewModel
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.component.ListCard
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardDescriptionModel
import org.hisp.dhis.mobile.ui.designsystem.component.ListCardTitleModel
import org.hisp.dhis.mobile.ui.designsystem.component.MetadataAvatar
import org.hisp.dhis.mobile.ui.designsystem.component.MetadataAvatarSize
import org.hisp.dhis.mobile.ui.designsystem.component.MetadataIcon
import org.hisp.dhis.mobile.ui.designsystem.component.TopBar
import org.hisp.dhis.mobile.ui.designsystem.component.TopBarActionIcon
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberAdditionalInfoColumnState
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberListCardState
import org.hisp.dhis.mobile.ui.designsystem.resource.provideDHIS2Icon
import org.hisp.dhis.mobile.ui.designsystem.theme.Radius
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsProgramScreen(
    settingsViewModel: SettingsProgramViewModel = koinViewModel(),
    onBack: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        containerColor = MaterialTheme.colorScheme.primary,
        topBar = {
            TopBar(
                navigationIcon = {
                    TopBarActionIcon(
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        contentDescription = "back arrow",
                        onClick = onBack,
                    )
                },
                actions = { },
                title = {
                    Text(
                        text = stringResource(R.string.activity_program_settings),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                colors =
                    TopAppBarDefaults.topAppBarColors().copy(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
            )
        },
    ) { paddingValues ->
        val settings by settingsViewModel.programSettings.collectAsState(emptyList())

        LazyColumn(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .clip(
                        shape =
                            RoundedCornerShape(
                                topStart = Radius.L,
                                topEnd = Radius.L,
                            ),
                    ).background(color = MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(Spacing.Spacing16),
            verticalArrangement = spacedBy(Spacing.Spacing4),
        ) {
            items(items = settings) { setting ->
                val additionalInfoColumnState =
                    rememberAdditionalInfoColumnState(
                        additionalInfoList = emptyList(),
                        syncProgressItem = AdditionalInfoItem(value = ""),
                    )
                val listCardState =
                    rememberListCardState(
                        title = ListCardTitleModel(text = setting.name ?: ""),
                        description = ListCardDescriptionModel(text = setting.description),
                        additionalInfoColumnState = additionalInfoColumnState,
                        shadow = false,
                    )
                ListCard(
                    modifier = Modifier,
                    listCardState = listCardState,
                    listAvatar = {
                        MetadataAvatar(
                            icon = {
                                if (setting.metadataIconData.isFileLoaded()) {
                                    MetadataIcon(
                                        imageCardData = setting.metadataIconData.imageCardData,
                                    )
                                } else {
                                    Icon(
                                        painter = provideDHIS2Icon("dhis2_image_not_supported"),
                                        contentDescription = "",
                                    )
                                }
                            },
                            iconTint = setting.metadataIconData.color,
                            size = MetadataAvatarSize.M(),
                        )
                    },
                    onCardClick = {},
                )
            }
        }
    }
}
