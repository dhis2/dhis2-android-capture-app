package org.dhis2.uicomponents.map.carousel

internal interface CarouselBinder<T> {
    fun bind(data: T)
}