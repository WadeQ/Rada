package com.rada.android

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.rada.android.data.AndroidPlaceNameResolver
import com.rada.android.data.FirestoreReportRepository
import com.rada.android.data.ReportHistoryPreferences
import com.rada.shared.domain.ReportType
import com.rada.shared.location.AndroidLocationProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale

class QuickReportActivity : ComponentActivity() {

    private val activity: ComponentActivity get() = this
    private var tts: TextToSpeech? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val type = intent.getStringExtra(EXTRA_REPORT_TYPE)?.let {
            runCatching { ReportType.valueOf(it) }.getOrNull()
        }

        if (type == null || FirebaseAuth.getInstance().currentUser == null || !hasLocationPermission()) {
            openApp()
            finish()
            return
        }

        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) tts?.language = Locale.UK
        }

        CoroutineScope(Dispatchers.Main).launch {
            submit(type)
            finish()
        }
    }

    private suspend fun submit(type: ReportType) {
        val locationProvider = AndroidLocationProvider(LocationServices.getFusedLocationProviderClient(this))
        val location = withTimeoutOrNull(LOCATION_TIMEOUT_MS) {
            locationProvider.getLastKnownLocation()
        }
        if (location == null) {
            speakAndToast("Couldn't get your location for that report.")
            return
        }

        val placeName = withTimeoutOrNull(GEOCODE_TIMEOUT_MS) {
            AndroidPlaceNameResolver(activity).resolve(location.latitude, location.longitude)
        }
        val result = FirestoreReportRepository().submitReport(
            type = type,
            latitude = location.latitude,
            longitude = location.longitude,
            bearing = location.bearingCurrent,
            placeName = placeName
        )

        if (result.isSuccess) {
            ReportHistoryPreferences(this).record(System.currentTimeMillis())
            speakAndToast("${type.spokenLabel()} reported${placeName?.let { " near $it" } ?: ""}. Thanks for the heads-up.")
        } else {
            speakAndToast("That report didn't go through. Try again from the app.")
        }
    }

    private fun speakAndToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, "rada-quick-report")
    }

    private fun openApp() {
        startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun hasLocationPermission() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    override fun onDestroy() {

        tts?.let { engine -> android.os.Handler(mainLooper).postDelayed({ engine.shutdown() }, TTS_SHUTDOWN_DELAY_MS) }
        super.onDestroy()
    }

    private companion object {
        const val EXTRA_REPORT_TYPE = "report_type"
        const val LOCATION_TIMEOUT_MS = 4_000L
        const val GEOCODE_TIMEOUT_MS = 4_500L
        const val TTS_SHUTDOWN_DELAY_MS = 4_000L

        fun ReportType.spokenLabel() = when (this) {
            ReportType.CAMERA -> "Speed camera"
            ReportType.CHECKPOINT -> "Police checkpoint"
            ReportType.ALCOBLOW -> "Alcohol check"
        }
    }
}
