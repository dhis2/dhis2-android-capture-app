package org.dhis2.usescases.login.accounts.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dhis2.R
import org.dhis2.usescases.login.accounts.AccountModel

@Composable
fun AccountItem(
    modifier: Modifier = Modifier,
    account: AccountModel,
    onItemClicked: (AccountModel) -> Unit
) {
    Card(
        elevation = 8.dp,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClicked(account) }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colorResource(id = R.color.colorPrimary))
            ) {
                Text(
                    text = account.name.first().uppercaseChar().toString(),
                    color = Color.White,
                    fontSize = 20.sp,
                )
            }
            Column(
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = account.name,
                    color = colorResource(id = R.color.colorPrimary)
                )
                Text(
                    text = account.serverUrl,
                    color = colorResource(id = R.color.textSecondary),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Preview(showBackground = false)
@Composable
fun AccountPreview() {
    AccountItem(
        account = AccountModel("android", "https://play.dhis2.com/android-dev"),
        onItemClicked = {}
    )
}