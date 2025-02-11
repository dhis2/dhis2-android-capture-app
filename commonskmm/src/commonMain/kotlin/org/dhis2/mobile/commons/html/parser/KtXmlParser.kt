package org.dhis2.mobile.commons.html.parser

import org.dhis2.mobile.commons.html.HtmlHandler
import org.dhis2.mobile.commons.html.HtmlParser
import org.kobjects.ktxml.api.EventType
import org.kobjects.ktxml.api.XmlPullParser
import org.kobjects.ktxml.mini.MiniXmlPullParser

class KtXmlParser(private val html: CharIterator) : HtmlParser {
    override fun parse(handler: HtmlHandler) {
        val parser: XmlPullParser = MiniXmlPullParser(
            source = html,
            relaxed = true,
        )
        val attributes = { name: String -> parser.getAttributeValue("", name) }
        val tagStack = mutableListOf<String>()

        while (true) {
            when (parser.next()) {
                EventType.START_TAG -> {
                    val lowerCaseName = parser.name.lowercase()
                    handler.onOpenTag(lowerCaseName, attributes)
                    if (lowerCaseName == "br" || lowerCaseName == "hr" || lowerCaseName == "img") {
                        // Special case for unpaired tags: closing event is notified immediately
                        handler.onCloseTag(lowerCaseName)
                        if (parser.isEmptyElementTag) {
                            parser.next()
                        }
                    } else {
                        tagStack.add(lowerCaseName)
                    }
                }

                EventType.END_TAG -> {
                    val name = parser.name
                    if (name.equals("br", ignoreCase = true)) {
                        // A closing BR tag is interpreted as a self-closing BR tag
                        handler.onOpenTag("br", EMPTY_ATTRIBUTES)
                        handler.onCloseTag("br")
                    } else {
                        val stackPosition =
                            tagStack.indexOfLast { it.equals(name, ignoreCase = true) }
                        if (stackPosition != -1) {
                            // Also close all unclosed child tags, if any
                            for (i in tagStack.lastIndex downTo stackPosition) {
                                handler.onCloseTag(tagStack.removeAt(i))
                            }
                        }
                    }
                }

                EventType.TEXT -> handler.onText(parser.text)
                EventType.END_DOCUMENT -> break
                else -> {}
            }
        }

        // Close remaining open tags, if any
        for (i in tagStack.lastIndex downTo 0) {
            handler.onCloseTag(tagStack[i])
        }
    }

    companion object {
        private val EMPTY_ATTRIBUTES: (String) -> String? = { null }
    }
}
