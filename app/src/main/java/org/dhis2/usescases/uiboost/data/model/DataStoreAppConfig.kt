package org.dhis2.usescases.uiboost.data.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.serialization.Serializable
import org.dhis2.usescases.uiboost.data.model.DataStoreAppConfig.Mapper.translateJsonToObject

@Serializable
data class DataStoreAppConfig(
    val key: String,
    val programGroups: List<ProgramGroup>
) {
    private fun toJson(): String = translateJsonToObject().writeValueAsString(this)

    companion object {
        fun fromJson(json: String?): DataStoreAppConfig? = if (json != null) {
            translateJsonToObject()
                .readValue(json, DataStoreAppConfig::class.java)
        } else {
            null
        }
    }

    override fun toString(): String {
        return toJson()
    }

    object Mapper {
        fun translateJsonToObject(): ObjectMapper {
            return jacksonObjectMapper().apply {
                propertyNamingStrategy = PropertyNamingStrategy.LOWER_CAMEL_CASE
                setSerializationInclusion(JsonInclude.Include.NON_NULL)
            }
        }
    }
}