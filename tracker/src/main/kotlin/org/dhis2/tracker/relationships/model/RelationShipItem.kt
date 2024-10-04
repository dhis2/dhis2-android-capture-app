package org.dhis2.tracker.relationships.model

import org.dhis2.commons.data.RelationshipOwnerType
import org.dhis2.ui.avatar.AvatarProviderConfiguration

data class RelationShipItem(
    val title: String,
    val attributes: List<Pair<String, String>>,
    val ownerType: RelationshipOwnerType,
    val ownerUid: String,
    val avatar: AvatarProviderConfiguration,
    val canOpen: Boolean,
)
