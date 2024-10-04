package org.dhis2.usescases.teiDashboard.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dhis2.commons.R

@Composable
fun NoRelationships() {
    Column(
        modifier = Modifier
            .background(Color.White)
            .padding(42.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            modifier = Modifier
                .padding(1.dp)
                .width(139.dp)
                .height(125.dp),
            painter = painterResource(id = R.drawable.no_relationships),
            contentDescription = stringResource(id = org.dhis2.R.string.empty_relationships),
        )
        Spacer(
            modifier = Modifier
                .height(17.dp)
                .fillMaxWidth(),
        )
        Text(
            text = stringResource(id = org.dhis2.R.string.empty_relationships),
            style = TextStyle(
                fontSize = 17.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.Normal,
                color = colorResource(id = R.color.gray_990),
                textAlign = TextAlign.Center,
            ),
        )
    }
}

@Preview
@Composable
fun NoRelationshipsPreview() {
    NoRelationships()
}
