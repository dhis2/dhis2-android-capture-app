package org.dhis2.utils.rules

data class RuleEffectResult(
        val fields : ArrayList<String> = ArrayList(),
        val warnings: HashMap<String, String> = HashMap(),
        val errors: HashMap<String, String> = HashMap(),
        val displayTextList: ArrayList<String> = ArrayList(),
        val displayKeyValue: HashMap<String, String> = HashMap(),
        val sectionsToHide: ArrayList<String> = ArrayList(),
        val mandatoryFields: ArrayList<String> = ArrayList(),
        val warningOnCompletions: HashMap<String, String> = HashMap(),
        val errorOnCompletions: HashMap<String, String> = HashMap(),
        val stagesToHide: ArrayList<String> = ArrayList(),
        val optionsToHide: ArrayList<String> = ArrayList(),
        val optionGroupsToHide: ArrayList<String> = ArrayList(),
        val showOptionGroup: ArrayList<String> = ArrayList(),
        val unsupportedRules: ArrayList<String> = ArrayList()
)