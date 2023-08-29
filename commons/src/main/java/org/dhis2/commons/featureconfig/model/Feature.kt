package org.dhis2.commons.featureconfig.model

enum class Feature(val description: String) {
    SINGLE_TRACKER_HOME_ITEM("Returns one tracker program in home"),
    SINGLE_LMIS_HOME_ITEM("Returns one lmis program in home"),
    SINGLE_EVENT_HOME_ITEM("Returns one event program in home"),
    SINGLE_DATASET_HOME_ITEM("Returns one data set in home"),
    COMPOSE_FORMS("Use compose in forms"),
    DISABLE_COLLAPSIBLE_SECTIONS("Disable collapsible sections"),
}
