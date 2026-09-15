package org.dhis2.mobile.plugin.sdk

/**
 * One attribute a tracked entity should be shown under, and the label to show it with.
 *
 * Plain data, in `commonMain`, so a plugin's `commonMain` state and Composables can hold it without
 * reaching for the SDK — which is what keeps them unit-testable and previewable.
 *
 * @property label What a human should read: the attribute's form name, or its display name.
 * @property value The tracked entity's value for that attribute.
 */
data class LabelledAttribute(
    val label: String,
    val value: String,
)

/**
 * An attribute a programme lists a tracked entity under, resolved from metadata.
 *
 * Carries the label so the values can be labelled without a second metadata lookup per tracked
 * entity. Resolve these once for a programme, then apply them to many tracked entities.
 *
 * @property uid The tracked entity attribute's uid.
 * @property label What a human should read for it.
 */
data class DisplayAttribute(
    val uid: String,
    val label: String,
)
