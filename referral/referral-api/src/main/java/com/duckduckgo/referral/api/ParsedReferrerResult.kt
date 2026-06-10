/*
 * Copyright (c) 2019 DuckDuckGo
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

package com.duckduckgo.referral.api

sealed class ParsedReferrerResult(open val fromCache: Boolean = false) {
    data class EuAuctionSearchChoiceReferrerFound(override val fromCache: Boolean = false) : ParsedReferrerResult(fromCache)
    data class EuAuctionBrowserChoiceReferrerFound(override val fromCache: Boolean = false) : ParsedReferrerResult(fromCache)
    data class CampaignReferrerFound(
        val campaignSuffix: String,
        override val fromCache: Boolean = false,
    ) : ParsedReferrerResult(fromCache)

    data class ReferrerNotFound(override val fromCache: Boolean = false) : ParsedReferrerResult(fromCache)
    data class ParseFailure(val reason: ParseFailureReason) : ParsedReferrerResult()
    data object ReferrerInitialising : ParsedReferrerResult()
}

sealed class ParseFailureReason {
    data object FeatureNotSupported : ParseFailureReason()
    data object ServiceUnavailable : ParseFailureReason()
    data object DeveloperError : ParseFailureReason()
    data object ServiceDisconnected : ParseFailureReason()
    data object UnknownError : ParseFailureReason()
    data object ReferralServiceUnavailable : ParseFailureReason()
}
