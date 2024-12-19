package org.dhis2.usescases.settings.bindings

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.ComposeView
import androidx.databinding.BindingAdapter
import org.dhis2.ui.model.ButtonUiModel
import org.dhis2.ui.theme.Dhis2Theme
import org.dhis2.ui.theme.textSecondary
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicator
import org.hisp.dhis.mobile.ui.designsystem.component.ProgressIndicatorType
import org.hisp.dhis.mobile.ui.designsystem.theme.DHIS2Theme

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
        DHIS2Theme {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ProgressIndicator(
                    type = ProgressIndicatorType.CIRCULAR,
                )
                message?.let { Text(it, color = textSecondary) }
            }
        }
    }
}
