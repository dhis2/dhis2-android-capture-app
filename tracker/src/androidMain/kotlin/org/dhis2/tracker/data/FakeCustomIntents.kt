package org.dhis2.tracker.data

import org.hisp.dhis.android.core.settings.CustomIntent
import org.hisp.dhis.android.core.settings.CustomIntentActionType
import org.hisp.dhis.android.core.settings.CustomIntentAttribute
import org.hisp.dhis.android.core.settings.CustomIntentDataElement
import org.hisp.dhis.android.core.settings.CustomIntentRequest
import org.hisp.dhis.android.core.settings.CustomIntentRequestArgument
import org.hisp.dhis.android.core.settings.CustomIntentResponse
import org.hisp.dhis.android.core.settings.CustomIntentResponseData
import org.hisp.dhis.android.core.settings.CustomIntentResponseDataExtra
import org.hisp.dhis.android.core.settings.CustomIntentResponseExtraType
import org.hisp.dhis.android.core.settings.CustomIntentTrigger

val FakeCustomIntents: List<CustomIntent> =
    listOf(
        CustomIntent
            .builder()
            .uid("customIntentRegisterUid")
            .name("Registration custom intent")
            .trigger(
                CustomIntentTrigger
                    .builder()
                    .attributes(
                        listOf(
                            CustomIntentAttribute
                                .builder()
                                .customIntentUid("customIntentRegisterUid")
                                .uid("M2wNlKugVe9")
                                .build(),
                        ),
                    ).dataElements(
                        listOf(
                            CustomIntentDataElement
                                .builder()
                                .customIntentUid("customIntentRegisterUid")
                                .uid("bYZCH0o9l8W")
                                .build(),
                            CustomIntentDataElement
                                .builder()
                                .customIntentUid("customIntentRegisterEventProgramUid")
                                .uid("goBca56SGgZ")
                                .build(),
                        ),
                    ).build(),
            ).action(listOf(CustomIntentActionType.DATA_ENTRY))
            .packageName("com.packageName.id.REGISTER")
            .request(
                CustomIntentRequest
                    .builder()
                    .arguments(
                        listOf(
                            CustomIntentRequestArgument
                                .builder()
                                .key("projectId")
                                .value("testDhisProjectId")
                                .build(),
                            CustomIntentRequestArgument
                                .builder()
                                .key("moduleId")
                                .value("testDhisModuleId")
                                .build(),
                            CustomIntentRequestArgument
                                .builder()
                                .key("userId")
                                .value("testDhisUserId")
                                .build(),
                        ),
                    ).build(),
            ).response(
                CustomIntentResponse
                    .builder()
                    .data(
                        CustomIntentResponseData
                            .builder()
                            .extras(
                                listOf(
                                    CustomIntentResponseDataExtra
                                        .builder()
                                        .extraName("enrolment")
                                        .extraType(CustomIntentResponseExtraType.OBJECT)
                                        .key("guid")
                                        .build(),
                                ),
                            ).build(),
                    ).build(),
            ).build(),
        CustomIntent
            .builder()
            .uid("customIntentIdentifyUid")
            .name("Identification custom intent")
            .trigger(
                CustomIntentTrigger
                    .builder()
                    .attributes(
                        listOf(
                            CustomIntentAttribute
                                .builder()
                                .customIntentUid("customIntentIdentifyUid")
                                .uid("WxshfKSrRdM")
                                .build(),
                        ),
                    ).build(),
            ).action(listOf(CustomIntentActionType.DATA_ENTRY))
            .packageName("com.simprints.id.IDENTIFY")
            .request(
                CustomIntentRequest
                    .builder()
                    .arguments(
                        listOf(
                            CustomIntentRequestArgument
                                .builder()
                                .key("projectId")
                                .value("testDhisProjectId")
                                .build(),
                            CustomIntentRequestArgument
                                .builder()
                                .key("moduleId")
                                .value("testDhisModuleId")
                                .build(),
                            CustomIntentRequestArgument
                                .builder()
                                .key("userId")
                                .value("testDhisUserId")
                                .build(),
                        ),
                    ).build(),
            ).response(
                CustomIntentResponse
                    .builder()
                    .data(
                        CustomIntentResponseData
                            .builder()
                            .extras(
                                listOf(
                                    CustomIntentResponseDataExtra
                                        .builder()
                                        .extraName("identification")
                                        .extraType(CustomIntentResponseExtraType.OBJECT)
                                        .key("guid")
                                        .build(),
                                ),
                            ).build(),
                    ).build(),
            ).build(),
    )
