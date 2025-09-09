package org.dhis2.form.data

import org.dhis2.commons.dialogs.bottomsheet.FieldWithIssue
import org.dhis2.form.model.EventMode
import org.hisp.dhis.android.core.common.ValidationStrategy
import org.hisp.dhis.android.core.event.EventStatus

sealed class DataIntegrityCheckResult(
    open val canComplete: Boolean = false,
    open val onCompleteMessage: String? = null,
    open val allowDiscard: Boolean = false,
    open val eventResultDetails: EventResultDetails = EventResultDetails(null, null, null),
)

data class MissingMandatoryResult(
    val mandatoryFields: Map<String, String>,
    val errorFields: List<FieldWithIssue>,
    val warningFields: List<FieldWithIssue>,
    override val canComplete: Boolean,
    override val onCompleteMessage: String?,
    override val allowDiscard: Boolean,
    override val eventResultDetails: EventResultDetails,
) : DataIntegrityCheckResult(
        canComplete,
        onCompleteMessage,
        allowDiscard,
        eventResultDetails,
    )

data class FieldsWithErrorResult(
    val mandatoryFields: Map<String, String>,
    val fieldUidErrorList: List<FieldWithIssue>,
    val warningFields: List<FieldWithIssue>,
    override val canComplete: Boolean,
    override val onCompleteMessage: String?,
    override val allowDiscard: Boolean,
    override val eventResultDetails: EventResultDetails,
) : DataIntegrityCheckResult(
        canComplete,
        onCompleteMessage,
        allowDiscard,
        eventResultDetails,
    )

data class FieldsWithWarningResult(
    val fieldUidWarningList: List<FieldWithIssue>,
    override val canComplete: Boolean,
    override val onCompleteMessage: String?,
    override val eventResultDetails: EventResultDetails,
) : DataIntegrityCheckResult(
        canComplete,
        onCompleteMessage,
        eventResultDetails = eventResultDetails,
    )

data class SuccessfulResult(
    val extraData: String? = null,
    override val canComplete: Boolean,
    override val onCompleteMessage: String?,
    override val eventResultDetails: EventResultDetails,
) : DataIntegrityCheckResult(
        canComplete,
        onCompleteMessage,
        eventResultDetails = eventResultDetails,
    )

data class EventResultDetails(
    val eventStatus: EventStatus?,
    val eventMode: EventMode?,
    val validationStrategy: ValidationStrategy?,
)

object NotSavedResult : DataIntegrityCheckResult()
