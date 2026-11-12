package org.dhis2.mobile.extension.host.domain

import org.dhis2.mobile.extension.host.model.FailureReason

/**
 * A per-kind check that runs before an extension is shown. No general versioning scheme: web
 * content has no host version to compare against, so each rule answers "usable or not, and why".
 */
fun interface CompatibilityRule<in C> {
    fun check(content: C): FailureReason?
}

/**
 * Static scan of a custom form's HTML for legacy `dhis2.*` calls the shim does not provide yet.
 * Cheap and predictable; anything that slips through fails at runtime.
 *
 * The supported list is a starting point: the survey of real forms supplies the real one.
 */
class LegacyApiScan(
    private val supported: Set<String> = DEFAULT_SUPPORTED,
) : CompatibilityRule<String> {
    override fun check(content: String): FailureReason? =
        if (unsupportedCalls(content).isEmpty()) null else FailureReason.UNSUPPORTED_FEATURE

    /** Every `dhis2.de.*` and `dhis2.util.*` name the form mentions. */
    fun calls(content: String): Set<String> = CALL.findAll(content).map { it.groupValues[1] }.toSet()

    /** The calls the scan found that are not supported; for logs and the survey. */
    fun unsupportedCalls(content: String): Set<String> = calls(content) - supported

    companion object {
        private val CALL = Regex("""\b(dhis2\.(?:de|util)\.[A-Za-z_][\w.]*)""")

        /** What `custom-form.js` in `extension-web` provides. Keep the two in step. */
        val DEFAULT_SUPPORTED =
            setOf(
                "dhis2.util.on",
                "dhis2.de.event.formLoaded",
                "dhis2.de.event.dataValuesLoaded",
                "dhis2.de.event.formReady",
                "dhis2.de.event.dataValueSaved",
                "dhis2.de.event.completed",
                "dhis2.de.event.uncompleted",
                "dhis2.de.event.validationSucces",
                "dhis2.de.event.validationError",
                "dhis2.de.currentDataSetId",
                "dhis2.de.currentOrganisationUnitId",
                "dhis2.de.currentPeriodId",
                "dhis2.de.currentAttributeOptionComboId",
            )
    }
}
