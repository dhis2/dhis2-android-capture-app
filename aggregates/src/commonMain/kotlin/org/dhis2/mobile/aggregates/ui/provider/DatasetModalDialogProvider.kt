package org.dhis2.mobile.aggregates.ui.provider

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import org.dhis2.mobile.aggregates.ui.states.DataSetModalDialogUIState
import org.hisp.dhis.mobile.ui.designsystem.component.Button
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonBlock
import org.hisp.dhis.mobile.ui.designsystem.component.ButtonStyle
import org.hisp.dhis.mobile.ui.designsystem.component.state.BottomSheetShellDefaults
import org.hisp.dhis.mobile.ui.designsystem.component.state.BottomSheetShellUIState

internal class DatasetModalDialogProvider(
    val resourceManager: ResourceManager,
) {

    suspend fun provideCompletionDialog(
        onDismiss: () -> Unit,
        onNotNow: () -> Unit,
        onComplete: () -> Unit,
    ): DataSetModalDialogUIState {
        val notNowText = resourceManager.provideNotNow()
        val completeText = resourceManager.provideComplete()

        return DataSetModalDialogUIState(
            contentDialogUIState = BottomSheetShellUIState(
                title = resourceManager.provideCompletionDialogTitle(),
                subtitle = resourceManager.provideCompletionDialogDescription(),
                showBottomSectionDivider = false,
                headerTextAlignment = TextAlign.Start,
            ),
            buttonsDialog = {
                ButtonBlock(
                    modifier = Modifier.padding(
                        BottomSheetShellDefaults.buttonBlockPaddings(),
                    ),
                    primaryButton = {
                        Button(
                            style = ButtonStyle.OUTLINED,
                            text = notNowText,
                            onClick = onNotNow,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    },
                    secondaryButton = {
                        Button(
                            style = ButtonStyle.FILLED,
                            text = completeText,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = onComplete,
                        )
                    },
                )
            },
            onDismiss = onDismiss,
        )
    }

    suspend fun provideMandatoryFieldsDialog(
        mandatoryFieldsMessage: String,
        onDismiss: () -> Unit,
        onAccept: () -> Unit,
    ): DataSetModalDialogUIState {
        val acceptText = resourceManager.provideOK()

        return DataSetModalDialogUIState(
            contentDialogUIState = BottomSheetShellUIState(
                title = resourceManager.provideSaved(),
                subtitle = mandatoryFieldsMessage,
                showBottomSectionDivider = false,
                headerTextAlignment = TextAlign.Start,
            ),
            buttonsDialog = {
                ButtonBlock(
                    modifier = Modifier.padding(
                        BottomSheetShellDefaults.buttonBlockPaddings(),
                    ),
                    primaryButton = {
                        Button(
                            style = ButtonStyle.FILLED,
                            text = acceptText,
                            modifier = Modifier.fillMaxWidth(),
                            onClick = onAccept,
                        )
                    },
                )
            },
            onDismiss = onDismiss,
        )
    }
}
