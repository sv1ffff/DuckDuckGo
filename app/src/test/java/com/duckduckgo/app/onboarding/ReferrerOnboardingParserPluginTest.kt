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

import com.duckduckgo.app.onboarding.store.OnboardingStore
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify

class ReferrerOnboardingParserPluginTest {

    private val onboardingStore: OnboardingStore = mock()
    private val plugin = ReferrerOnboardingParserPlugin(onboardingStore)

    @Test
    fun whenOnboardingAiThenCustomAiOnboardingFlowSet() {
        plugin.process(listOf("origin=funnel_playstore", "onboarding=ai"))
        verify(onboardingStore).setCustomAiOnboardingFlow()
    }

    @Test
    fun whenOnboardingMissingThenCustomAiOnboardingFlowNotSet() {
        plugin.process(listOf("origin=funnel_playstore"))
        verify(onboardingStore, never()).setCustomAiOnboardingFlow()
    }

    @Test
    fun whenOnboardingEmptyThenCustomAiOnboardingFlowNotSet() {
        plugin.process(listOf("onboarding="))
        verify(onboardingStore, never()).setCustomAiOnboardingFlow()
    }

    @Test
    fun whenOnboardingUnrecognisedThenCustomAiOnboardingFlowNotSet() {
        plugin.process(listOf("onboarding=somethingelse"))
        verify(onboardingStore, never()).setCustomAiOnboardingFlow()
    }
}
