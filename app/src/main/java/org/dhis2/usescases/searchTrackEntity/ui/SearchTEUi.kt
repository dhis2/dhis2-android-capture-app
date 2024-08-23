package org.dhis2.usescases.searchTrackEntity.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dhis2.R
import org.dhis2.commons.filters.workingLists.WorkingListChipGroup
import org.dhis2.commons.filters.workingLists.WorkingListViewModel
import org.dhis2.commons.resources.ColorType
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.usescases.searchTrackEntity.listView.SearchResult
import org.hisp.dhis.mobile.ui.designsystem.component.ExtendedFAB
import org.hisp.dhis.mobile.ui.designsystem.component.FAB
import org.hisp.dhis.mobile.ui.designsystem.component.FABStyle
import org.hisp.dhis.mobile.ui.designsystem.component.SearchBar
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2TextStyle
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor
import org.hisp.dhis.mobile.ui.designsystem.theme.TextColor
import org.hisp.dhis.mobile.ui.designsystem.theme.getTextStyle

@Composable
fun SearchResultUi(searchResult: SearchResult, onSearchOutsideClick: () -> Unit) {
    when (searchResult.type) {
        SearchResult.SearchResultType.LOADING ->
            LoadingContent(
                loadingDescription = stringResource(R.string.search_loading_more),
            )

        SearchResult.SearchResultType.SEARCH_OUTSIDE -> SearchOutsideProgram(
            resultText = stringResource(R.string.search_no_results_in_program)
                .format(searchResult.extraData!!),
            buttonText = stringResource(R.string.search_outside_action),
            onSearchOutsideClick = onSearchOutsideClick,
        )

        SearchResult.SearchResultType.NO_MORE_RESULTS -> NoMoreResults()
        SearchResult.SearchResultType.TOO_MANY_RESULTS -> TooManyResults()
        SearchResult.SearchResultType.NO_RESULTS -> NoResults()
        SearchResult.SearchResultType.SEARCH_OR_CREATE -> SearchOrCreate(searchResult.extraData!!)
        SearchResult.SearchResultType.SEARCH -> InitSearch(searchResult.extraData!!)
        SearchResult.SearchResultType.NO_MORE_RESULTS_OFFLINE -> NoMoreResults(
            message = stringResource(id = R.string.search_no_more_results_offline),
        )

        SearchResult.SearchResultType.UNABLE_SEARCH_OUTSIDE -> UnableToSearchOutside(
            uiData = searchResult.uiData as UnableToSearchOutsideData,
        )
    }
}

@Composable
fun SearchButton(
    teTypeName: String,
    modifier: Modifier = Modifier,
    createButtonVisible: Boolean = true,
    onClick: () -> Unit,
) {
    val textId = if (createButtonVisible) {
        R.string.search_te_type
    } else {
        R.string.search_add_new_te_type
    }

    OutlinedButton(
        modifier = Modifier
            .requiredHeight(56.dp)
            .then(modifier),
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = SurfaceColor.Primary,
        ),
        border = BorderStroke(1.dp, SurfaceColor.Primary),
        shape = RoundedCornerShape(Spacing.Spacing16),
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_search),
            contentDescription = "",
            tint = SurfaceColor.Primary,
        )

        Spacer(modifier = Modifier.requiredWidth(Spacing.Spacing8))

        Text(
            text = stringResource(id = textId, teTypeName.lowercase()),
            style = getTextStyle(style = DHIS2TextStyle.LABEL_LARGE),
        )
    }
}

@Composable
fun AddNewButton(
    teTypeName: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .requiredHeight(44.dp)
            .then(modifier),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = SurfaceColor.PrimaryContainer,
            contentColor = TextColor.OnPrimaryContainer,
        ),
        shape = RoundedCornerShape(Spacing.Spacing16),
        elevation = ButtonDefaults.elevation(
            defaultElevation = 0.dp,
        ),
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_add_primary),
            contentDescription = "",
            tint = SurfaceColor.Primary,
        )

        Spacer(modifier = Modifier.requiredWidth(Spacing.Spacing8))

        Text(
            text = stringResource(id = R.string.add_te_type, teTypeName.lowercase()),
            style = getTextStyle(style = DHIS2TextStyle.LABEL_LARGE),
            color = SurfaceColor.Primary,
        )
    }
}

