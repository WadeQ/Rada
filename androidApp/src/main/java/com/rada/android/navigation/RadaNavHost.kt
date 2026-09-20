package com.rada.android.navigation

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rada.android.features.dashboard.DashboardRouteScreen
import com.rada.android.features.drive.DriveRouteScreen
import com.rada.android.features.help.HelpRouteScreen
import com.rada.android.features.login.LoginRouteScreen
import com.rada.android.features.onboarding.OnboardingRouteScreen
import com.rada.android.features.otp.OtpRouteScreen
import com.rada.android.features.paywall.PaywallRouteScreen
import com.rada.android.features.profile.CompleteProfileRouteScreen
import com.rada.android.features.report.ReportRouteScreen
import com.rada.android.features.settings.SettingsRouteScreen
import com.rada.android.features.splash.SplashRouteScreen
import com.rada.android.screens.RadaBottomBar
import com.rada.android.screens.RadaTab

private fun NavController.navigateToTab(route: String) {
    navigate(route) {
        popUpTo(RadaDestination.Dashboard.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun RadaNavHost() {
    val navController = rememberNavController()
    // isPro mirrors the prototype's in-memory flag — no persistence yet, resets on relaunch.
    // Replace with a real subscription-status check once Paywall has a real payment backend
    // (see PaymentRepository.kt) — Play Billing if this ever sells through Play, or a check
    // against your own backend if selling directly via M-Pesa outside Play's billing system.
    var isPro by remember { mutableStateOf(false) }

    // Root background: paints the whole screen with the theme's background color underneath
    // every destination, and insets content so the bottom nav row and top content don't draw
    // underneath the status/navigation bars under edge-to-edge.
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val currentTab = when (currentRoute) {
        RadaDestination.Dashboard.route -> RadaTab.Home
        RadaDestination.Drive.route -> RadaTab.Drive
        RadaDestination.Help.route -> RadaTab.Help
        RadaDestination.Settings.route -> RadaTab.Settings
        else -> null // splash/onboarding/login/report/paywall don't show the tab bar
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .systemBarsPadding()
    ) {
        NavHost(
            navController = navController,
            startDestination = RadaDestination.Splash.route,
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            composable(RadaDestination.Splash.route) {
                SplashRouteScreen(
                    onGoToOnboarding = {
                        navController.navigate(RadaDestination.Onboarding.route) {
                            popUpTo(RadaDestination.Splash.route) { inclusive = true }
                        }
                    },
                    onGoToLogin = {
                        navController.navigate(RadaDestination.Login.route) {
                            popUpTo(RadaDestination.Splash.route) { inclusive = true }
                        }
                    },
                    onGoToDashboard = {
                        navController.navigate(RadaDestination.Dashboard.route) {
                            popUpTo(RadaDestination.Splash.route) { inclusive = true }
                        }
                    },
                    onGoToCompleteProfile = {
                        navController.navigate(RadaDestination.CompleteProfile.route) {
                            popUpTo(RadaDestination.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(RadaDestination.Onboarding.route) {
                OnboardingRouteScreen(onDone = {
                    navController.navigate(RadaDestination.Login.route) {
                        popUpTo(RadaDestination.Onboarding.route) { inclusive = true }
                    }
                })
            }

            composable(RadaDestination.Login.route) {
                LoginRouteScreen(
                    onNavigateToOtp = { verificationId, e164Phone ->
                        navController.navigate(
                            RadaDestination.Otp.createRoute(verificationId = verificationId, phoneDisplay = e164Phone)
                        )
                    },
                    onSignedInDirectly = {
                        navController.navigate(RadaDestination.Dashboard.route) {
                            popUpTo(RadaDestination.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToCompleteProfile = {
                        navController.navigate(RadaDestination.CompleteProfile.route) {
                            popUpTo(RadaDestination.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(
                RadaDestination.Otp.route,
                arguments = listOf(
                    navArgument("verificationId") { type = NavType.StringType },
                    navArgument("phoneDisplay") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val verificationId = backStackEntry.arguments?.getString("verificationId").orEmpty()
                val phoneDisplay = backStackEntry.arguments?.getString("phoneDisplay").orEmpty()
                OtpRouteScreen(
                    verificationId = verificationId,
                    phoneDisplay = phoneDisplay,
                    onVerified = {
                        navController.navigate(RadaDestination.Dashboard.route) {
                            popUpTo(RadaDestination.Login.route) { inclusive = true }
                        }
                    },
                    onNavigateToCompleteProfile = {
                        navController.navigate(RadaDestination.CompleteProfile.route) {
                            popUpTo(RadaDestination.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(RadaDestination.CompleteProfile.route) {
                CompleteProfileRouteScreen(onDone = {
                    navController.navigate(RadaDestination.Dashboard.route) {
                        popUpTo(RadaDestination.CompleteProfile.route) { inclusive = true }
                    }
                })
            }

            composable(RadaDestination.Dashboard.route) {
                DashboardRouteScreen(
                    isPro = isPro,
                    onDrive = { navController.navigateToTab(RadaDestination.Drive.route) },
                    onReport = { navController.navigateToTab(RadaDestination.Report.route) },
                    onHelp = { navController.navigateToTab(RadaDestination.Help.route) },
                    onUpgrade = { navController.navigate(RadaDestination.Paywall.route) },
                    onSettings = { navController.navigateToTab(RadaDestination.Settings.route) }
                )
            }

            composable(RadaDestination.Drive.route) {
                // Location permission is requested here — the moment the user actually taps into
                // the feature that needs it — rather than during onboarding, before they have any
                // context for why the app wants it. Foreground-only for now (ACCESS_FINE_LOCATION);
                // the ACCESS_BACKGROUND_LOCATION upgrade Android 11+ requires as a separate prompt
                // is deferred to whenever background-driving-mode alerts actually get built, since
                // asking for it before that exists would be the same "premature ask" problem again.
                val context = LocalContext.current
                var hasLocationPermission by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED
                    )
                }
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { granted -> hasLocationPermission = granted }

                LaunchedEffect(Unit) {
                    if (!hasLocationPermission) {
                        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }

                DriveRouteScreen(
                    isPro = isPro,
                    hasLocationPermission = hasLocationPermission,
                    onRequestPermission = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                    onHome = { navController.navigateToTab(RadaDestination.Dashboard.route) },
                    onReport = { navController.navigateToTab(RadaDestination.Report.route) },
                    onHelp = { navController.navigateToTab(RadaDestination.Help.route) },
                    onUnlockTapped = { navController.navigate(RadaDestination.Paywall.route) },
                    onSettings = { navController.navigateToTab(RadaDestination.Settings.route) }
                )
            }

            composable(RadaDestination.Report.route) {
                // Checks current permission status only — doesn't eagerly re-prompt on entry,
                // since Report is only reachable from Drive's nav bar, which already asked when
                // the user first tapped into Drive. ReportViewModel asks lazily (on first tap of
                // the Report button) if it turns out permission still isn't granted.
                val context = LocalContext.current
                var hasLocationPermission by remember {
                    mutableStateOf(
                        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                                == PackageManager.PERMISSION_GRANTED
                    )
                }
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { granted -> hasLocationPermission = granted }

                ReportRouteScreen(
                    hasLocationPermission = hasLocationPermission,
                    onRequestPermission = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                    onDone = { navController.navigateToTab(RadaDestination.Drive.route) }
                )
            }

            composable(RadaDestination.Help.route) {
                HelpRouteScreen(
                    onHome = { navController.navigateToTab(RadaDestination.Dashboard.route) },
                    onDrive = { navController.navigateToTab(RadaDestination.Drive.route) },
                    onReport = { navController.navigateToTab(RadaDestination.Report.route) },
                    onSettings = { navController.navigateToTab(RadaDestination.Settings.route) }
                )
            }

            composable(RadaDestination.Settings.route) {
                SettingsRouteScreen(
                    onHome = { navController.navigateToTab(RadaDestination.Dashboard.route) },
                    onDrive = { navController.navigateToTab(RadaDestination.Drive.route) },
                    onReport = { navController.navigateToTab(RadaDestination.Report.route) },
                    onHelp = { navController.navigateToTab(RadaDestination.Help.route) },
                    onSignedOut = {
                        // Full reset, not a lateral tab switch — clears the entire tab back stack
                        // (Dashboard is the anchor every navigateToTab call pops up to, so popping
                        // it inclusively clears everything under it too) and lands on a fresh Login.
                        navController.navigate(RadaDestination.Login.route) {
                            popUpTo(RadaDestination.Dashboard.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(RadaDestination.Paywall.route) {
                PaywallRouteScreen(onTrialStarted = {
                    isPro = true
                    navController.navigate(RadaDestination.Dashboard.route) {
                        popUpTo(RadaDestination.Dashboard.route) { inclusive = true }
                    }
                })
            }
        }

        if (currentTab != null) {
            RadaBottomBar(
                selected = currentTab,
                onSelect = { tab ->
                    navController.navigateToTab(
                        when (tab) {
                            RadaTab.Home -> RadaDestination.Dashboard.route
                            RadaTab.Drive -> RadaDestination.Drive.route
                            RadaTab.Report -> RadaDestination.Report.route
                            RadaTab.Help -> RadaDestination.Help.route
                            RadaTab.Settings -> RadaDestination.Settings.route
                        }
                    )
                }
            )
        }
    }
}
