package com.rada.android.car

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session

class RadaCarSession : Session() {
    override fun onCreateScreen(intent: Intent): Screen = RadaCarHomeScreen(carContext)
}
