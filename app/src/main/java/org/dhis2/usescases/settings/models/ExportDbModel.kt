package org.dhis2.usescases.settings.models

import java.io.File
import java.util.UUID

data class ExportDbModel(
    val id: UUID = UUID.randomUUID(),
    val file: File,
)
