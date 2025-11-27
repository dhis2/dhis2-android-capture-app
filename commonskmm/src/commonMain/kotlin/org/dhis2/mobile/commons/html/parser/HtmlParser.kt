package org.dhis2.mobile.commons.html.parser

import org.dhis2.mobile.commons.html.HtmlHandler
import org.kobjects.ktxml.api.EventType
import org.kobjects.ktxml.api.XmlPullParser
import org.kobjects.ktxml.mini.MiniXmlPullParser

class HtmlParser(
    private val html: CharIterator,
) {
    fun parse(handler: HtmlHandler) {
        val parser: XmlPullParser =
            MiniXmlPullParser(
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
                    tagStack.add(lowerCaseName)
                }

                EventType.END_TAG -> {
                    val name = parser.name

                    val stackPosition =
                        tagStack.indexOfLast { it.equals(name, ignoreCase = true) }
                    if (stackPosition != -1) {
                        // Also close all unclosed child tags, if any
                        for (i in tagStack.lastIndex downTo stackPosition) {
                            handler.onCloseTag(tagStack.removeAt(i))
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
}
