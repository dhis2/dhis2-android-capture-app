package org.dhis2.utils.customviews

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.composethemeadapter.MdcTheme
import org.dhis2.R
import org.dhis2.ui.DataEntryBottomDialogContent
import org.dhis2.ui.DataEntryDialogUiModel

class DataEntryBottomDialog(
    var dataEntryDialogUiModel: DataEntryDialogUiModel,
    var onMainButtonClicked: () -> Unit,
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
                        dataEntryDialogUiModel = dataEntryDialogUiModel,
                        onMainButtonClicked = onMainButtonClicked,
                        onSecondaryButtonClicked = onSecondaryButtonClicked
                    )
                }
            }
        }
    }
}