@Composable
fun SearchButtonWithQuery(
    modifier: Modifier = Modifier,
    queryData: Map<String, String> = emptyMap(),
    onClick: () -> Unit,
    onClearSearchQuery: () -> Unit,
) {
    Box(modifier) {
        SearchBar(
            text = queryData.values.joinToString(separator = ", "),
            modifier = Modifier.fillMaxWidth(),
            onQueryChange = {
                if (it.isBlank()) onClearSearchQuery()
            },
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(end = 48.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.Unspecified)
                .clickable(
                    onClick = onClick,
                    interactionSource = MutableInteractionSource(),
                    indication = rememberRipple(
                        true,
                        color = SurfaceColor.Primary,
                    ),
                ),
        )
    }
}

@Composable
fun WrappedSearchButton(
    teTypeName: String,
    onClick: () -> Unit,
) {
    SearchButton(
        modifier = Modifier.wrapContentWidth(align = Alignment.CenterHorizontally),
        onClick = onClick,
        teTypeName = teTypeName,
    )
}

@ExperimentalAnimationApi
@Composable
fun FullSearchButtonAndWorkingList(
    teTypeName: String,
    modifier: Modifier,
    createButtonVisible: Boolean = false,
    closeFilterVisibility: Boolean = false,
    isLandscape: Boolean = false,
    queryData: Map<String, String> = emptyMap(),
    onSearchClick: () -> Unit = {},
    onEnrollClick: () -> Unit = {},
    onCloseFilters: () -> Unit = {},
    onClearSearchQuery: () -> Unit = {},
    workingListViewModel: WorkingListViewModel? = null,
    shouldShowCreateButton: Boolean = true
) {
    Column(modifier = modifier) {
        if (!isLandscape || queryData.isNotEmpty()) {
            Row(
                modifier = Modifier.padding(
                    top = Spacing.Spacing16,
                    start = Spacing.Spacing16,
                    end = Spacing.Spacing16,
                    bottom = Spacing.Spacing0,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (shouldShowCreateButton) {
                    Column(
                        modifier = Modifier
                            .wrapContentHeight()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(Spacing.Spacing8),
                    ) {
                        if (queryData.isNotEmpty()) {
                            SearchButtonWithQuery(
                                modifier = Modifier.fillMaxWidth(),
                                queryData = queryData,
                                onClick = onSearchClick,
                                onClearSearchQuery = onClearSearchQuery,
                            )
                        } else {
                            SearchAndCreateTEIButton(
                                onSearchClick = onSearchClick,
                                teTypeName = teTypeName,
                                createButtonVisible = createButtonVisible,
                                onEnrollClick = onEnrollClick,
                            )
                        }
                    }
                }

                if (closeFilterVisibility) {
                    Spacer(modifier = Modifier.size(16.dp))
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .shadow(2.dp, CircleShape, clip = false)
                            .clip(CircleShape)
                            .background(Color.White),
                    ) {
                        IconButton(onClick = onCloseFilters) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_arrow_up),
                                contentDescription = "",
                                tint = Color(
                                    ColorUtils().getPrimaryColor(
                                        LocalContext.current,
                                        ColorType.PRIMARY,
                                    ),
                                ),
                            )
                        }
                    }
                }
            }
        }

//        Spacer(modifier = Modifier.requiredHeight(Spacing.Spacing16))

        workingListViewModel?.let {
            WorkingListChipGroup(workingListViewModel = it)
        }
    }
}

@Composable
private fun SearchAndCreateTEIButton(
    onSearchClick: () -> Unit,
    teTypeName: String,
    createButtonVisible: Boolean,
    onEnrollClick: () -> Unit,
) {
    SearchButton(
        modifier = Modifier.fillMaxWidth(),
        onClick = onSearchClick,
        teTypeName = teTypeName,
        createButtonVisible = createButtonVisible,
    )

    if (createButtonVisible) {
        AddNewButton(
            modifier = Modifier.fillMaxWidth(),
            teTypeName = teTypeName,
            onClick = onEnrollClick,
        )
    }
}

