package org.dhis2.maps.carousel

internal interface CarouselBinder<T> {
    fun bind(data: T)
    fun showNavigateButton()
    fun hideNavigateButton()
}
