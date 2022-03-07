package org.dhis2.utils.customviews

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.composethemeadapter.MdcTheme
import org.dhis2.R
import org.dhis2.commons.data.FieldWithIssue
import org.dhis2.ui.DataEntryBottomDialogContent

class DataEntryBottomDialog(
    var isSaved: Boolean,
    var message: String,
    var fieldsWithIssues: List<FieldWithIssue>? = emptyList(),
    var mainButtonContent: @Composable (RowScope.() -> Unit),
    var onMainButtonClicked: () -> Unit,
    var secondaryButtonContent: @Composable RowScope.() -> Unit = {},
    var onSecondaryButtonClicked: () -> Unit = {}
) : BottomSheetDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MdcTheme {
                    DataEntryBottomDialogContent(
                        isSaved = isSaved,
                        message = message,
                        fieldsWithIssues = fieldsWithIssues,
                        mainButtonContent = mainButtonContent,
                        onMainButtonClicked = onMainButtonClicked,
                        secondaryButtonContent = secondaryButtonContent,
                        onSecondaryButtonClicked = onSecondaryButtonClicked
                    )
                }
            }
        }
    }
}
