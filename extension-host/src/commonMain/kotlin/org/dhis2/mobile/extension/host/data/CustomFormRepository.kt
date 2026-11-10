package org.dhis2.mobile.extension.host.data

/** Reads a data set's custom form from the SDK (B2). Read when the data set opens, never cached. */
interface CustomFormRepository {
    /** The form's HTML, or null when the data set's form type is not CUSTOM. */
    suspend fun customFormHtml(dataSetUid: String): String?

    /**
     * Whether the forms were downloaded over https. Over http a form could be altered in
     * transit, so it is not run. False when the server address is unknown.
     */
    suspend fun serverUsesHttps(): Boolean
}
