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

package com.duckduckgo.app.onboarding.store

import android.content.SharedPreferences
import androidx.core.content.edit
import com.duckduckgo.data.store.api.SharedPreferencesProvider
import com.duckduckgo.di.scopes.AppScope
import com.squareup.anvil.annotations.ContributesBinding
import dagger.SingleInstanceIn
import javax.inject.Inject

interface ReferrerOnboardingDataStore {
    var onboardingPath: OnboardingPath
}

@ContributesBinding(AppScope::class)
@SingleInstanceIn(AppScope::class)
class ReferrerOnboardingDataStoreImpl @Inject constructor(
    private val sharedPreferencesProvider: SharedPreferencesProvider,
) : ReferrerOnboardingDataStore {

    override var onboardingPath: OnboardingPath
        get() {
            val stored = preferences.getString(KEY_ONBOARDING_PATH, null) ?: return OnboardingPath.NONE
            return runCatching { OnboardingPath.valueOf(stored) }.getOrDefault(OnboardingPath.NONE)
        }
        set(value) = preferences.edit(true) { putString(KEY_ONBOARDING_PATH, value.name) }

    private val preferences: SharedPreferences by lazy { sharedPreferencesProvider.getSharedPreferences(FILENAME) }

    companion object {
        const val FILENAME = "com.duckduckgo.app.onboarding.referral"
        private const val KEY_ONBOARDING_PATH = "KEY_ONBOARDING_PATH"
    }
}
