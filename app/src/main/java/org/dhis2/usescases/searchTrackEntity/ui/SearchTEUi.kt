package org.dhis2.usescases.searchTrackEntity.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarHost
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.dhis2.R
import org.dhis2.commons.filters.workingLists.WorkingListChipGroup
import org.dhis2.commons.filters.workingLists.WorkingListViewModel
import org.dhis2.commons.resources.ColorType
import org.dhis2.commons.resources.ColorUtils
import org.dhis2.usescases.searchTrackEntity.listView.SearchResult
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing

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
fun SearchButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        modifier = modifier,
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        elevation = ButtonDefaults.elevation(),
    ) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
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
                text = stringResource(id = R.string.search),
                color = colorResource(id = R.color.textSecondary),
            )
        }
    }
}

@Composable
fun WrappedSearchButton(onClick: () -> Unit) {
    SearchButton(
        modifier = Modifier
            .wrapContentWidth(align = Alignment.CenterHorizontally)
            .height(44.dp),
        onClick = onClick,
    )
}

@ExperimentalAnimationApi
@Composable
fun FullSearchButtonAndWorkingList(
    modifier: Modifier,
    visible: Boolean = true,
    closeFilterVisibility: Boolean = false,
    isLandscape: Boolean = false,
    onClick: () -> Unit = {},
    onCloseFilters: () -> Unit = {},
    workingListViewModel: WorkingListViewModel? = null,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = slideInVertically(initialOffsetY = { -it }),
        exit = slideOutVertically(targetOffsetY = { -it }),
    ) {
        Column {
            Row(
                modifier = Modifier.padding(
                    top = Spacing.Spacing16,
                    start = Spacing.Spacing16,
                    end = Spacing.Spacing16,
                    bottom = Spacing.Spacing8,
                ),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                SearchButton(
                    modifier = Modifier
                        .weight(weight = 1f)
                        .height(48.dp),
                    onClick = onClick,
                )
                if (!isLandscape && closeFilterVisibility) {
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
            workingListViewModel?.let {
                WorkingListChipGroup(workingListViewModel = it)
            }
        }
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
fun CreateNewButton(modifier: Modifier, extended: Boolean = true, onClick: () -> Unit) {
    Button(
        modifier = modifier
            .defaultMinSize(minWidth = 1.dp, minHeight = 1.dp)
            .apply {
                if (extended) {
                    wrapContentWidth()
                } else {
                    widthIn(56.dp)
                }
            }
            .height(56.dp),
        contentPadding = PaddingValues(16.dp),
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = ButtonDefaults.elevation(),
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = R.drawable.ic_add_accent),
            contentDescription = "",
            tint = Color(
                ColorUtils().getPrimaryColor(
                    LocalContext.current,
                    ColorType.PRIMARY,
                ),
            ),
        )
        AnimatedVisibility(visible = extended) {
            Row {
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = stringResource(R.string.search_create_new),
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
}

@Composable
fun MinAttributesMessage(minAttributes: Int) {
    val message = stringResource(R.string.search_min_attributes_message)
        .format("$minAttributes")
    var lineCount by remember { mutableStateOf(1) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Icon(
            modifier = Modifier
                .alignBy { it.measuredHeight / 2 }
                .size(9.dp),
            painter = painterResource(id = R.drawable.ic_info_outline),
            contentDescription = "",
            tint = colorResource(id = R.color.primaryBgTextColor),
        )
        Spacer(modifier = Modifier.size(9.dp))
        Text(
            modifier = Modifier
                .alignBy {
                    if (lineCount > 0) {
                        it.measuredHeight / (2 * lineCount)
                    } else {
                        it.measuredHeight / 2
                    }
                }
                .align(Alignment.CenterVertically),
            text = AnnotatedString(
                text = message,
                spanStyles = listOf(
                    AnnotatedString.Range(
                        SpanStyle(fontWeight = FontWeight.Bold),
                        start = message.indexOf("$minAttributes"),
                        end = message.indexOf("$minAttributes") + "$minAttributes".length,
                    ),
                ),
            ),
            fontSize = 12.sp,
            color = colorResource(id = R.color.primaryBgTextColor),
            style = LocalTextStyle.current.copy(
                lineHeight = 16.sp,
                fontFamily = FontFamily(Font(R.font.rubik_regular)),
            ),
            onTextLayout = {
                lineCount = it.lineCount
            },
        )
    }
}

@Composable
fun MinAttributesSnackbar(minAttributes: Int) {
    val message = stringResource(R.string.search_min_attributes_message)
        .format("$minAttributes")

    val snackState = remember { SnackbarHostState() }
    val snackScope = rememberCoroutineScope()

    SnackbarHost(
        hostState = snackState,
        snackbar = { snackBarData ->
            Snackbar(
                modifier = Modifier,
                action = {
                    TextButton(onClick = { }) {
                        Text(text = stringResource(id = R.string.button_ok))
                    }
                },
            ) {
                Text(text = snackBarData.message)
            }
        },
    )

    snackScope.launch { snackState.showSnackbar(message) }
}

@ExperimentalAnimationApi
@Preview(showBackground = true, backgroundColor = 0x2C98F0)
@Composable
fun SearchFullWidthPreview() {
    FullSearchButtonAndWorkingList(modifier = Modifier)
}

@Preview(showBackground = true, backgroundColor = 0x2C98F0)
@Composable
fun SearchWrapWidthPreview() {
    WrappedSearchButton {
    }
}

@ExperimentalAnimationApi
@Preview
@Composable
fun ExtendedCreateNewButtonPreview() {
    CreateNewButton(modifier = Modifier) {}
}

@ExperimentalAnimationApi
@Preview
@Composable
fun CreateNewButtonPreview() {
    CreateNewButton(modifier = Modifier, extended = false) {}
}

@Preview(showBackground = true, backgroundColor = 0x2C98F0)
@Composable
fun MinAttributesMessage() {
    MinAttributesMessage(2)
}

@Preview(showBackground = true, backgroundColor = 0x2C98F0)
@Composable
fun MinAttributesSnackBarPreview() {
    MinAttributesSnackbar(2)
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
