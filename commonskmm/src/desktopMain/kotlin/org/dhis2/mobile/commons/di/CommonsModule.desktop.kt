package org.dhis2.mobile.commons.di

import org.dhis2.mobile.commons.data.ValueParser
import org.dhis2.mobile.commons.featureconfig.data.FeatureConfigRepository
import org.dhis2.mobile.commons.featureconfig.model.Feature
import org.dhis2.mobile.commons.featureconfig.model.FeatureState
import org.dhis2.mobile.commons.model.internal.ValueInfo
import org.koin.core.module.Module
import org.koin.dsl.module

actual val commonsModule: Module
    get() =
        module {
            single<FeatureConfigRepository> {
                object : FeatureConfigRepository {
                    override val featuresList: List<FeatureState>
                        get() = emptyList()

                    override fun updateItem(featureState: FeatureState) = Unit

                    override fun isFeatureEnable(feature: Feature) = false
                }
            }

            single {
                object : ValueParser {
                    override suspend fun getValueInfo(
                        uid: String,
                        value: String,
                    ): ValueInfo =
                        ValueInfo(
                            optionSetUid = null,
                            valueIsValidOption = true,
                            isMultiText = false,
                            isOrganisationUnit = false,
                            isFile = false,
                            isDate = false,
                            isDateTime = false,
                            isTime = false,
                            isPercentage = false,
                            valueIsAValidOrgUnit = false,
                            valueIsAValidFile = false,
                            isCoordinate = false,
                            isBooleanType = false,
                        )

                    override suspend fun valueFromMultiTextAsOptionNames(
                        optionSetUid: String,
                        value: String,
                    ) = value

                    override suspend fun valueFromOptionSetAsOptionName(
                        optionSetUid: String,
                        value: String,
                    ) = value

                    override suspend fun valueFromOrgUnitAsOrgUnitName(value: String) = value

                    override suspend fun valueToFileName(value: String) = value

                    override suspend fun valueFromCoordinateAsLatLong(value: String) = value

                    override suspend fun valueFromBooleanType(value: String) = value
                }
            }
        }
