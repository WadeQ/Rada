package com.rada.android.car

import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

/**
 * Entry point Android Auto binds to. Declared under the IoT category in the manifest — the
 * pragmatic fit among Google's fixed Car App Library categories (Navigation, POI, IoT, Weather,
 * Media, Messaging, Calling) for a read-only driving-status screen, given that Rada isn't a
 * turn-by-turn navigation app. See the README for the caveat this creates for a future Play
 * listing.
 */
class RadaCarAppService : CarAppService() {

    override fun createHostValidator(): HostValidator =
        HostValidator.Builder(applicationContext).build()

    override fun onCreateSession(): Session = RadaCarSession()
}
