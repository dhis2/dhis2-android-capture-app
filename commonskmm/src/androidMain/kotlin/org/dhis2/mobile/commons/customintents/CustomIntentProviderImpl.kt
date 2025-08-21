package org.dhis2.mobile.commons.customintents

import org.dhis2.mobile.commons.model.CustomIntentModel
import org.dhis2.mobile.commons.model.CustomIntentRequestArgumentModel
import org.dhis2.mobile.commons.model.CustomIntentResponseDataModel
import org.dhis2.mobile.commons.model.CustomIntentResponseExtraType
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.settings.CustomIntent
import org.hisp.dhis.android.core.settings.CustomIntentActionType
import org.hisp.dhis.android.core.settings.CustomIntentContext
import org.hisp.dhis.android.core.settings.CustomIntentResponseExtraType as ExtraType

class CustomIntentProviderImpl(
    private val d2: D2,
) : CustomIntentProvider {
    private val customIntents: List<CustomIntent?> = d2.settingModule().customIntents().blockingGet()

    override fun getCustomIntentsWithTrigger(
        triggerUid: String?,
        programUid: String?,
        programStageUid: String?,
    ): CustomIntentModel? {
        return getCustomIntentFromUid(triggerUid, CustomIntentContext(programUid, programStageUid))
    }

    private fun getCustomIntentFromUid(uid: String?, context: CustomIntentContext): CustomIntentModel? {
        return getFilteredCustomIntents(uid).firstOrNull { customIntent ->
            customIntent?.action()?.contains(CustomIntentActionType.DATA_ENTRY) == true
        }?.let {
            val requestParams = evaluateCustomIntentRequestParams(it, context)
            val customIntentRequest = requestParams.mapNotNull { param ->
                param.value?.let { value ->
                    CustomIntentRequestArgumentModel(
                        key = param.key,
                        value = value,
                    )
                }
            }
            val customIntentResponse = it.response()?.data()?.extras()?.map { dataExtra ->
                CustomIntentResponseDataModel(
                    name = dataExtra.extraName(),
                    extraType = when (dataExtra.extraType()) {
                        ExtraType.STRING -> CustomIntentResponseExtraType.STRING
                        ExtraType.INTEGER -> CustomIntentResponseExtraType.INTEGER
                        ExtraType.BOOLEAN -> CustomIntentResponseExtraType.BOOLEAN
                        ExtraType.FLOAT -> CustomIntentResponseExtraType.FLOAT
                        ExtraType.OBJECT -> CustomIntentResponseExtraType.OBJECT
                        ExtraType.LIST_OF_OBJECTS -> CustomIntentResponseExtraType.LIST_OF_OBJECTS
                    },
                    key = dataExtra.key(),
                )
            } ?: emptyList()

            CustomIntentModel(
                uid = it.uid(),
                name = it.name(),
                customIntentRequest = customIntentRequest,
                customIntentResponse = customIntentResponse,
                packageName = it.packageName() ?: "",
            )
        }
    }

    private fun getFilteredCustomIntents(uid: String?): List<CustomIntent?> {
        return customIntents.filter { customIntent ->
            customIntent?.trigger()?.attributes()?.any { it.uid() == uid } == true ||
                customIntent?.trigger()?.dataElements()?.any { it.uid() == uid } == true
        }
    }

    private fun evaluateCustomIntentRequestParams(
        customIntent: CustomIntent,
        context: CustomIntentContext,
    ): Map<String, Any?> {
        return d2.settingModule().customIntentService()
            .blockingEvaluateRequestParams(customIntent, context)
    }
}
