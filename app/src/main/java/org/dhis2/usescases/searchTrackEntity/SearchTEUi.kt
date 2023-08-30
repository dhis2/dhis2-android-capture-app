package org.dhis2.usescases.searchTrackEntity

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import org.dhis2.R
import org.dhis2.usescases.searchTrackEntity.listView.SearchResult

@Composable
fun SearchResult(
    searchResultType: SearchResult.SearchResultType,
    onSearchOutsideClick: () -> Unit,
) {
    when (searchResultType) {
        SearchResult.SearchResultType.LOADING ->
            LoadingContent(
                loadingDescription = stringResource(R.string.search_loading_more),
            )
        SearchResult.SearchResultType.SEARCH_OUTSIDE -> SearchOutsideProgram(
            resultText = stringResource(R.string.search_no_results_in_program),
            buttonText = stringResource(R.string.search_outside_action),
            onSearchOutsideClick = onSearchOutsideClick,
        )
        SearchResult.SearchResultType.NO_MORE_RESULTS -> NoMoreResults()
        SearchResult.SearchResultType.TOO_MANY_RESULTS -> TooManyResults()
        SearchResult.SearchResultType.NO_RESULTS -> NoResults()
        SearchResult.SearchResultType.SEARCH_OR_CREATE -> SearchOrCreate()
        else -> {
            // Nothing to do in these cases
        }
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
                tint = colorResource(id = R.color.colorPrimary),
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
fun FullSearchButton(modifier: Modifier, visible: Boolean = true, onClick: () -> Unit) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = slideInVertically(),
        exit = slideOutVertically(),
    ) {
        SearchButton(
            modifier = Modifier.fillMaxWidth()
                .height(48.dp),
            onClick = onClick,
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
            fontSize = 14.sp,
            color = Color.Black.copy(alpha = 0.38f),
            style = LocalTextStyle.current.copy(
                lineHeight = 10.sp,
                fontFamily = FontFamily(Font(R.font.rubik_regular)),
            ),
        )
        Spacer(modifier = Modifier.size(16.dp))
        Button(
            onClick = onSearchOutsideClick,
            border = BorderStroke(1.dp, colorResource(id = R.color.colorPrimary)),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = colorResource(id = R.color.white),
            ),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = "",
                tint = colorResource(id = R.color.colorPrimary),
            )
            Spacer(modifier = Modifier.size(16.dp))
            Text(text = buttonText, color = colorResource(id = R.color.colorPrimary))
        }
    }
}

@Composable
fun NoMoreResults() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.string_no_more_results),
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
fun SearchOrCreate() {
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
            text = stringResource(R.string.search_or_create),
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
            .wrapContentWidth()
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
            tint = colorResource(id = R.color.colorPrimary),
        )
        AnimatedVisibility(visible = extended) {
            Row {
                Spacer(modifier = Modifier.size(12.dp))
                Text(
                    text = stringResource(R.string.search_create_new),
                    color = colorResource(id = R.color.colorPrimary),
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

@SuppressLint("CoroutineCreationDuringComposition")
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
    FullSearchButton(modifier = Modifier) {
    }
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
    SearchResult(searchResultType = SearchResult.SearchResultType.SEARCH_OUTSIDE) {}
}

@Preview(showBackground = true)
@Composable
fun NoMoreResultsPreview() {
    SearchResult(searchResultType = SearchResult.SearchResultType.NO_MORE_RESULTS) {}
}

@Preview(showBackground = true)
@Composable
fun TooManyResultsPreview() {
    SearchResult(searchResultType = SearchResult.SearchResultType.TOO_MANY_RESULTS) {}
}

@Preview(showBackground = true)
@Composable
fun NoResultsPreview() {
    SearchResult(searchResultType = SearchResult.SearchResultType.NO_RESULTS) {}
}

@Preview(showBackground = true)
@Composable
fun SearchOrCreatePreview() {
    SearchResult(searchResultType = SearchResult.SearchResultType.SEARCH_OR_CREATE) {}
}
