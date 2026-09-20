package com.rada.android.navigation

sealed class RadaDestination(val route: String) {
    data object Splash : RadaDestination("splash")
    data object Onboarding : RadaDestination("onboarding")
    data object Login : RadaDestination("login")
    data object Otp : RadaDestination("otp/{verificationId}/{phoneDisplay}") {
        fun createRoute(verificationId: String, phoneDisplay: String) =
            "otp/$verificationId/$phoneDisplay"
    }
    data object Dashboard : RadaDestination("dashboard")
    data object CompleteProfile : RadaDestination("complete_profile")
    data object Drive : RadaDestination("drive")
    data object Report : RadaDestination("report")
    data object Help : RadaDestination("help")
    data object Settings : RadaDestination("settings")
    data object Paywall : RadaDestination("paywall")
}
