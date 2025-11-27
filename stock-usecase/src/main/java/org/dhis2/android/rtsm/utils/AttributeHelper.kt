package org.dhis2.android.rtsm.utils

import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

object AttributeHelper {
    fun teiAttributeValueByAttributeUid(
        trackedEntityInstance: TrackedEntityInstance,
        attributeUid: String,
        isOptionSet: Boolean,
        toOptionName: (optionCode: String) -> String,
    ): String? {
        val attrValues = trackedEntityInstance.trackedEntityAttributeValues()

        return if (attrValues == null || attrValues.isEmpty()) {
            null
        } else if (isOptionSet) {
            attrValues
                .firstOrNull {
                    it.trackedEntityAttribute().equals(attributeUid)
                }?.value()
                ?.let { code ->
                    toOptionName(code)
                }
        } else {
            attrValues
                .firstOrNull {
                    it.trackedEntityAttribute().equals(attributeUid)
                }?.value()
        }
    }
}
