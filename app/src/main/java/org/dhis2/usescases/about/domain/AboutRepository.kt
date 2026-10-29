package org.dhis2.usescases.about.domain

import org.dhis2.usescases.about.model.AboutInfo

interface AboutRepository {
    suspend fun aboutInfo(): AboutInfo
}
