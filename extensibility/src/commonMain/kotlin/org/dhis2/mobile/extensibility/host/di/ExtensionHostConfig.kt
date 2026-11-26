package org.dhis2.mobile.extensibility.host.di

/** What depends on the platform and the build; provided by the platform module. */
class ExtensionHostConfig(
    /** Where a failure is logged, so a developer sees why the native form opened. */
    val log: (String) -> Unit,
)
