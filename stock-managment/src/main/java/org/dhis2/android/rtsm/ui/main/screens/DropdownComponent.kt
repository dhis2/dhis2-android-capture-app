@file:Suppress("PreviewAnnotationInFunctionWithParameters")

package org.dhis2.android.rtsm.ui.main.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import org.dhis2.android.rtsm.R

@Preview
@Composable
fun DropdownComponent(
    data: MutableList<String>
) {
    var isExpanded by remember { mutableStateOf(false) }

    var selectedText by remember { mutableStateOf("") }

    var textFieldSize by remember { mutableStateOf(Size.Zero) }

    val icon = if (isExpanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown

    Column(Modifier.padding(16.dp)) {

        OutlinedTextField(
            value = selectedText,
            onValueChange = { selectedText = it },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    textFieldSize = coordinates.size.toSize()
                }.background(color = Color.White, shape = RoundedCornerShape(30.dp)),
            readOnly = true,
            singleLine = true,
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_org_unit_chart),
                    contentDescription = null,
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp),
                    tint = colorResource(id = R.color.colorPrimary)
                )
            },
            trailingIcon = {
                IconButton(onClick = {
                    isExpanded = !isExpanded
                }) {
                    Icon(icon,contentDescription= null, tint = colorResource(R.color.colorPrimary))
                }
            },
            shape = RoundedCornerShape(30.dp),
            placeholder = { Text(data.first()) }
        )

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            modifier = Modifier
                .width(with(LocalDensity.current) { textFieldSize.width.toDp() })
                .background(Color.White, RoundedCornerShape(30.dp)),
            offset = DpOffset(x= 0.dp, y = 5.dp),
        ) {
            data.forEach { label ->
                DropdownMenuItem(onClick = {
                    selectedText = label
                    isExpanded = false
                }) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_sync),
                            contentDescription = null,
                            Modifier.padding(6.dp),
                            tint = colorResource(id = R.color.colorPrimary)
                        )
                        Text(text = label)
                    }
                }
            }
        }
    }
}
