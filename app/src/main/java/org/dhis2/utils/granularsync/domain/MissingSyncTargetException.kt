package org.dhis2.utils.granularsync.domain

class MissingSyncTargetException(
    val recordUid: String,
) : IllegalStateException("Resource not found: $recordUid")