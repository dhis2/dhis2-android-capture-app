package org.dhis2.form.model

enum class ValueStoreResult {
    VALUE_CHANGED,
    VALUE_HAS_NOT_CHANGED,
    VALUE_NOT_UNIQUE,
    UID_IS_NOT_DE_OR_ATTR,
    ERROR_UPDATING_VALUE,
    FILE_SAVED,
    TEXT_CHANGING,
    FINISH,
}
