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

import com.duckduckgo.data.store.api.FakeSharedPreferencesProvider
import com.duckduckgo.referral.api.AppInstallationReferrerStateListener
import com.duckduckgo.referral.api.ParsedReferrerResult
import dagger.Lazy
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CustomAiOnboardingTest {

    private fun testee(listener: AppInstallationReferrerStateListener) =
        RealCustomAiOnboarding(
            referrerStateListener = Lazy { listener },
            sharedPreferencesProvider = FakeSharedPreferencesProvider(),
        )

    private val resolvedListener = object : AppInstallationReferrerStateListener {
        override fun initialiseReferralRetrieval() {}
        override suspend fun waitForReferrerCode(): ParsedReferrerResult = ParsedReferrerResult.ReferrerNotFound()
    }

    private val neverResolvingListener = object : AppInstallationReferrerStateListener {
        override fun initialiseReferralRetrieval() {}
        override suspend fun waitForReferrerCode(): ParsedReferrerResult = awaitCancellation()
    }

    @Test
    fun whenReferrerContainsOnboardingAiThenIsActiveTrue() = runTest {
        val customAiOnboarding = testee(resolvedListener)
        customAiOnboarding.process(listOf("origin=funnel_playstore", "onboarding=ai"))
        assertTrue(customAiOnboarding.isActive())
    }

    @Test
    fun whenReferrerHasNoOnboardingParamThenIsActiveFalse() = runTest {
        val customAiOnboarding = testee(resolvedListener)
        customAiOnboarding.process(listOf("origin=funnel_playstore"))
        assertFalse(customAiOnboarding.isActive())
    }

    @Test
    fun whenOnboardingValueEmptyOrUnknownThenIsActiveFalse() = runTest {
        val customAiOnboarding = testee(resolvedListener)
        customAiOnboarding.process(listOf("onboarding="))
        assertFalse(customAiOnboarding.isActive())
        customAiOnboarding.process(listOf("onboarding=somethingelse"))
        assertFalse(customAiOnboarding.isActive())
    }

    @Test
    fun whenOnboardingValueWrongCaseThenIsActiveFalse() = runTest {
        // the match is exact lowercase "ai"; uppercase/mixed-case must not activate
        val customAiOnboarding = testee(resolvedListener)
        customAiOnboarding.process(listOf("onboarding=AI"))
        assertFalse(customAiOnboarding.isActive())
    }

    @Test
    fun whenReferrerNeverResolvesThenIsActiveFalseAfterTimeout() = runTest {
        val customAiOnboarding = testee(neverResolvingListener)
        assertFalse(customAiOnboarding.isActive())
    }
}
