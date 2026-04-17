package org.dhis2.utils.granularsync.domain

enum class SyncStatus {
    NOT_SYNCED,
    ERROR,
    WARNING,
    SYNCED,
    RELATIONSHIP,
    UPLOADING,
    SENT_VIA_SMS,
    SYNCED_VIA_SMS,
}