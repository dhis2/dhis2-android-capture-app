package org.dhis2.form.data

import org.dhis2.form.model.StoreResult
import org.dhis2.form.model.ValueStoreResult
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityDataValueObjectRepository

class FormEventsRepository(
    private val repositoryImpl: FormRepositoryImpl,
    private val d2: D2,
    private val recordUid: String
) : FormRepository by repositoryImpl {

    init {
        repositoryImpl.storeValue = { uid: String, value: String?, _: String? ->
            storeDataElement(uid, value)
        }
    }

    private fun storeDataElement(uid: String, value: String?): StoreResult {

        val valueRepository = d2.trackedEntityModule().trackedEntityDataValues()
            .value(recordUid, uid)
        val valueType = d2.dataElementModule().dataElements().uid(uid).blockingGet().valueType()
        var newValue = repositoryImpl.withValueTypeCheck(value, valueType) ?: ""
        if (valueType == ValueType.IMAGE && value != null) {
            newValue = repositoryImpl.saveFileResource(value)
        }

        val currentValue = if (valueRepository.blockingExists()) {
            repositoryImpl.withValueTypeCheck(valueRepository.blockingGet().value(), valueType)
        } else {
            ""
        }

        return if (currentValue != newValue) {
            if (!value.isNullOrEmpty()) {
                if (setCheck(valueRepository, uid, newValue)) {
                    StoreResult(uid, ValueStoreResult.VALUE_CHANGED)
                } else {
                    StoreResult(uid, ValueStoreResult.VALUE_HAS_NOT_CHANGED)
                }
            } else {
                valueRepository.blockingDeleteIfExist()
                StoreResult(uid, ValueStoreResult.VALUE_CHANGED)
            }
        } else {
            StoreResult(uid, ValueStoreResult.VALUE_HAS_NOT_CHANGED)
        }
    }

    private fun setCheck(
        valueRepository: TrackedEntityDataValueObjectRepository,
        deUid: String,
        value: String
    ): Boolean {
        return d2.dataElementModule().dataElements().uid(deUid).blockingGet().let {
            if (check(it.valueType(), it.optionSet()?.uid(), value)) {
                val finalValue = repositoryImpl.assureCodeForOptionSet(it.optionSet()?.uid(), value)
                valueRepository.blockingSet(finalValue)
                true
            } else {
                valueRepository.blockingDeleteIfExist()
                false
            }
        }
    }

    private fun check(
        valueType: ValueType?,
        optionSetUid: String?,
        value: String
    ): Boolean {
        return when {
            optionSetUid != null -> {
                val optionByCodeExist =
                    d2.optionModule().options().byOptionSetUid().eq(optionSetUid)
                        .byCode().eq(value).one().blockingExists()
                val optionByNameExist =
                    d2.optionModule().options().byOptionSetUid().eq(optionSetUid)
                        .byDisplayName().eq(value).one().blockingExists()
                optionByCodeExist || optionByNameExist
            }
            valueType != null -> {
                if (valueType.isNumeric) {
                    try {
                        value.toFloat().toString()
                        true
                    } catch (e: Exception) {
                        false
                    }
                } else {
                    when (valueType) {
                        ValueType.FILE_RESOURCE, ValueType.IMAGE ->
                            d2.fileResourceModule().fileResources()
                                .byUid().eq(value).one().blockingExists()
                        ValueType.ORGANISATION_UNIT ->
                            d2.organisationUnitModule().organisationUnits().uid(value)
                                .blockingExists()
                        else -> true
                    }
                }
            }
            else -> false
        }
    }
}
