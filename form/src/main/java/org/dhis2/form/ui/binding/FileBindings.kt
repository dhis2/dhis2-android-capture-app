package org.dhis2.form.ui.binding

import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.databinding.BindingAdapter
import com.google.android.material.composethemeadapter.MdcTheme
import java.io.File
import org.dhis2.form.R
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.UiEventType
import org.dhis2.ui.inputs.FileInput

@BindingAdapter("add_file")
fun ComposeView.addFile(fieldUiModel: FieldUiModel) {
    setContent {
        setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        MdcTheme {
            FileInput(
                file = fieldUiModel.value?.let { File(it) },
                addFileLabel = stringResource(id = R.string.add_file),
                onAddFile = {
                    fieldUiModel.invokeUiEvent(UiEventType.ADD_FILE)
                },
                onDeleteFile = {
                    fieldUiModel.invokeUiEvent(UiEventType.CLEAR_FILE)
                },
                onDownloadClick = {
                    fieldUiModel.invokeUiEvent(UiEventType.OPEN_FILE)
                }
            )
        }
    }
}
