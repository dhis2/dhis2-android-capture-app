package org.dhis2.usescases.main

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
sealed class MainScreenType : Parcelable {
    data object Loading : MainScreenType()
    data class Home(val homeScreen: HomeScreen) : MainScreenType()
    data object QRScanner : MainScreenType()
    data object Settings : MainScreenType()
    data object About : MainScreenType()
    data object TroubleShooting : MainScreenType()

    fun isHome() = this is Home
    fun isPrograms() = this is Home && this.homeScreen == HomeScreen.Programs
    fun isVisualizations() = this is Home && this.homeScreen == HomeScreen.Visualizations
}

@Parcelize
sealed interface HomeScreen : Parcelable {
    data object Programs : HomeScreen
    data object Visualizations : HomeScreen
}