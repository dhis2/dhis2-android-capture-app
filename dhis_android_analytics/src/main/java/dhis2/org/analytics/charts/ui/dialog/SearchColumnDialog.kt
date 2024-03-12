package dhis2.org.analytics.charts.ui.dialog

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.themeadapter.material3.Mdc3Theme
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dhis2.org.R
import org.hisp.dhis.mobile.ui.designsystem.component.BasicTextField
import org.hisp.dhis.mobile.ui.designsystem.component.InputShellState
import org.hisp.dhis.mobile.ui.designsystem.component.SquareIconButton
import org.hisp.dhis.mobile.ui.designsystem.theme.Spacing
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

class SearchColumnDialog(
    private val title: String,
    private val onSearch: (String?) -> Unit,
) : BottomSheetDialogFragment() {

    companion object {
        const val TAG: String = "SEARCH_COLUM_DIALOG"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed,
            )
            setContent {
                Mdc3Theme {
                    val focusRequester = remember { FocusRequester() }
                    SearchColumnInput(
                        title = title,
                        focusRequester = focusRequester,
                        onSearch = {
                            onSearch(it)
                            dismiss()
                        },
                    )
                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus()
                    }
                }
            }
        }
    }
}

@Composable
fun SearchColumnInput(
    title: String,
    focusRequester: FocusRequester,
    onSearch: (String?) -> Unit,
) {
    var textValue: TextFieldValue? by remember { mutableStateOf(TextFieldValue("")) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.White,
                shape = RoundedCornerShape(
                    topStart = Spacing.Spacing16,
                    topEnd = Spacing.Spacing16,
                ),
            )
            .padding(Spacing.Spacing8),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier.weight(1f).padding(start = Spacing.Spacing16),
                verticalArrangement = Arrangement.spacedBy(Spacing.Spacing2),
            ) {
                Text(
                    text = title,
                    style = TextStyle(fontSize = 14.sp),
                    color = SurfaceColor.Primary,
                    maxLines = 1,
                )
                BasicTextField(
                    modifier = Modifier.focusRequester(focusRequester),
                    state = InputShellState.FOCUSED,
                    inputTextValue = textValue,
                    onInputChanged = { textValue = it },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    onSearchClicked = {
                        onSearch(textValue?.text)
                    },
                )
            }
            SquareIconButton(
                modifier = Modifier.padding(horizontal = Spacing.Spacing4),
                icon = {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_search),
                        tint = SurfaceColor.Primary,
                        contentDescription = "",
                    )
                },
                onClick = {
                    onSearch(textValue?.text)
                },
            )
        }
        Divider(
            modifier = Modifier.padding(top = Spacing.Spacing8),
            color = SurfaceColor.Primary,
            thickness = 1.dp,
        )
    }
}

@Preview
@Composable
fun PreviewSearchColum() {
    SearchColumnInput(
        title = "Column name",
        focusRequester = FocusRequester(),
        onSearch = {
        },
    )
}
