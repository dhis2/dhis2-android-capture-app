package org.dhis2.usescases.settings.bindings

import androidx.compose.ui.platform.ComposeView
import androidx.databinding.BindingAdapter
import org.dhis2.ui.Dhis2ProgressIndicator
import org.dhis2.ui.buttons.Dhis2TextButton
import org.dhis2.ui.model.ButtonUiModel
import org.dhis2.ui.theme.Dhis2Theme

@BindingAdapter("addTextButton")
fun ComposeView.addTextButton(model: ButtonUiModel?) {
    model?.let {
        setContent {
            Dhis2Theme {
                Dhis2TextButton(model = it)
            }
        }
    }
}

@BindingAdapter("progressIndicator")
fun ComposeView.progressIndicator(message: String?) {
    setContent {
        Dhis2Theme {
            Dhis2ProgressIndicator(message)
        }
    }
}
