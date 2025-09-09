package org.dhis2.mobile.commons.html

interface HtmlHandler {
    fun onOpenTag(
        name: String,
        attributes: (String) -> String?,
    )

    fun onCloseTag(name: String)

    fun onText(text: String)
}
