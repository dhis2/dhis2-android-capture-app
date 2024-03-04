package org.dhis2.usescases.login.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.Absolute.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import org.dhis2.R
import org.hisp.dhis.mobile.ui.designsystem.resource.provideFontResource
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

@Composable
fun LoginTopBar(
    version: String,
    displayMoreActions: Boolean = true,
    onImportDatabase: () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(MaterialTheme.colors.primary),
    ) {
        val (logoLayout, versionLabel) = createRefs()

        Row(
            Modifier
                .constrainAs(logoLayout) {
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                }
                .padding(horizontal = 4.dp),
            horizontalArrangement = spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(
                modifier = Modifier
                    .size(48.dp),
            )
            Image(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                painter = painterResource(id = R.drawable.ic_dhis_white),
                contentDescription = "dhis2 logo",
            )

            if (displayMoreActions) {
                Box {
                    IconButton(
                        modifier = Modifier
                            .size(48.dp),
                        onClick = { expanded = true },
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colors.onPrimary,
                        )
                    }

                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        DropdownMenuItem(onClick = {
                            expanded = false
                            onImportDatabase()
                        }) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = spacedBy(16.dp),
                            ) {
                                Icon(
                                    imageVector = ImageVector.vectorResource(id = R.drawable.ic_import_db),
                                    contentDescription = "Import database",
                                    tint = MaterialTheme.colors.primary,
                                )

                                Text(
                                    text = "Import database",
                                    style = TextStyle(
                                        fontSize = 16.sp,
                                        lineHeight = 24.sp,
                                        fontFamily = provideFontResource("rubik_regular"),
                                        fontWeight = FontWeight.Normal,
                                        color = Color.Black,
                                        letterSpacing = 0.5.sp,
                                    ),
                                )
                            }
                        }
                    }
                }
            }
        }

        Text(
            modifier = Modifier.constrainAs(versionLabel) {
                end.linkTo(parent.end, margin = 16.dp)
                bottom.linkTo(parent.bottom, margin = 8.dp)
            },
            text = version,
            style = TextStyle(
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontFamily = provideFontResource("rubik_regular"),
                fontWeight = FontWeight.Normal,
                color = SurfaceColor.ContainerHighest,
                letterSpacing = 0.4.sp,
            ),
        )
    }
}

@Preview
@Composable
private fun PreviewLoginTopBar() {
    DHIS2Theme {
        LoginTopBar(version = "v2.9") {}
    }
}
