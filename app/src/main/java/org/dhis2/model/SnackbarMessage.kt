package org.dhis2.model

import java.util.UUID

data class SnackbarMessage(
    val id: UUID = UUID.randomUUID(),
    val message: String = "",
)
