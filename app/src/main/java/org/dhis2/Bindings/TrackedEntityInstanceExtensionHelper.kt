package org.dhis2.Bindings

import org.dhis2.utils.cache.AppExpirableCache
import org.hisp.dhis.android.core.D2
import java.util.concurrent.TimeUnit

object TrackedEntityInstanceExtensionHelper {

    private val profileImageCache = AppExpirableCache<Pair<String, List<String>>, List<String>>(TimeUnit.SECONDS.toMillis(5))

    fun getProfilePictureAttributes(d2: D2, programUid: String, imageAttributes: List<String>): List<String> {
        val key = Pair(programUid, imageAttributes)
        return profileImageCache[key]
            ?: internalGetProfilePictureAttributes(d2, programUid, imageAttributes)
                .also { profileImageCache[key] = it }
    }

    private fun internalGetProfilePictureAttributes(d2: D2, programUid: String, imageAttributes: List<String>): List<String> {
        val sections = d2.programModule().programSections().withAttributes().byProgramUid()
            .eq(programUid).blockingGet()
        return if (sections.isEmpty()) {
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
    }
}