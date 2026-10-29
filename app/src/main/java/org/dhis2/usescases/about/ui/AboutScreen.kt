package org.dhis2.usescases.about.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dhis2.R
import org.dhis2.usescases.about.AboutViewModel
import org.dhis2.usescases.about.model.AboutInfo
import org.dhis2.usescases.about.model.AboutLink
import org.dhis2.usescases.about.ui.state.AboutUiEvent
import org.dhis2.usescases.about.ui.state.AboutUiState
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import org.hisp.dhis.mobile.ui.designsystem.theme.Outline
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import org.hisp.dhis.mobile.ui.designsystem.theme.dropShadow

const val ABOUT_VERSION_VALUE_TAG = "ABOUT_VERSION_VALUE_TAG"
const val ABOUT_SDK_VALUE_TAG = "ABOUT_SDK_VALUE_TAG"
const val ABOUT_SERVER_VALUE_TAG = "ABOUT_SERVER_VALUE_TAG"
const val ABOUT_USER_VALUE_TAG = "ABOUT_USER_VALUE_TAG"

fun aboutLinkTag(link: AboutLink) = "ABOUT_LINK_${link.name}_TAG"

private val SectionHeaderTopPadding = Spacing.Spacing18
private val FirstSectionHeaderTopPadding = 20.dp
private val ValueRowVerticalPadding = 9.dp
private val LinkRowVerticalPadding = 13.dp
private val LinkRowMinHeight = 52.dp
private val LinkIconSize = 18.dp
private val ParagraphLineHeight = 21.sp

@Composable
fun AboutScreen(
    viewModel: AboutViewModel,
    onOpenLink: (AboutLink) -> Unit,
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val noConnectionMessage = stringResource(R.string.about_no_connection)

    LaunchedEffect(viewModel.uiEvent) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AboutUiEvent.OpenLink -> onOpenLink(event.link)
                AboutUiEvent.ShowNoConnection -> snackbarHostState.showSnackbar(noConnectionMessage)
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SurfaceColor.SurfaceBright,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier.dropShadow(shape = SnackbarDefaults.shape),
                    snackbarData = data,
                )
            }
        },
    ) { paddingValues ->
        AboutContent(
            uiState = uiState,
            contentPadding = paddingValues,
            onLinkClick = viewModel::onLinkClicked,
        )
    }
}

@Composable
internal fun AboutContent(
    uiState: AboutUiState,
    contentPadding: PaddingValues,
    onLinkClick: (AboutLink) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(SurfaceColor.SurfaceBright)
                .padding(contentPadding)
                .verticalScroll(rememberScrollState()),
    ) {
        ThisInstallSection(uiState.info)
        HorizontalDivider(thickness = Spacing.Spacing1, color = Outline.Light)
        AboutThisAppSection(onLinkClick)
        HorizontalDivider(thickness = Spacing.Spacing1, color = Outline.Light)
        LegalAndCreditsSection()
    }
}

@Composable
private fun ThisInstallSection(info: AboutInfo?) {
    SectionHeader(
        text = stringResource(R.string.about_section_this_install),
        topPadding = FirstSectionHeaderTopPadding,
    )
    ValueRow(
        label = stringResource(R.string.about_label_version),
        value = versionValue(info),
        valueTestTag = ABOUT_VERSION_VALUE_TAG,
    )
    ValueRow(
        label = stringResource(R.string.about_label_sdk),
        value = AnnotatedString(info?.sdkVersion.orEmpty()),
        valueTestTag = ABOUT_SDK_VALUE_TAG,
    )
    ValueRow(
        label = stringResource(R.string.about_label_server),
        value = AnnotatedString(info?.serverUrl.orEmpty()),
        valueTestTag = ABOUT_SERVER_VALUE_TAG,
    )
    ValueRow(
        label = stringResource(R.string.about_label_user),
        value = AnnotatedString(info?.username.orEmpty()),
        valueTestTag = ABOUT_USER_VALUE_TAG,
        bottomPadding = Spacing.Spacing18,
    )
}

