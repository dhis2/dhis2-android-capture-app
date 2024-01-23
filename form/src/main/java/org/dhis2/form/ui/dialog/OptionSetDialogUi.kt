package org.dhis2.form.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.dhis2.form.R
import org.dhis2.form.model.OptionSetDialogViewModel
import org.hisp.dhis.android.core.option.Option

@Composable
fun OptionSetDialogScreen(
    viewModel: OptionSetDialogViewModel,
    onClearClick: () -> Unit,
    onCancelClick: () -> Unit,
    onOptionClick: (optionCode: String?) -> Unit,
) {
    Column(
        modifier = Modifier
            .background(
                color = Color.White,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(
                vertical = 8.dp,
                horizontal = 16.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val searchValue by viewModel.searchValue.observeAsState("")
        val options by viewModel.options.observeAsState(emptyList())

        DialogTitle(title = viewModel.field.formattedLabel)
        SearchBar(
            searchValue = searchValue,
            onSearchValueChanged = { newValue ->
                viewModel.onSearchingOption(newValue)
            },
            onClearSearchClick = {
                viewModel.onSearchingOption("")
            },
        )

        if (options.isNotEmpty()) {
            OptionList(options) { onOptionClick(it) }
        } else {
            Box(
                modifier = Modifier
                    .height(300.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                when (searchValue.isNotEmpty()) {
                    true -> Text(stringResource(R.string.no_option_found))
                    else -> CircularProgressIndicator()
                }
            }
        }
        DialogButtonActions(
            onClearClick = onClearClick,
            onCancelClick = onCancelClick,
        )
    }
}

@Composable
private fun DialogTitle(title: String) {
    Text(
        text = title,
        style = TextStyle(
            color = colorResource(id = R.color.text_black_333),
            fontSize = 20.sp,
            fontFamily = FontFamily(Font(R.font.rubik_regular)),
        ),
    )
}

@Composable
private fun SearchBar(
    searchValue: String,
    onSearchValueChanged: (String) -> Unit,
    onClearSearchClick: () -> Unit,
) {
    BasicTextField(
        modifier = Modifier
            .height(32.dp)
            .fillMaxWidth(),
        value = searchValue,
        onValueChange = onSearchValueChanged,
        singleLine = true,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colors.primary,
                        shape = RoundedCornerShape(16.dp),
                    )
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "",
                    tint = MaterialTheme.colors.primary,
                )
                Spacer(modifier = Modifier.size(8.dp))
                Box(Modifier.weight(1f)) {
                    if (searchValue.isEmpty()) {
                        Text(
                            text = stringResource(id = R.string.search),
                            style = LocalTextStyle.current.copy(
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.3f),
                                fontSize = 16.sp,
                            ),
                        )
                    }
                    innerTextField()
                }
                if (searchValue.isNotEmpty()) {
                    IconButton(onClick = onClearSearchClick) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "",
                            tint = MaterialTheme.colors.primary,
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun OptionList(options: List<Option>, onOptionClick: (code: String?) -> Unit) {
    LazyColumn(
        modifier = Modifier.height(300.dp),
    ) {
        items(items = options) { option ->
            Box(
                modifier = Modifier
                    .defaultMinSize(minHeight = 42.dp)
                    .wrapContentHeight()
                    .fillMaxWidth()
                    .clickable { onOptionClick(option.code()) },
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    text = option.displayName() ?: option.uid(),
                    style = TextStyle(
                        color = colorResource(id = R.color.text_black_333),
                        fontSize = 14.sp,
                        fontFamily = FontFamily(Font(R.font.rubik_regular)),
                    ),
                )
            }
            Divider()
        }
    }
}

@Composable
private fun DialogButtonActions(onClearClick: () -> Unit, onCancelClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        TextButton(onClick = onClearClick) {
            Text(text = stringResource(id = R.string.clear).uppercase())
        }
        Spacer(modifier = Modifier.size(8.dp))
        TextButton(onClick = onCancelClick) {
            Text(text = stringResource(id = R.string.cancel).uppercase())
        }
    }
}
