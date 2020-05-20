package org.dhis2.uicomponents.map.geometry.mapper

interface Mapper<in T, out E> {
    fun mapTo(item: T): E
}
