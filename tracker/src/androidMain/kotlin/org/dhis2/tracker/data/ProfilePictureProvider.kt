package org.dhis2.tracker.data

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

class ProfilePictureProvider(
    private val d2: D2,
) {
    operator fun invoke(
        tei: TrackedEntityInstance,
        programUid: String?,
    ): String {
        var path = ""

        val attrRepository = d2.trackedEntityModule().trackedEntityAttributes()
        val imageAttributes =
            if (programUid != null) {
                attrRepository.byValueType().eq(ValueType.IMAGE).blockingGetUids()
            } else {
                attrRepository
                    .byDisplayInListNoProgram()
                    .isTrue
                    .byValueType()
                    .eq(ValueType.IMAGE)
                    .blockingGetUids()
            }

        val imageAttributeValues =
            d2
                .trackedEntityModule()
                .trackedEntityAttributeValues()
                .byTrackedEntityInstance()
                .eq(tei.uid())
                .byTrackedEntityAttribute()
                .`in`(imageAttributes)
                .blockingGet()

        if (imageAttributeValues.isEmpty()) {
            return path
        }

        var attributes =
            d2
                .trackedEntityModule()
                .trackedEntityTypeAttributes()
                .byTrackedEntityTypeUid()
                .eq(tei.trackedEntityType())
                .byDisplayInList()
                .isTrue
                .byTrackedEntityAttributeUid()
                .`in`(imageAttributes)
                .bySortOrder()
                .isNotNull
                .blockingGet()
                .map { it.trackedEntityAttribute()?.uid() }

        if (attributes.isEmpty() && programUid != null) {
            val sections =
                d2
                    .programModule()
                    .programSections()
                    .withAttributes()
                    .byProgramUid()
                    .eq(programUid)
                    .blockingGet()
            attributes =
                if (sections.isEmpty()) {
                    d2
                        .programModule()
                        .programTrackedEntityAttributes()
                        .byDisplayInList()
                        .isTrue
                        .byProgram()
                        .eq(programUid)
                        .byTrackedEntityAttribute()
                        .`in`(imageAttributes)
                        .blockingGet()
                        .filter { it.trackedEntityAttribute() != null }
                        .map { it.trackedEntityAttribute()!!.uid() }
                } else {
                    d2
                        .programModule()
                        .programSections()
                        .withAttributes()
                        .byProgramUid()
                        .eq(programUid)
                        .blockingGet()
                        .mapNotNull { section ->
                            section
                                .attributes()
                                ?.filter { imageAttributes.contains(it.uid()) }
                                ?.map { it.uid() }
                        }.flatten()
                }
        } else if (attributes.isEmpty() && programUid == null) {
            val enrollmentProgramUids =
                d2
                    .enrollmentModule()
                    .enrollments()
                    .byTrackedEntityInstance()
                    .eq(tei.uid())
                    .blockingGet()
                    .mapNotNull { it.program() }
                    .distinct()
            attributes =
                d2
                    .programModule()
                    .programTrackedEntityAttributes()
                    .byDisplayInList()
                    .isTrue
                    .byProgram()
                    .`in`(enrollmentProgramUids)
                    .byTrackedEntityAttribute()
                    .`in`(imageAttributes)
                    .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
                    .blockingGet()
                    .filter { it.trackedEntityAttribute() != null }
                    .map { it.trackedEntityAttribute()!!.uid() }
        }

        val attributeValue =
            attributes.firstOrNull()?.let { attributeUid ->
                imageAttributeValues.find { it.trackedEntityAttribute() == attributeUid }
            }

        if (attributeValue?.value() != null) {
            val fileResource =
                d2
                    .fileResourceModule()
                    .fileResources()
                    .uid(attributeValue.value())
                    .blockingGet()
            if (fileResource != null) {
                path = fileResource.path() ?: ""
            }
        }

        return path
    }
}
