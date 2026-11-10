package org.dhis2.mobile.extension.host.di

/** What depends on the platform and the build; provided by the platform module. */
class ExtensionHostConfig(
    /**
     * SPIKE ONLY: true in debug builds, so a local http test server can show custom forms.
     * The real development has no exception to the https rule.
     */
    val allowHttp: Boolean,
    /** Where a failure is logged, so a developer sees why the native form opened. */
    val log: (String) -> Unit,
)
