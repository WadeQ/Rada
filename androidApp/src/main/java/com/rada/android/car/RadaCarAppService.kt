package com.rada.android.car

import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator
import com.rada.android.BuildConfig

/**
 * Entry point Android Auto binds to. Declared under the IoT category in the manifest — the
 * pragmatic fit among Google's fixed Car App Library categories (Navigation, POI, IoT, Weather,
 * Media, Messaging, Calling) for a read-only driving-status screen, given that Rada isn't a
 * turn-by-turn navigation app. See the README for the caveat this creates for a future Play
 * listing.
 *
 * Host validation: debug builds trust any host (needed to test a sideloaded app at all — an
 * empty HostValidator trusts *nothing*, not even Android Auto's own official host, and that's
 * the difference between "the app doesn't show up" and "it shows up"). This must never ship in
 * a release build, which is why it's gated on BuildConfig.DEBUG; a real release needs Google's
 * published production host allowlist here instead.
 */
class RadaCarAppService : CarAppService() {

    override fun createHostValidator(): HostValidator =
        if (BuildConfig.DEBUG) {
            HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
        } else {
            // Deny everything until this is replaced with Google's real production host
            // allowlist (package name + signing certificate hash) before any public release.
            HostValidator.Builder(applicationContext).build()
        }

    override fun onCreateSession(): Session = RadaCarSession()
}
