package org.dhis2.android.rtsm.utils

import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

object AttributeHelper {

    fun teiAttributeValueByAttributeUid(
        trackedEntityInstance: TrackedEntityInstance,
        attributeUid: String,
    ): String? {
        val attrValues = trackedEntityInstance.trackedEntityAttributeValues()

        return if (attrValues == null || attrValues.isEmpty()) {
            null
        } else {
            attrValues.firstOrNull {
                it.trackedEntityAttribute().equals(attributeUid)
            }?.value()
        }
    }
}
