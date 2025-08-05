package org.dhis2.form.data.metadata

import androidx.paging.PagingData
import androidx.paging.filter
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import org.dhis2.commons.bindings.disableCollapsableSectionsInProgram
import org.dhis2.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.commons.featureconfig.model.Feature
import org.dhis2.commons.viewmodel.DispatcherProvider
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.settings.CustomIntent
import org.hisp.dhis.android.core.settings.CustomIntentActionType
import org.hisp.dhis.android.core.settings.CustomIntentAttribute
import org.hisp.dhis.android.core.settings.CustomIntentContext
import org.hisp.dhis.android.core.settings.CustomIntentDataElement
import org.hisp.dhis.android.core.settings.CustomIntentRequest
import org.hisp.dhis.android.core.settings.CustomIntentRequestArgument
import org.hisp.dhis.android.core.settings.CustomIntentResponse
import org.hisp.dhis.android.core.settings.CustomIntentResponseData
import org.hisp.dhis.android.core.settings.CustomIntentResponseDataExtra
import org.hisp.dhis.android.core.settings.CustomIntentResponseExtraType
import org.hisp.dhis.android.core.settings.CustomIntentTrigger

open class FormBaseConfiguration(
    private val d2: D2,
    private val dispatcher: DispatcherProvider,
    private val featureConfig: FeatureConfigRepository,
) {
    fun optionGroups(optionGroupUids: List<String>) = d2.optionModule().optionGroups()
        .withOptions()
        .byUid().`in`(optionGroupUids)
        .blockingGet()

    fun customIntents(): List<CustomIntent?> = if (featureConfig.isFeatureEnable(Feature.CUSTOM_INTENTS)) {
        getMockedCustomIntents()
    } else {
        d2.settingModule().customIntents()
            .blockingGet()
    }

    private fun getMockedCustomIntents(): List<CustomIntent> {
        return listOf(
            CustomIntent.builder()
                .uid("customIntentRegisterUid")
                .name("Registration custom intent")
                .trigger(
                    CustomIntentTrigger.builder()
                        .attributes(
                            listOf(
                                CustomIntentAttribute.builder()
                                    .customIntentUid("customIntentRegisterUid")
                                    .uid("M2wNlKugVe9")
                                    .build(),
                            ),
                        )
                        .dataElements(
                            listOf(
                                CustomIntentDataElement.builder()
                                    .customIntentUid("customIntentRegisterUid")
                                    .uid("bYZCH0o9l8W")
                                    .build(),
                                CustomIntentDataElement.builder()
                                    .customIntentUid("customIntentRegisterEventProgramUid")
                                    .uid("goBca56SGgZ")
                                    .build(),
                            ),
                        )
                        .build(),
                )
                .action(listOf(CustomIntentActionType.DATA_ENTRY))
                .packageName("com.packageName.id.REGISTER")
                .request(
                    CustomIntentRequest.builder().arguments(
                        listOf(
                            CustomIntentRequestArgument.builder().key("projectId").value("'testDhisProjectId'").build(),
                            CustomIntentRequestArgument.builder().key("moduleId").value("'testDhisModuleId'").build(),
                            CustomIntentRequestArgument.builder().key("userId").value("'testDhisUserId'").build(),
                        ),
                    ).build(),
                )
                .response(
                    CustomIntentResponse.builder()
                        .data(
                            CustomIntentResponseData.builder()
                                .extras(
                                    listOf(
                                        CustomIntentResponseDataExtra.builder()
                                            .extraName("enrolment")
                                            .extraType(CustomIntentResponseExtraType.OBJECT)
                                            .key("guid")
                                            .build(),
                                    ),
                                )
                                .build(),
                        )
                        .build(),
                ).build(),
            CustomIntent.builder()
                .uid("customIntentIdentifyUid")
                .name("Identification custom intent")
                .trigger(
                    CustomIntentTrigger.builder()
                        .attributes(
                            listOf(
                                CustomIntentAttribute.builder()
                                    .customIntentUid("customIntentIdentifyUid")
                                    .uid("WxshfKSrRdM")
                                    .build(),
                            ),
                        )
                        .build(),
                )
                .action(listOf(CustomIntentActionType.DATA_ENTRY))
                .packageName("com.simprints.id.IDENTIFY")
                .request(
                    CustomIntentRequest.builder().arguments(
                        listOf(
                            CustomIntentRequestArgument.builder().key("projectId").value("'testDhisProjectId'").build(),
                            CustomIntentRequestArgument.builder().key("moduleId").value("'testDhisModuleId'").build(),
                            CustomIntentRequestArgument.builder().key("userId").value("'testDhisUserId'").build(),
                        ),
                    ).build(),
                )
                .response(
                    CustomIntentResponse.builder()
                        .data(
                            CustomIntentResponseData.builder()
                                .extras(
                                    listOf(
                                        CustomIntentResponseDataExtra.builder()
                                            .extraName("identification")
                                            .extraType(CustomIntentResponseExtraType.OBJECT)
                                            .key("guid")
                                            .build(),
                                    ),
                                ).build(),
                        )
                        .build(),
                ).build(),
        )
    }

    fun evaluateCustomIntentRequestParams(
        customIntent: CustomIntent,
        context: CustomIntentContext
    ): Map<String, Any?> {
        return d2.settingModule().customIntentService()
            .blockingEvaluateRequestParams(customIntent, context)
    }

    fun disableCollapsableSectionsInProgram(programUid: String) =
        d2.disableCollapsableSectionsInProgram(programUid)

    fun dateFormatConfiguration() =
        d2.systemInfoModule().systemInfo().blockingGet()?.dateFormat()

    fun options(
        optionSetUid: String,
        query: String,
        optionsToHide: List<String>,
        optionGroupsToHide: List<String>,
        optionGroupsToShow: List<String>,
    ): Flow<PagingData<Option>> {
        return when {
            query.isEmpty() -> d2.optionModule()
                .options()
                .byOptionSetUid().eq(optionSetUid).orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                .getPagingData(10)

            else ->
                d2.optionModule()
                    .options()
                    .byOptionSetUid().eq(optionSetUid).orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                    .byDisplayName().like("%$query%")
                    .getPagingData(10)
        }.map { pagingData ->
            pagingData.filter { option ->
                withContext(dispatcher.io()) {
                    val optionInGroupToHide = d2.optionModule().optionGroups()
                        .withOptions()
                        .byUid().`in`(optionGroupsToHide)
                        .blockingGet().any { optionGroup ->
                            optionGroup.options()?.map { it.uid() }?.contains(option.uid()) == true
                        }

                    val optionInGroupToShow = d2.optionModule().optionGroups()
                        .withOptions()
                        .byUid().`in`(optionGroupsToShow)
                        .blockingGet().any { optionGroup ->
                            optionGroup.options()?.map { it.uid() }?.contains(option.uid()) == true
                        }

                    val hideOption = if (optionGroupsToShow.isEmpty()) {
                        optionsToHide.contains(option.uid()) || optionInGroupToHide
                    } else {
                        !optionInGroupToShow
                    }

                    !hideOption
                }
            }
        }
    }
}
