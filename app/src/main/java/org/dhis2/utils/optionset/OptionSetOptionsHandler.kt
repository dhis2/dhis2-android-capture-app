package org.dhis2.utils.optionset

class OptionSetOptionsHandler(
    private val optionsToHide: List<String>?,
    private val optionGroupsToShow: List<String>?,
    private val optionGroupsToHide: List<String>?,
) {
    fun handleOptions(): Pair<List<String>, List<String>> {
        val finalOptionsToHide: MutableList<String> = mutableListOf()
        val finalOptionsToShow: MutableList<String> = mutableListOf()

        if (optionsToHide?.isNotEmpty() == true) {
            finalOptionsToHide.addAll(optionsToHide)
        }

        if (optionGroupsToShow?.isNotEmpty() == true) {
            finalOptionsToShow.addAll(optionGroupsToShow)
        }

        if (optionGroupsToHide?.isNotEmpty() == true) {
            finalOptionsToHide.addAll(optionGroupsToHide)
        }

        return Pair(finalOptionsToHide, finalOptionsToShow)
    }
}
