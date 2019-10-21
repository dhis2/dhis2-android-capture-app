package org.dhis2.data.fingerprint

class FingerPrintResult (val type: Type, val message: String?)

enum class Type {
    SUCCESS,
    INFO,
    ERROR
}

enum class Reason {
    HARDWARE_UNAVAILABLE,
    UNABLE_TO_PROCESS,
    TIMEOUT,
    NO_SPACE,
    CANCELED,
    LOCKOUT,
    VENDOR,
    LOCKOUT_PERMANENT,
    USER_CANCELED,
    GOOD,
    PARTIAL,
    INSUFFICIENT,
    IMAGER_DIRTY,
    TOO_SLOW,
    TOO_FAST,
    AUTHENTICATION_START,
    AUTHENTICATION_SUCCESS,
    AUTHENTICATION_FAIL,
    UNKNOWN
}