package org.dhis2.commons.components

import org.dhis2.commons.sync.SyncComponentProvider

interface ComponentProvider {
    val syncComponentProvider: SyncComponentProvider
}
