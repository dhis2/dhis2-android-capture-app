package org.dhis2.usescases.login.accounts.ui

import android.view.View
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import org.dhis2.bindings.buildInfo
import org.dhis2.R
import org.dhis2.usescases.login.accounts.AccountModel

@ExperimentalMaterialApi
@Composable
fun AccountsScreen(
    accounts: List<AccountModel>,
    onAccountClicked: (AccountModel) -> Unit,
    onAddAccountClicked: () -> Unit,
) {
    MaterialTheme {
        Column(
            Modifier
                .fillMaxWidth()
                .background(colorResource(id = R.color.colorPrimary)),
        ) {
            LoginHeader()
            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(Color.White),
            ) {
                LazyColumn(Modifier.weight(1f).padding(top = 16.dp)) {
                    items(accounts) {
                        AccountItem(
                            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            it,
                            onAccountClicked,
                        )
                    }
                }
                Column(Modifier.padding(16.dp)) {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = colorResource(id = R.color.colorPrimary),
                            contentColor = Color.White,
                        ),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = 5.dp,
                            pressedElevation = 15.dp,
                            disabledElevation = 0.dp,
                        ),
                        onClick = { onAddAccountClicked() },
                    ) {
                        Text(
                            text = stringResource(R.string.add_accout).toUpperCase(Locale.current),
                            fontFamily = FontFamily(
                                Font(R.font.rubik_regular, FontWeight.Medium),
                            ),
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LoginHeader() {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp, 8.dp, 16.dp, 12.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            AndroidView(factory = { View.inflate(it, R.layout.dhis_logo, null) })
        }
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = LocalContext.current.buildInfo(),
                color = colorResource(id = R.color.colorAccentAlpha),
                fontSize = 9.sp,
            )
        }
    }
}

@ExperimentalMaterialApi
@Preview(showBackground = true)
@Composable
fun AccountsPreview() {
    val accounts = mutableListOf<AccountModel>().apply {
        repeat(13) {
            add(AccountModel("android", "https://play.dhis2.com/android-dev"))
        }
    }
    AccountsScreen(
        accounts,
        {},
        {},
    )
}

@ExperimentalMaterialApi
@Preview(showBackground = true)
@Composable
fun FewAccountsPreview() {
    val accounts = mutableListOf<AccountModel>().apply {
        repeat(3) {
            add(AccountModel("android", "https://play.dhis2.com/android-dev"))
        }
    }
    AccountsScreen(
        accounts,
        {},
        {},
    )
}
