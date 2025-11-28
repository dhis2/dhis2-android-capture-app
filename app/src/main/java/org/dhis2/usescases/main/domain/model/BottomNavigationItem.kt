package org.dhis2.usescases.main.domain.model

sealed interface BottomNavigationItem {
    data object Program : BottomNavigationItem

    data object Analytics : BottomNavigationItem
}
