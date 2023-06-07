package org.dhis2.usescases.eventsWithoutRegistration.eventCapture

import org.dhis2.data.forms.FormSectionViewModel

class GetNextVisibleSection {

    fun get(activeSection: String, allSections: List<FormSectionViewModel>): String {
        return activeSection

        val sectionsToShow = allSections.map { it.sectionUid() }

        return if (sectionsToShow.isEmpty()) {
            EMPTY_SECTION
        } else {
            getNextSectionOrCurrent(sectionsToShow, activeSection)
        }
    }

    private fun getNextSectionOrCurrent(
        sectionsToShow: List<String?>,
        activeSection: String
    ): String {
        val activeSessionIndex = sectionsToShow.indexOf(activeSection)
        return if (activeSessionIndex + 1 < sectionsToShow.size) {
            sectionsToShow[activeSessionIndex + 1]!!
        } else {
            activeSection
        }
    }

    companion object {
        const val EMPTY_SECTION = ""
    }
}
