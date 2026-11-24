package com.blockme.feature.settings

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blockme.core.data.local.preferences.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

data class InstalledApp(
    val packageName: String,
    val label: String
)

data class SettingsUiState(
    val hapticEnabled: Boolean = false,
    val amoledMode: Boolean = false,
    val unlockChallengeEnabled: Boolean = true,
    val unlockChallengeType: String = "MATH",
    val whitelistedPackages: Set<String> = emptySet(),
    val installedApps: List<InstalledApp> = emptyList()
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val installedApps = MutableStateFlow<List<InstalledApp>>(emptyList())

    private val preferenceState = combine(
        userPreferences.hapticEnabled,
        userPreferences.amoledMode,
        userPreferences.unlockChallengeEnabled,
        userPreferences.unlockChallengeType,
        userPreferences.whitelistedPackages
    ) { haptic, amoled, challenge, challengeType, whitelisted ->
        SettingsUiState(
            hapticEnabled = haptic,
            amoledMode = amoled,
            unlockChallengeEnabled = challenge,
            unlockChallengeType = challengeType,
            whitelistedPackages = whitelisted
        )
    }

    val state: StateFlow<SettingsUiState> = combine(preferenceState, installedApps) { preferences, apps ->
        preferences.copy(installedApps = apps)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    init {
        refreshInstalledApps()
    }

    fun refreshInstalledApps() {
        viewModelScope.launch(Dispatchers.IO) {
            val packageManager = context.packageManager
            val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val apps = packageManager.queryIntentActivities(launcherIntent, 0)
                .mapNotNull { resolveInfo ->
                    val packageName = resolveInfo.activityInfo?.packageName ?: return@mapNotNull null
                    if (packageName == context.packageName) return@mapNotNull null
                    InstalledApp(
                        packageName = packageName,
                        label = resolveInfo.loadLabel(packageManager)?.toString()
                            ?.takeIf { it.isNotBlank() } ?: packageName
                    )
                }
                .distinctBy { it.packageName }
                .sortedBy { it.label.lowercase(Locale.getDefault()) }
            installedApps.value = apps
        }
    }

    fun setAppWhitelisted(packageName: String, allowed: Boolean) {
        viewModelScope.launch {
            userPreferences.setAppWhitelisted(packageName, allowed)
        }
    }

    fun setHapticEnabled(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setHapticEnabled(enabled) }
    }

    fun setAmoledMode(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setAmoledMode(enabled) }
    }

    fun setUnlockChallengeEnabled(enabled: Boolean) {
        viewModelScope.launch { userPreferences.setUnlockChallengeEnabled(enabled) }
    }
}
