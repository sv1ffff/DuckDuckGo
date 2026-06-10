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

import com.duckduckgo.app.onboarding.store.OnboardingPath
import com.duckduckgo.app.onboarding.store.ReferrerOnboardingDataStore
import org.junit.Assert.assertEquals
import org.junit.Test

class ReferrerOnboardingParserPluginTest {

    private val fakeStore = object : ReferrerOnboardingDataStore {
        override var onboardingPath: OnboardingPath = OnboardingPath.NONE
    }
    private val plugin = ReferrerOnboardingParserPlugin(fakeStore)

    @Test
    fun whenOnboardingAiThenPathIsAi() {
        plugin.process(listOf("origin=funnel_playstore", "onboarding=ai"))
        assertEquals(OnboardingPath.AI, fakeStore.onboardingPath)
    }

    @Test
    fun whenOnboardingMissingThenPathIsNone() {
        plugin.process(listOf("origin=funnel_playstore"))
        assertEquals(OnboardingPath.NONE, fakeStore.onboardingPath)
    }

    @Test
    fun whenOnboardingEmptyThenPathIsNone() {
        plugin.process(listOf("onboarding="))
        assertEquals(OnboardingPath.NONE, fakeStore.onboardingPath)
    }

    @Test
    fun whenOnboardingUnrecognisedThenPathIsNone() {
        plugin.process(listOf("onboarding=somethingelse"))
        assertEquals(OnboardingPath.NONE, fakeStore.onboardingPath)
    }
}
