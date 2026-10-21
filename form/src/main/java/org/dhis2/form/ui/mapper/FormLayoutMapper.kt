package org.dhis2.form.ui.mapper

import org.dhis2.form.model.FormSection
import org.dhis2.form.model.SectionPlugin
import org.dhis2.mobile.plugin.domain.model.FormLayout
import org.dhis2.mobile.plugin.domain.model.FormLayoutElement

/**
 * These sections with the form field plugins of [layout] placed in them.
 *
 * Sections and fields keep the order the SDK gives them, and none is hidden: maintenance owns the
 * form, and the Tracker Plugin Configurator only places plugins. A plugin goes right after the
 * nearest field before it in its configured section that the form shows. A field a program rule
 * hides does not count, so the plugin stays where it was put. With no such field it goes before the
 * nearest one after it, and with neither at the end of its section. A plugin in a section the form
 * does not have goes next to that field.
 */
fun List<FormSection>.withLayout(layout: FormLayout): List<FormSection> {
    val plugins = mutableMapOf<String, MutableList<SectionPlugin>>()

    layout.sections.forEach { configured ->
        configured.elements.forEachIndexed { index, element ->
            if (element !is FormLayoutElement.Plugin) return@forEachIndexed
            val previous = nearestShownField(configured.elements.take(index).asReversed())
            val next = nearestShownField(configured.elements.drop(index + 1))
            val section =
                firstOrNull { it.uid == configured.id }
                    ?: (previous ?: next)?.let(::sectionShowing)
                    ?: return@forEachIndexed
            val position =
                previous?.let { uid -> section.indexOfField(uid)?.plus(1) }
                    ?: next?.let { uid -> section.indexOfField(uid) }
                    ?: section.fields.size
            plugins.getOrPut(section.uid) { mutableListOf() } += SectionPlugin(element.placement.elementId, position)
        }
    }

    return map { section -> plugins[section.uid]?.let { section.copy(plugins = it) } ?: section }
}

/** The uid of the first of [elements] that is a field the form shows. */
private fun List<FormSection>.nearestShownField(elements: List<FormLayoutElement>): String? =
    elements.filterIsInstance<FormLayoutElement.Field>().firstOrNull { sectionShowing(it.uid) != null }?.uid

private fun List<FormSection>.sectionShowing(fieldUid: String): FormSection? = firstOrNull { it.indexOfField(fieldUid) != null }

private fun FormSection.indexOfField(uid: String): Int? = fields.indexOfFirst { it.uid == uid }.takeIf { it >= 0 }