@Composable
private fun AboutThisAppSection(onLinkClick: (AboutLink) -> Unit) {
    SectionHeader(
        text = stringResource(R.string.about_section_about_this_app),
        topPadding = SectionHeaderTopPadding,
    )
    Text(
        text = stringResource(R.string.about_description),
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    start = Spacing.Spacing16,
                    end = Spacing.Spacing16,
                    bottom = Spacing.Spacing16,
                ),
        style = MaterialTheme.typography.bodyMedium.copy(lineHeight = ParagraphLineHeight),
        color = TextColor.OnSurfaceVariant,
    )
    LinkRow(
        label = stringResource(R.string.about_supported_features),
        link = AboutLink.SUPPORTED_FEATURES,
        onClick = onLinkClick,
    )
    LinkRow(
        label = stringResource(R.string.about_privacy_policy),
        link = AboutLink.PRIVACY_POLICY,
        onClick = onLinkClick,
    )
    LinkRow(
        label = stringResource(R.string.about_source_code),
        link = AboutLink.SOURCE_CODE,
        onClick = onLinkClick,
        bottomPadding = Spacing.Spacing18,
    )
}

@Composable
private fun LegalAndCreditsSection() {
    SectionHeader(
        text = stringResource(R.string.about_section_legal_and_credits),
        topPadding = SectionHeaderTopPadding,
    )
    ValueRow(
        label = stringResource(R.string.about_label_licence),
        value = AnnotatedString(stringResource(R.string.about_licence_value)),
    )
    ValueRow(
        label = stringResource(R.string.about_label_developed_by),
        value = AnnotatedString(stringResource(R.string.about_developed_by_value)),
    )
    ValueRow(
        label = stringResource(R.string.about_label_questions),
        value = AnnotatedString(stringResource(R.string.about_questions_value)),
        bottomPadding = Spacing.Spacing24,
    )
}

/** Version and build hash share a line, the hash dimmed so the version reads first. */
@Composable
private fun versionValue(info: AboutInfo?): AnnotatedString =
    buildAnnotatedString {
        append(info?.version.orEmpty())
        info?.buildHash?.let { hash ->
            append(" ")
            withStyle(SpanStyle(color = TextColor.OnSurfaceLight)) {
                append("($hash)")
            }
        }
    }

@Composable
private fun SectionHeader(
    text: String,
    topPadding: Dp,
) {
    Text(
        text = text,
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    start = Spacing.Spacing16,
                    end = Spacing.Spacing16,
                    top = topPadding,
                    bottom = Spacing.Spacing6,
                ),
        style = MaterialTheme.typography.labelMedium,
        color = TextColor.OnPrimaryContainer,
    )
}

@Composable
private fun ValueRow(
    label: String,
    value: AnnotatedString,
    valueTestTag: String? = null,
    bottomPadding: Dp = ValueRowVerticalPadding,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(
                    start = Spacing.Spacing16,
                    end = Spacing.Spacing16,
                    top = ValueRowVerticalPadding,
                    bottom = bottomPadding,
                ),
        horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing16),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextColor.OnSurfaceLight,
        )
        Text(
            text = value,
            modifier =
                Modifier
                    .weight(1f)
                    .let { if (valueTestTag != null) it.testTag(valueTestTag) else it },
            style = MaterialTheme.typography.bodyMedium,
            color = TextColor.OnSurface,
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun LinkRow(
    label: String,
    link: AboutLink,
    onClick: (AboutLink) -> Unit,
    bottomPadding: Dp = LinkRowVerticalPadding,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .heightIn(min = LinkRowMinHeight)
                .testTag(aboutLinkTag(link))
                .clickable { onClick(link) }
                .padding(
                    start = Spacing.Spacing16,
                    end = Spacing.Spacing16,
                    top = LinkRowVerticalPadding,
                    bottom = bottomPadding,
                ),
        horizontalArrangement = Arrangement.spacedBy(Spacing.Spacing12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = TextColor.OnPrimaryContainer,
        )
        if (link.isExternal) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                contentDescription = stringResource(R.string.about_opens_in_browser),
                modifier = Modifier.size(LinkIconSize),
                tint = TextColor.OnPrimaryContainer,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AboutContentPreview() {
    DHIS2Theme {
        AboutContent(
            uiState =
                AboutUiState(
                    info =
                        AboutInfo(
                            version = "3.5.0",
                            buildHash = "2c28d35",
                            sdkVersion = "1.15.0",
                            serverUrl = "play.im.dhis2.org/stable-2-43-1",
                            username = "android",
                        ),
                ),
            contentPadding = PaddingValues(),
            onLinkClick = {},
        )
    }
}
