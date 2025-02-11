package org.dhis2.mobile.commons.html

interface HtmlHandler {
    /**
     * Each opening tag must have a following matching closing tag.
     * All tag names must be lower case.
     */
    fun onOpenTag(name: String, attributes: (String) -> String?)

    /**
     * Each closing tag must have a preceding matching opening tag.
     * Tags must be closed in the exact reverse order they were opened.
     * All tag names must be lower case.
     */
    fun onCloseTag(name: String)

    /**
     * HTML entities must be decoded.
     * This method may be called multiple times for a single contiguous text block.
     */
    fun onText(text: String)
}
