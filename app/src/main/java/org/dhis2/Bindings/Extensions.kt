package org.dhis2.Bindings

import android.content.Context
import android.content.res.Resources
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import java.text.DecimalFormat
import org.dhis2.App
import org.dhis2.data.user.UserComponent
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

fun <T : Any?> MutableLiveData<T>.default(initialValue: T) = this.apply { setValue(initialValue) }

fun TrackedEntityInstance.profilePicturePath(d2: D2, programUid: String?): String {
    var path: String? = null

    val attrRepository = d2.trackedEntityModule().trackedEntityAttributes()
    val imageAttributes = if (programUid != null) {
        attrRepository.byValueType().eq(ValueType.IMAGE).blockingGetUids()
    } else {
        attrRepository.byDisplayInListNoProgram().isTrue.byValueType().eq(ValueType.IMAGE)
            .blockingGetUids()
    }

    val imageAttributeValues = d2.trackedEntityModule().trackedEntityAttributeValues()
        .byTrackedEntityInstance().eq(uid())
        .byTrackedEntityAttribute().`in`(imageAttributes)
        .blockingGet()

    if (imageAttributeValues.isEmpty()) {
        return ""
    }

    var attributes = d2.trackedEntityModule().trackedEntityTypeAttributes()
        .byTrackedEntityTypeUid().eq(trackedEntityType())
        .byDisplayInList().isTrue
        .byTrackedEntityAttributeUid().`in`(imageAttributes)
        .bySortOrder().isNotNull
        .blockingGet().map { it.trackedEntityAttribute()?.uid() }

    if (attributes.isEmpty() && programUid != null) {
        val sections = d2.programModule().programSections().withAttributes().byProgramUid()
            .eq(programUid).blockingGet()
        attributes = if (sections.isEmpty()) {
            d2.programModule().programTrackedEntityAttributes()
                .byDisplayInList().isTrue
                .byProgram().eq(programUid)
                .byTrackedEntityAttribute().`in`(imageAttributes)
                .blockingGet().filter { it.trackedEntityAttribute() != null }
                .map { it.trackedEntityAttribute()!!.uid() }
        } else {
            d2.programModule().programSections().withAttributes().byProgramUid().eq(programUid)
                .blockingGet()
                .mapNotNull { section ->
                    section.attributes()?.filter { imageAttributes.contains(it.uid()) }
                        ?.map { it.uid() }
                }.flatten()
        }
    } else if (attributes.isEmpty() && programUid == null) {
        val enrollmentProgramUids = d2.enrollmentModule().enrollments()
            .byTrackedEntityInstance().eq(uid())
            .blockingGet().map { it.program() }.distinct()
        attributes = d2.programModule().programTrackedEntityAttributes()
            .byDisplayInList().isTrue
            .byProgram().`in`(enrollmentProgramUids)
            .byTrackedEntityAttribute().`in`(imageAttributes)
            .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
            .blockingGet().filter { it.trackedEntityAttribute() != null }
            .map { it.trackedEntityAttribute()!!.uid() }
    }

    val attributeValue = attributes.firstOrNull()?.let { attributeUid ->
        imageAttributeValues.find { it.trackedEntityAttribute() == attributeUid }
    }

    if (attributeValue?.value() != null) {
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

fun Fragment.userComponent(): UserComponent? {
    return app().userComponent()
}

fun AppCompatActivity.app(): App {
    return applicationContext as App
}

fun AppCompatActivity.userComponent(): UserComponent? {
    return app().userComponent()
}

fun Context.app(): App {
    return applicationContext as App
}

fun Context.drawableFrom(@DrawableRes drawableResource: Int): Drawable? {
    return AppCompatResources.getDrawable(this, drawableResource)
}

val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

val Int.px: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

val Double.decimalFormat: String
    get() = DecimalFormat("0.##############").format(this)

fun AppCompatActivity.isKeyboardOpened(): Boolean {
    val r = Rect()
    val keyboardVisibilityThreshold = 100

    val activityRoot = (findViewById<ViewGroup>(android.R.id.content)).getChildAt(0)
    val visibleThreshold = keyboardVisibilityThreshold.dp

    if (activityRoot == null) return false
    activityRoot.getWindowVisibleDisplayFrame(r)

    val heightDiff = activityRoot.rootView.height - r.height()

    return heightDiff > visibleThreshold
}
