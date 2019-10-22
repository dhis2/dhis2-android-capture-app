package org.dhis2.Bindings

import android.view.MotionEvent
import android.widget.EditText
import androidx.lifecycle.MutableLiveData
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

/**
 * QUADRAM. Created by ppajuelo on 11/04/2019.
 */
fun <T : Any?> MutableLiveData<T>.default(initialValue: T) = this.apply { setValue(initialValue) }

fun EditText.onRightDrawableClicked(onClicked: (view: EditText) -> Unit) {
    this.setOnTouchListener { v, event ->
        var hasConsumed = false
        if (v is EditText) {
            if (event.x >= v.width - v.totalPaddingRight) {
                if (event.action == MotionEvent.ACTION_UP) {
                    onClicked(this)
                }
                hasConsumed = true
            }
        }
        hasConsumed
    }
}

fun TrackedEntityInstance.profilePicturePath(d2: D2, programUid: String?): String {
    var path: String? = null

    val attrRepository = d2.trackedEntityModule().trackedEntityAttributes()
    val imageAttributes = if (programUid != null) {
        attrRepository.byValueType().eq(ValueType.IMAGE).blockingGet().map { it.uid() }
    } else {
        attrRepository.byDisplayInListNoProgram().isTrue.byValueType().eq(ValueType.IMAGE)
            .blockingGet().map { it.uid() }
    }

    var attributes = d2.trackedEntityModule().trackedEntityTypeAttributes()
        .byTrackedEntityTypeUid().eq(trackedEntityType())
        .byDisplayInList().isTrue
        .byTrackedEntityAttributeUid().`in`(imageAttributes)
        .blockingGet().map { it.trackedEntityAttribute()?.uid() }

    if (attributes.isEmpty() && programUid != null) {
        attributes = d2.programModule().programTrackedEntityAttributes()
            .byDisplayInList().isTrue
            .byProgram().eq(programUid)
            .byTrackedEntityAttribute().`in`(imageAttributes)
            .blockingGet().filter { it.trackedEntityAttribute() != null }
            .map { it.trackedEntityAttribute()!!.uid() }
    }

    val attributeValue = d2.trackedEntityModule().trackedEntityAttributeValues()
        .byTrackedEntityInstance().eq(uid())
        .byTrackedEntityAttribute().`in`(attributes)
        .byValue().isNotNull
        .one().blockingGet()
    if (attributeValue != null) {
        val fileResource =
            d2.fileResourceModule().fileResources().uid(attributeValue.value()).blockingGet()
        if (fileResource != null) {
            path = fileResource.path()
        }
    }

    return path ?: ""
}
