package org.dhis2.mobile.aggregates.ui.provider

import org.dhis2.mobile.aggregates.resources.Res
import org.dhis2.mobile.aggregates.resources.complete
import org.dhis2.mobile.aggregates.resources.default_column_label
import org.dhis2.mobile.aggregates.resources.total_header_label
import org.dhis2.mobile.aggregates.resources.error_on_complete_dataset
import org.dhis2.mobile.aggregates.resources.mandatory_fields_combination_message
import org.dhis2.mobile.aggregates.resources.mandatory_fields_dialog_description
import org.dhis2.mobile.aggregates.resources.mark_dataset_complete
import org.dhis2.mobile.aggregates.resources.not_now
import org.dhis2.mobile.aggregates.resources.ok
import org.dhis2.mobile.aggregates.resources.saved
import org.dhis2.mobile.aggregates.resources.saved_and_completed
import org.dhis2.mobile.aggregates.resources.validation_success_title
import org.jetbrains.compose.resources.getString

internal class ResourceManager {
    suspend fun defaultHeaderLabel() = getString(Res.string.default_column_label)
    suspend fun totalsHeader() = getString(Res.string.total_header_label)

    suspend fun provideCompletionDialogTitle() = getString(Res.string.validation_success_title)

    suspend fun provideCompletionDialogDescription() = getString(Res.string.mark_dataset_complete)

    suspend fun provideNotNow() = getString(Res.string.not_now)

    suspend fun provideComplete() = getString(Res.string.complete)

    suspend fun provideSaved() = getString(Res.string.saved)

    suspend fun provideMandatoryFieldsMessage() =
        getString(Res.string.mandatory_fields_dialog_description)

    suspend fun provideMandatoryFieldsCombinationMessage() =
        getString(Res.string.mandatory_fields_combination_message)

    suspend fun provideOK() = getString(Res.string.ok)

    suspend fun provideSavedAndCompleted() = getString(Res.string.saved_and_completed)

    suspend fun provideErrorOnCompleteDataset() = getString(Res.string.error_on_complete_dataset)
}
