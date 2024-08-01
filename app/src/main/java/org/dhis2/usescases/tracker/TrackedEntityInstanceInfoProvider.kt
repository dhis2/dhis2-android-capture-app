package org.dhis2.usescases.tracker

import org.dhis2.bindings.profilePicturePath
import org.dhis2.commons.date.DateLabelProvider
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.maps.model.MapItemModel
import org.dhis2.maps.model.RelatedInfo
import org.dhis2.maps.model.RelationshipDirection
import org.dhis2.ui.avatar.AvatarProviderConfiguration.ProfilePic
import org.dhis2.utils.ValueUtils
import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.program.Program
import org.hisp.dhis.android.core.relationship.Relationship
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchItem
import org.hisp.dhis.android.core.trackedentity.search.TrackedEntitySearchItemAttribute
import org.hisp.dhis.mobile.ui.designsystem.component.AdditionalInfoItem
import org.hisp.dhis.mobile.ui.designsystem.theme.SurfaceColor

class TrackedEntityInstanceInfoProvider(
    private val d2: D2,
    private val resourceManager: ResourceManager,
    private val dateLabelProvider: DateLabelProvider,
) {
    private val cachedPrograms = mutableMapOf<String, Program>()

    fun getAvatar(
        tei: TrackedEntityInstance,
        programUid: String?,
        firstAttributeValue: AdditionalInfoItem?,
    ) = ProfilePic(
        profilePicturePath = tei.profilePicturePath(d2, programUid),
        firstMainValue = firstAttributeValue?.value?.firstOrNull()?.toString()
            ?: "",
    )

    fun getTeiTitle(
        header: String?,
        attributeValues: List<AdditionalInfoItem>,
    ): String {
        return when {
            header != null -> header
            attributeValues.isEmpty() -> "-"
            else -> with(attributeValues.first()) {
                "$key $value"
            }
        }
    }

    fun getTeiLastUpdated(tei: TrackedEntitySearchItem) = dateLabelProvider.span(tei.lastUpdated)

    fun getTeiAdditionalInfoList(
        attributeValues: List<TrackedEntitySearchItemAttribute>,
    ): List<AdditionalInfoItem> {
        return attributeValues.filter { attribute ->
            attribute.displayInList &&
                !listOf(
                    ValueType.IMAGE,
                    ValueType.FILE_RESOURCE,
                    ValueType.COORDINATE,
                ).contains(attribute.valueType)
        }.map { attribute ->
            AdditionalInfoItem(
                key = attribute.displayFormName,
                value = if (attribute.value != null) {
                    ValueUtils.transformValue(
                        d2,
                        attribute.value,
                        attribute.valueType,
                        attribute.optionSet,
                    )
                } else {
                    ""
                },
            )
        }
    }

    fun getRelatedInfo(
        searchItem: TrackedEntitySearchItem,
        selectedProgram: Program?,
    ): RelatedInfo {
        val lastEnrollmentInProgram = d2.enrollmentModule().enrollments()
            .byTrackedEntityInstance().eq(searchItem.uid)
            .byProgram().eq(selectedProgram?.uid())
            .byDeleted().isFalse
            .orderByEnrollmentDate(RepositoryScope.OrderByDirection.DESC)
            .blockingGet()
            .firstOrNull()

        return RelatedInfo(
            enrollment = lastEnrollmentInProgram?.let {
                RelatedInfo.Enrollment(
                    uid = lastEnrollmentInProgram.uid(),
                    geometry = lastEnrollmentInProgram.geometry(),
                )
            },
        )
    }

    fun updateRelationshipInfo(model: MapItemModel, relationship: Relationship): MapItemModel {
        val relationshipType = d2.relationshipModule().relationshipTypes()
            .uid(relationship.relationshipType())
            .blockingGet()

        val relationshipName: String?
        val relatedUid: String?
        val relationshipDirection: RelationshipDirection?
        when (model.uid) {
            relationship.from()?.elementUid() -> {
                relationshipName = relationshipType?.fromToName()
                relatedUid = relationship.to()?.elementUid()
                relationshipDirection = RelationshipDirection.FROM
            }

            relationship.to()?.elementUid() -> {
                relationshipName = relationshipType?.toFromName()
                relatedUid = relationship.from()?.elementUid()
                relationshipDirection = RelationshipDirection.TO
            }

            else -> {
                relationshipName = null
                relatedUid = null
                relationshipDirection = null
            }
        }
        val relationshipInfo = RelatedInfo.Relationship(
            uid = relationship.uid(),
            displayName = relationshipType?.displayName() ?: "",
            relationshipTypeUid = relationshipType?.uid(),
            relatedUid = relatedUid,
            relationshipDirection = relationshipDirection,
        )
        return model.copy(
            additionalInfoList = model.additionalInfoList +
                AdditionalInfoItem(
                    value = relationshipName ?: "",
                    isConstantItem = true,
                    color = SurfaceColor.Primary,
                ),
            relatedInfo = model.relatedInfo?.copy(
                relationship = relationshipInfo,
            ) ?: RelatedInfo(
                relationship = relationshipInfo,
            ),
        )
    }
}
