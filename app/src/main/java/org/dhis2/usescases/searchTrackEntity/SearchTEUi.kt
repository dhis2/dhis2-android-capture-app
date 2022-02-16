package org.dhis2.usescases.searchTrackEntity

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.dhis2.R

@Composable
fun SearchResultOptions(searchResultActionData: SearchResultActionData) {

    val tooManyResults = searchResultActionData.resultAction == ResultAction.TOO_MANY_RESULTS
    val onlineAvailable = searchResultActionData.onlineAvailable
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Start,
            text = when (searchResultActionData.resultAction) {
                ResultAction.END_OF_LIST -> "Not finding the results you are looking for?"
                ResultAction.NO_RESULTS -> "Your search criteria did not return any result."
                ResultAction.TOO_MANY_RESULTS -> "Too many results"
            }
        )
        if (tooManyResults) {
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start,
                text = "Try changing search terms or searching by more attributes to narrow down results.",
                color = colorResource(id = R.color.pink_500)
            )
        }
        if (onlineAvailable && !tooManyResults) {
            Spacer(modifier = Modifier.size(16.dp))
            LoadMoreButton()
        }
        Spacer(modifier = Modifier.size(16.dp))
        ModifySearchButton()
        if (!tooManyResults) {
            Spacer(modifier = Modifier.size(16.dp))
            CreateNewButton()
        }
    }
}

@Composable
fun LoadMoreButton() {
    Button(
        onClick = { },
        border = BorderStroke(1.dp, colorResource(id = R.color.colorPrimary)),
        colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.white))
    ) {
        Text(text = "Load more", color = colorResource(id = R.color.colorPrimary))
        Spacer(modifier = Modifier.size(16.dp))
        Icon(
            painter = painterResource(id = R.drawable.ic_cloud_download),
            contentDescription = "",
            tint = colorResource(id = R.color.colorPrimary)
        )
    }
}

@Composable
fun ModifySearchButton() {
    Button(
        onClick = { },
        colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.colorPrimaryLight))
    ) {
        Text(text = "Modify search", color = colorResource(id = R.color.primaryLightBgTextColor))
        Spacer(modifier = Modifier.size(16.dp))
        Icon(
            painter = painterResource(id = R.drawable.ic_search),
            contentDescription = "",
            tint = colorResource(id = R.color.primaryLightBgTextColor)
        )
    }
}

@Composable
fun CreateNewButton() {
    Button(
        onClick = { },
        colors = ButtonDefaults.buttonColors(backgroundColor = colorResource(id = R.color.colorPrimary))
    ) {
        Text(text = "Create new", color = colorResource(id = R.color.primaryBgTextColor))
        Spacer(modifier = Modifier.size(16.dp))
        Icon(
            painter = painterResource(id = R.drawable.ic_add_accent),
            contentDescription = "",
            tint = colorResource(id = R.color.primaryBgTextColor)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LoadMorePreview() {
    SearchResultOptions(
        SearchResultActionData(
            resultAction = ResultAction.END_OF_LIST,
            true
        )
    )
}

@Preview(showBackground = true)
@Composable
fun TooManyResultsPreview() {
    SearchResultOptions(
        SearchResultActionData(
            resultAction = ResultAction.TOO_MANY_RESULTS,
            true
        )
    )
}