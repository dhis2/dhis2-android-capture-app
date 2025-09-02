package org.dhis2.mobile.aggregates.ui.provider

import org.dhis2.mobile.aggregates.resources.Res
import org.dhis2.mobile.aggregates.resources.action_can_not_be_performed
import org.dhis2.mobile.aggregates.resources.action_done
import org.dhis2.mobile.aggregates.resources.action_next
import org.dhis2.mobile.aggregates.resources.dataset_saved_completed
import org.dhis2.mobile.aggregates.resources.default_column_label
import org.dhis2.mobile.aggregates.resources.error
import org.dhis2.mobile.aggregates.resources.error_on_complete_dataset
import org.dhis2.mobile.aggregates.resources.errors
import org.dhis2.mobile.aggregates.resources.field_mandatory
import org.dhis2.mobile.aggregates.resources.field_required
import org.dhis2.mobile.aggregates.resources.file_download_error
import org.dhis2.mobile.aggregates.resources.file_downloaded
import org.dhis2.mobile.aggregates.resources.indicators_label
import org.dhis2.mobile.aggregates.resources.mark_dataset_complete
import org.dhis2.mobile.aggregates.resources.run_validation_rules
import org.dhis2.mobile.aggregates.resources.saved
import org.dhis2.mobile.aggregates.resources.total_header_label
import org.dhis2.mobile.aggregates.resources.validation_success_title
import org.jetbrains.compose.resources.getString

internal class ResourceManager {
    suspend fun defaultHeaderLabel() = getString(Res.string.default_column_label)

    suspend fun totalsHeader() = getString(Res.string.total_header_label)

    suspend fun provideCompletionDialogTitle() = getString(Res.string.validation_success_title)

    suspend fun provideCompletionDialogDescription() = getString(Res.string.mark_dataset_complete)

    suspend fun provideSaved() = getString(Res.string.saved)

    suspend fun provideMandatoryFieldsMessage() = getString(Res.string.field_mandatory)

    suspend fun provideMandatoryFieldsCombinationMessage() = getString(Res.string.field_required)

    suspend fun provideSavedAndCompleted() = getString(Res.string.dataset_saved_completed)

    suspend fun provideErrorOnCompleteDataset() = getString(Res.string.error_on_complete_dataset)

    suspend fun provideAskRunValidations() = getString(Res.string.run_validation_rules)

    suspend fun provideValidationErrorDescription(errors: Int): String =
        if (errors == 1) {
            getString(Res.string.error)
        } else {
            getString(Res.string.errors)
        }

    suspend fun actionCantBePerformed(): String = getString(Res.string.action_can_not_be_performed)

    suspend fun provideDone() = getString(Res.string.action_done)

    suspend fun provideNext() = getString(Res.string.action_next)

    suspend fun provideFileDownload() = getString(Res.string.file_downloaded)

    suspend fun provideFileDownloadError() = getString(Res.string.file_download_error)

    suspend fun indicatorsLabel() = getString(Res.string.indicators_label)
}
