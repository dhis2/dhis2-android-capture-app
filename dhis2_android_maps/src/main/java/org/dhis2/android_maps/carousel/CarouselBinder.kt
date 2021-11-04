package org.dhis2.android_maps.carousel

internal interface CarouselBinder<T> {
    fun bind(data: T)
    fun showNavigateButton()
    fun hideNavigateButton()
}
