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

import android.content.Context
import androidx.core.content.edit
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.duckduckgo.app.onboarding.store.ReferrerOnboardingDataStoreImpl.Companion.FILENAME
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReferrerOnboardingDataStoreTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private lateinit var store: ReferrerOnboardingDataStoreImpl

    @Before
    fun setup() {
        context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE).edit { clear() }
        store = ReferrerOnboardingDataStoreImpl(context)
    }

    @Test
    fun whenNothingStoredThenDefaultIsNone() {
        assertEquals(OnboardingPath.NONE, store.onboardingPath)
    }

    @Test
    fun whenAiStoredThenAiReturned() {
        store.onboardingPath = OnboardingPath.AI
        assertEquals(OnboardingPath.AI, store.onboardingPath)
    }
}
