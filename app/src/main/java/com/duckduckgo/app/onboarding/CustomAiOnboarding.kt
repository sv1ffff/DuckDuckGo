/*
 * Copyright (c) 2026 DuckDuckGo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duckduckgo.app.onboarding

import androidx.core.content.edit
import com.duckduckgo.data.store.api.SharedPreferencesProvider
import com.duckduckgo.di.scopes.AppScope
import com.duckduckgo.referral.api.AppInstallationReferrerStateListener
import com.duckduckgo.referral.api.AppInstallationReferrerStateListener.Companion.MAX_REFERRER_WAIT_TIME_MS
import com.duckduckgo.referral.api.ReferrerParserPlugin
import com.squareup.anvil.annotations.ContributesBinding
import com.squareup.anvil.annotations.ContributesMultibinding
import dagger.Lazy
import dagger.SingleInstanceIn
import kotlinx.coroutines.withTimeoutOrNull
import logcat.LogPriority.INFO
import logcat.LogPriority.WARN
import logcat.logcat
import javax.inject.Inject

interface CustomAiOnboarding {
    /**
     * Awaits Play Install Referrer resolution (bounded by a timeout) and returns whether the app
     * was installed via the custom AI onboarding referral link (`onboarding=ai`).
     * Timeout / non-Play / referrer failure -> false (standard onboarding).
     */
    suspend fun isActive(): Boolean
}

@ContributesMultibinding(scope = AppScope::class, boundType = ReferrerParserPlugin::class)
@ContributesBinding(scope = AppScope::class, boundType = CustomAiOnboarding::class)
@SingleInstanceIn(AppScope::class)
class RealCustomAiOnboarding @Inject constructor(
    // Lazy breaks a Dagger dependency cycle: this class is contributed into the
    // ReferrerParserPlugin multibinding -> QueryParamReferrerParser holds that plugin set ->
    // the AppInstallationReferrerStateListener impl depends on the parser. Resolving the
    // listener lazily (only inside isActive()) defers it past construction and breaks the loop.
    // Do NOT replace with a direct injection.
    private val referrerStateListener: Lazy<AppInstallationReferrerStateListener>,
    private val sharedPreferencesProvider: SharedPreferencesProvider,
) : ReferrerParserPlugin, CustomAiOnboarding {

    // Single cached instance on purpose: the writer (process) and the reader (isActive) MUST share
    // the same SharedPreferences instance, so keep this `by lazy` (resolved once) rather than
    // fetching per call.
    private val preferences by lazy { sharedPreferencesProvider.getSharedPreferences(FILENAME) }

    override fun process(referrerParts: List<String>) {
        runCatching {
            val value = referrerParts
                .firstOrNull { it.startsWith("$ONBOARDING_KEY=") }
                ?.removePrefix("$ONBOARDING_KEY=")
            if (value == AI_VALUE) {
                logcat(INFO) { "Custom AI onboarding referral detected" }
                preferences.edit { putBoolean(KEY_CUSTOM_AI_ONBOARDING_FLOW, true) }
            }
        }.onFailure { logcat(WARN) { "Failed to persist custom AI onboarding flag: ${it.message}" } }
    }

    override suspend fun isActive(): Boolean {
        // Ensure the install referrer (and therefore process()) has resolved before reading.
        withTimeoutOrNull(MAX_REFERRER_WAIT_TIME_MS) { referrerStateListener.get().waitForReferrerCode() }
        return preferences.getBoolean(KEY_CUSTOM_AI_ONBOARDING_FLOW, false)
    }

    companion object {
        const val ONBOARDING_KEY = "onboarding"

        // exact lowercase value as sent in the campaign referrer link (e.g. ...&onboarding=ai)
        private const val AI_VALUE = "ai"
        private const val FILENAME = "com.duckduckgo.app.onboarding.customai"
        private const val KEY_CUSTOM_AI_ONBOARDING_FLOW = "customAiOnboardingFlow"
    }
}
