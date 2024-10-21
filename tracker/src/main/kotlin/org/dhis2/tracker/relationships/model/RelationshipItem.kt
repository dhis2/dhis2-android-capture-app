package org.dhis2.tracker.relationships.model

import org.dhis2.ui.avatar.AvatarProviderConfiguration

data class RelationshipItem(
    val uid: String,
    val title: String,
    val description: String?,
    val attributes: List<Pair<String, String>>,
    val ownerType: RelationshipOwnerType,
    val ownerUid: String,
    val avatar: AvatarProviderConfiguration,
    val canOpen: Boolean,
    val lastUpdated: String,
)
