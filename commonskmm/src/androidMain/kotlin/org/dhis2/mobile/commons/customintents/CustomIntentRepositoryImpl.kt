package org.dhis2.mobile.commons.customintents

import org.dhis2.mobile.commons.model.CustomIntentActionTypeModel
import org.dhis2.mobile.commons.model.CustomIntentModel
import org.dhis2.mobile.commons.model.CustomIntentRequestArgumentModel
import org.dhis2.mobile.commons.model.CustomIntentResponseDataModel
import org.dhis2.mobile.commons.model.CustomIntentResponseExtraType
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.settings.CustomIntent
import org.hisp.dhis.android.core.settings.CustomIntentContext
import org.hisp.dhis.android.core.settings.CustomIntentActionType as CustomIntentType
import org.hisp.dhis.android.core.settings.CustomIntentResponseExtraType as ExtraType

class CustomIntentRepositoryImpl(
    private val d2: D2,
) : CustomIntentRepository {
    private val customIntents: List<CustomIntent?> = d2.settingModule().customIntents().blockingGet()

    override fun getCustomIntent(
        triggerUid: String?,
        orgUnitUid: String?,
        actionType: CustomIntentActionTypeModel,
    ): CustomIntentModel? = getCustomIntentFromUid(triggerUid, CustomIntentContext(orgunitUid = orgUnitUid), actionType)

    override fun attributeHasCustomIntentAndReturnsAListOfValues(
        triggerUid: String,
        actionType: CustomIntentActionTypeModel,
    ): Boolean {
        val customIntent = getCustomIntentFromUid(triggerUid, CustomIntentContext(null), actionType)
        return customIntent != null &&
            (
                customIntent.customIntentResponse.size > 1 ||
                    customIntent.customIntentResponse.any { it.extraType == CustomIntentResponseExtraType.LIST_OF_OBJECTS }
            )
    }

    private fun getCustomIntentActionType(actionType: CustomIntentActionTypeModel): CustomIntentType =
        when (actionType) {
            CustomIntentActionTypeModel.DATA_ENTRY -> CustomIntentType.DATA_ENTRY
            CustomIntentActionTypeModel.SEARCH -> CustomIntentType.SEARCH
        }

    private fun getCustomIntentFromUid(
        uid: String?,
        context: CustomIntentContext,
        actionType: CustomIntentActionTypeModel,
    ): CustomIntentModel? =
        getFilteredCustomIntents(uid)
            .firstOrNull { customIntent ->
                customIntent?.action()?.contains(getCustomIntentActionType(actionType)) == true
            }?.let {
                val requestParams = evaluateCustomIntentRequestParams(it, context)

                val customIntentRequest =
                    requestParams.mapNotNull { param ->
                        param.value?.let { value ->
                            CustomIntentRequestArgumentModel(
                                key = param.key,
                                value = value,
                            )
                        }
                    }
                val customIntentResponse =
                    it.response()?.data()?.extras()?.map { dataExtra ->
                        CustomIntentResponseDataModel(
                            name = dataExtra.extraName(),
                            extraType =
                                when (dataExtra.extraType()) {
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

    private fun getFilteredCustomIntents(uid: String?): List<CustomIntent?> =
        customIntents.filter { customIntent ->
            customIntent?.trigger()?.attributes()?.any { it.uid() == uid } == true ||
                customIntent?.trigger()?.dataElements()?.any { it.uid() == uid } == true
        }

    private fun evaluateCustomIntentRequestParams(
        customIntent: CustomIntent,
        context: CustomIntentContext,
    ): Map<String, Any?> =
        d2
            .settingModule()
            .customIntentService()
            .blockingEvaluateRequestParams(customIntent, context)

    override fun reEvaluateCustomIntentRequestParams(
        orgUnitUid: String,
        customIntentUid: String,
    ): Map<String, Any?> {
        val customIntent = customIntents.firstOrNull { it?.uid() == customIntentUid }
        val context = CustomIntentContext(orgunitUid = orgUnitUid)
        return customIntent?.let {
            evaluateCustomIntentRequestParams(customIntent, context)
        } ?: emptyMap()
    }
}
