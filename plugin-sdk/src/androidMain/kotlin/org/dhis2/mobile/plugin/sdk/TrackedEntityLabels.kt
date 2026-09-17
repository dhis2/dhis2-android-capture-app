package org.dhis2.mobile.plugin.sdk

import org.hisp.dhis.android.core.D2
import org.hisp.dhis.android.core.arch.repositories.scope.RepositoryScope
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

/**
 * Labels tracked entities the way the app that hosts you labels them.
 *
 * A tracked entity's attribute values come back in **no particular order**, and none of them is a
 * "primary" attribute. So both obvious readings produce nonsense: taking the first value with
 * something in it renders a person labelled *Female*, and rendering all of them renders
 * *Gender: Female / First name: Filona / Last name: Ryder*. Neither is what the user just saw in the
 * app's own list, and a plugin that disagrees with that list is worse than one showing nothing.
 *
 * The rule, which mirrors the Capture App's own `TeiAttributesProvider`: the attributes the
 * **programme** marks `displayInList`, in their configured sort order; failing that, the ones the
 * **tracked entity type** marks `displayInList`; failing that, something a human still recognises —
 * the org unit's name. Never a UID.
 *
 * Build one per query, not one per row — resolving the metadata is what costs:
 *
 * ```kotlin
 * val labeller = d2.trackedEntityLabeller(programUid)   // metadata, once
 * rows.map { labeller.labelsFor(it) }                   // per tracked entity, no further queries
 * ```
 *
 * [trackedEntityLabeller] is **blocking**; call it off the main thread. The labeller it returns is
 * pure and holds no `D2`.
 *
 * One caveat worth knowing: `sortOrder` is nullable on both link types, so a programme that leaves
 * it unset has no total order and the sequence is the database's choice.
 */
public class TrackedEntityLabeller(
    private val attributes: List<DisplayAttribute>,
    private val fallback: (TrackedEntityInstance) -> LabelledAttribute?,
) {
    // The constructor is public, and the fallback is a lambda, for one reason: a plugin author has
    // to be able to build one of these in a unit test. Resolving the fallback inside the class
    // would need a D2, and a D2 needs an Android context, a database and an HTTP stack — so the
    // labelling of a tracked entity would only ever be checkable on a device. Injecting it is what
    // keeps the interesting half on the JVM.

    /**
     * [tei]'s values for the listed attributes, labelled and in the programme's order.
     *
     * Values the entity has not got are skipped rather than rendered blank. When that leaves
     * nothing — a programme listing no attributes, or an entity with none of them filled in — the
     * fallback stands in, so a row is never empty and never a UID.
     */
    public fun labelsFor(tei: TrackedEntityInstance): List<LabelledAttribute> =
        labelled(tei, attributes).ifEmpty { listOfNotNull(fallback(tei)) }

    /**
     * The same thing as one string — a name, which is how the app titles a tracked entity.
     *
     * Use this when you want *Filona Ryder*; use [labelsFor] when you want the values under their
     * labels.
     */
    public fun labelFor(
        tei: TrackedEntityInstance,
        separator: String = " ",
    ): String = labelsFor(tei).joinToString(separator) { it.value }
}

/**
 * Reads [programUid]'s list attributes and their labels, then hands back a labeller for them.
 *
 * Blocking, and does its metadata reads once: two queries for the attributes, plus one org unit
 * lookup per distinct org unit only if the fallback is ever reached.
 */
public fun D2.trackedEntityLabeller(programUid: String): TrackedEntityLabeller {
    val uids =
        programDisplayAttributeUids(programUid)
            .ifEmpty { typeDisplayAttributeUids(programUid) }

    val labels = if (uids.isEmpty()) emptyMap() else labelsByUid(uids)

    // Ordered by the metadata, not by the label lookup — associating into a map first is how the
    // sort order gets thrown away again, which is the whole thing this exists to prevent.
    val attributes = uids.map { uid -> DisplayAttribute(uid = uid, label = labels[uid] ?: uid) }

    // Memoised: a programme that lists no attributes sends *every* row down this path, and one
    // query per row to name the same clinic is the cost this whole class is trying to avoid.
    val orgUnitNames = mutableMapOf<String, String?>()
    return TrackedEntityLabeller(attributes) { tei ->
        val orgUnitUid = tei.organisationUnit() ?: return@TrackedEntityLabeller null
        val name =
            orgUnitNames.getOrPut(orgUnitUid) {
                organisationUnitModule()
                    .organisationUnits()
                    .uid(orgUnitUid)
                    .blockingGet()
                    ?.let { it.displayName() ?: it.name() }
            }
        name?.let { LabelledAttribute(label = "Organisation unit", value = it) }
    }
}

/**
 * The pure half: match [attributes] against [tei]'s values, keeping the order of [attributes].
 *
 * Separated from the querying so it can be unit-tested on the JVM — a `D2` needs an Android
 * context, a database and an HTTP stack, but a `TrackedEntityInstance` builds from the SDK's own
 * builders. Every label defect seen so far has been here rather than in the query.
 */
internal fun labelled(
    tei: TrackedEntityInstance,
    attributes: List<DisplayAttribute>,
): List<LabelledAttribute> {
    val values =
        tei
            .trackedEntityAttributeValues()
            .orEmpty()
            .mapNotNull { value ->
                val uid = value.trackedEntityAttribute() ?: return@mapNotNull null
                val text = value.value()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                uid to text
            }.toMap()

    return attributes.mapNotNull { attribute ->
        values[attribute.uid]?.let { LabelledAttribute(attribute.label, it) }
    }
}

/** What the programme itself lists, in the order it lists it. */
private fun D2.programDisplayAttributeUids(programUid: String): List<String> =
    programModule()
        .programTrackedEntityAttributes()
        .byProgram()
        .eq(programUid)
        .byDisplayInList()
        .isTrue
        .orderBySortOrder(RepositoryScope.OrderByDirection.ASC)
        .blockingGet()
        .mapNotNull { it.trackedEntityAttribute()?.uid() }

/** The tracked entity type's list attributes, for a programme that declares none of its own. */
private fun D2.typeDisplayAttributeUids(programUid: String): List<String> {
    val trackedEntityType =
        programModule()
            .programs()
            .uid(programUid)
            .blockingGet()
            ?.trackedEntityType()
            ?.uid()
            ?: return emptyList()

    // This repository has no orderBySortOrder, unlike the programme one, so the sort happens here.
    return trackedEntityModule()
        .trackedEntityTypeAttributes()
        .byTrackedEntityTypeUid()
        .eq(trackedEntityType)
        .byDisplayInList()
        .isTrue
        .blockingGet()
        .sortedBy { it.sortOrder() ?: Int.MAX_VALUE }
        .map { it.trackedEntityAttribute().uid() }
}

/** Attribute uid to the label a human should read, preferring the form name the programme shows. */
private fun D2.labelsByUid(uids: List<String>): Map<String, String> =
    trackedEntityModule()
        .trackedEntityAttributes()
        .byUid()
        .`in`(uids)
        .blockingGet()
        .associate { attribute ->
            attribute.uid() to (
                attribute.displayFormName()
                    ?: attribute.displayName()
                    ?: attribute.uid()
            )
        }
