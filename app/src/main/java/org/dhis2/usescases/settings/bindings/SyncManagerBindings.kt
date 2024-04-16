package org.dhis2.usescases.settings.bindings

import androidx.compose.ui.platform.ComposeView
import androidx.databinding.BindingAdapter
import org.dhis2.ui.Dhis2ProgressIndicator
import org.dhis2.ui.model.ButtonUiModel
import org.dhis2.ui.theme.Dhis2Theme
import org.hisp.dhis.mobile.ui.designsystem.component.Button

@BindingAdapter("addTextButton")
fun ComposeView.addTextButton(model: ButtonUiModel?) {
    model?.let {
        setContent {
            Dhis2Theme {
                Button(
                    text = it.text,
                    enabled = it.enabled,
                    onClick = it.onClick,
                )
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