@Composable
fun LoadingContent(loadingDescription: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = loadingDescription,
            fontSize = 14.sp,
            color = Color.Black.copy(alpha = 0.38f),
            style = LocalTextStyle.current.copy(
                lineHeight = 10.sp,
                fontFamily = FontFamily(Font(R.font.rubik_regular)),
            ),
        )
    }
}

@Composable
fun SearchOutsideProgram(resultText: String, buttonText: String, onSearchOutsideClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = resultText,
            textAlign = TextAlign.Center,
            fontSize = 14.sp,
            color = Color.Black.copy(alpha = 0.38f),
            style = LocalTextStyle.current.copy(
                fontFamily = FontFamily(Font(R.font.rubik_regular)),
            ),
        )
        Spacer(modifier = Modifier.size(16.dp))
        Button(
            onClick = onSearchOutsideClick,
            border = BorderStroke(
                1.dp,
                Color(
                    ColorUtils().getPrimaryColor(
                        LocalContext.current,
                        ColorType.PRIMARY,
                    ),
                ),
            ),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = colorResource(id = R.color.white),
            ),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = "",
                tint = Color(
                    ColorUtils().getPrimaryColor(
                        LocalContext.current,
                        ColorType.PRIMARY,
                    ),
                ),
            )
            Spacer(modifier = Modifier.size(16.dp))
            Text(
                text = buttonText,
                color = Color(
                    ColorUtils().getPrimaryColor(
                        LocalContext.current,
                        ColorType.PRIMARY,
                    ),
                ),
            )
        }
    }
}

@Composable
fun NoMoreResults(message: String = stringResource(R.string.string_no_more_results)) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 96.dp, 16.dp, 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
            fontSize = 14.sp,
            color = Color.Black.copy(alpha = 0.38f),
            style = LocalTextStyle.current.copy(
                lineHeight = 10.sp,
                fontFamily = FontFamily(Font(R.font.rubik_regular)),
            ),
        )
    }
}

@Composable
fun UnableToSearchOutside(uiData: UnableToSearchOutsideData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.search_unable_search_outside)
                .format(uiData.trackedEntityTypeName.toLowerCase(Locale.current)),
            fontSize = 14.sp,
            color = Color.Black.copy(alpha = 0.38f),
            style = LocalTextStyle.current.copy(
                fontFamily = FontFamily(Font(R.font.rubik_regular)),
            ),
        )
        Spacer(modifier = Modifier.size(16.dp))
        uiData.trackedEntityTypeAttributes.forEach {
            AttributeField(it)
            Spacer(modifier = Modifier.size(4.dp))
        }
    }
}

@Composable
fun AttributeField(fieldName: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Spacer(modifier = Modifier.size(6.dp))
            Icon(
                modifier = Modifier.size(5.dp),
                painter = painterResource(id = R.drawable.ic_circle),
                contentDescription = "",
                tint = Color(
                    ColorUtils().getPrimaryColor(
                        LocalContext.current,
                        ColorType.PRIMARY,
                    ),
                ),
            )
        }
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = fieldName,
            color = colorResource(id = R.color.textSecondary),
            fontSize = 14.sp,
            style = LocalTextStyle.current.copy(
                fontFamily = FontFamily(Font(R.font.rubik_regular)),
            ),
        )
    }
}

@Composable
fun NoResults() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_empty_folder),
            contentDescription = "",
        )
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = stringResource(R.string.search_no_results),
            fontSize = 17.sp,
            color = Color.Black.copy(alpha = 0.38f),
            style = LocalTextStyle.current.copy(
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.rubik_regular)),
            ),
        )
    }
}

@Composable
fun TooManyResults() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_too_many),
            contentDescription = "",
        )
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = stringResource(R.string.search_too_many_results),
            fontSize = 17.sp,
            color = colorResource(id = R.color.pink_500),
            textAlign = TextAlign.Center,
            style = LocalTextStyle.current.copy(
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.rubik_regular)),
            ),
        )
        Text(
            text = stringResource(R.string.search_too_many_results_message),
            fontSize = 17.sp,
            color = Color.Black.copy(alpha = 0.38f),
            style = LocalTextStyle.current.copy(
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.rubik_regular)),
            ),
        )
    }
}

