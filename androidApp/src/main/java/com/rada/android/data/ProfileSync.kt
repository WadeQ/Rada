package com.rada.android.data

import kotlinx.coroutines.flow.first

suspend fun syncProfileName(auth: AuthRepository, prefs: UserProfilePreferences) {
    val local = prefs.userName.first()?.trim().orEmpty()
    val remote = auth.currentUserDisplayName()?.trim().orEmpty()
    when {
        remote.isNotBlank() && remote != local -> prefs.setUserName(remote)
        remote.isBlank() && local.isNotBlank() -> auth.updateDisplayName(local)
    }
}
