package org.dhis2.form.ui

class SectionHandler {
    fun getSectionPositionFromVisiblePosition(
        visiblePosition: Int,
        isSection: Boolean,
        sectionPositions: MutableList<Int>,
    ): Int {
        return if (isSection) {
            visiblePosition
        } else {
            var finalPosition = NO_POSITION
            positionLoop@ for (sectionPosition in sectionPositions) {
                if (sectionPosition <= visiblePosition) {
                    finalPosition = sectionPosition
                } else {
                    break@positionLoop
                }
            }
            return finalPosition
        }
    }
}
