package org.dhis2.Bindings

import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import org.dhis2.App
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

fun <T : Any?> MutableLiveData<T>.default(initialValue: T) = this.apply { setValue(initialValue) }

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

fun Fragment.app(): App {
    return context?.applicationContext as App
}

fun AppCompatActivity.app(): App {
    return applicationContext as App
}

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()