@Composable
fun SearchOrCreate(teTypeName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            imageVector = ImageVector.vectorResource(id = R.drawable.ic_searchvscreate),
            contentDescription = "",
        )
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = stringResource(R.string.search_or_create).format(teTypeName),
            fontSize = 17.sp,
            color = Color.Black.copy(alpha = 0.38f),
            style = LocalTextStyle.current.copy(
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.rubik_regular)),
            ),
        )
    }
}

@Composable
fun InitSearch(teTypeName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.init_search).format(teTypeName),
            fontSize = 17.sp,
            color = Color.Black.copy(alpha = 0.38f),
            style = LocalTextStyle.current.copy(
                lineHeight = 24.sp,
                fontFamily = FontFamily(Font(R.font.rubik_regular)),
            ),
        )
    }
}

@ExperimentalAnimationApi
@Composable
fun CreateNewButton(
    teTypeName: String,
    modifier: Modifier = Modifier,
    extended: Boolean = true,
    onClick: () -> Unit,
) {
    val icon = @Composable {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.ic_add_accent),
            contentDescription = "",
            tint = TextColor.OnPrimaryContainer,
        )
    }

    AnimatedContent(
        targetState = extended,
        label = "FAB_Expansion",
    ) {
        if (it) {
            ExtendedFAB(
                modifier = modifier,
                onClick = onClick,
                text = stringResource(R.string.search_new_te_type, teTypeName.lowercase()),
                icon = icon,
                style = FABStyle.SECONDARY,
            )
        } else {
            FAB(
                modifier = modifier,
                onClick = onClick,
                icon = icon,
                style = FABStyle.SECONDARY,
            )
        }
    }
}

@ExperimentalAnimationApi
@Preview(showBackground = true, backgroundColor = 0xFFF)
@Composable
fun SearchFullWidthPreview() {
    FullSearchButtonAndWorkingList(teTypeName = "Person", modifier = Modifier)
}

@Preview(showBackground = true, backgroundColor = 0xFFF)
@Composable
fun SearchWrapWidthPreview() {
    WrappedSearchButton(teTypeName = "Person") {
    }
}

@ExperimentalAnimationApi
@Preview
@Composable
fun ExtendedCreateNewButtonPreview() {
    CreateNewButton(
        teTypeName = "Patient",
        extended = true,
    ) {}
}

@ExperimentalAnimationApi
@Preview
@Composable
fun CreateNewButtonPreview() {
    CreateNewButton(
        teTypeName = "Patient",
        extended = false,
    ) {}
}

@Preview(showBackground = true)
@Composable
fun LoadingMoreResultsPreview() {
    LoadingContent(loadingDescription = "Loading more results...")
}

@Preview(showBackground = true)
@Composable
fun SearchOutsidePreview() {
    SearchResultUi(searchResult = SearchResult(SearchResult.SearchResultType.SEARCH_OUTSIDE)) {}
}

@Preview(showBackground = true)
@Composable
fun NoMoreResultsPreview() {
    SearchResultUi(searchResult = SearchResult(SearchResult.SearchResultType.NO_MORE_RESULTS)) {}
}

@Preview(showBackground = true)
@Composable
fun TooManyResultsPreview() {
    SearchResultUi(searchResult = SearchResult(SearchResult.SearchResultType.TOO_MANY_RESULTS)) {}
}

@Preview(showBackground = true)
@Composable
fun NoResultsPreview() {
    SearchResultUi(searchResult = SearchResult(SearchResult.SearchResultType.NO_RESULTS)) {}
}

@Preview(showBackground = true)
@Composable
fun SearchOrCreatePreview() {
    SearchResultUi(
        searchResult = SearchResult(
            SearchResult.SearchResultType.SEARCH_OR_CREATE,
            "Person",
        ),
    ) {}
}

@Preview(showBackground = true)
@Composable
fun FieldItemPreview() {
    UnableToSearchOutside(
        UnableToSearchOutsideData(
            listOf(
                "Last Name",
                "First Name",
                "Unique Id",
            ),
            "Person",
        ),
    )
}
