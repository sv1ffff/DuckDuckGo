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

package com.duckduckgo.common.utils.edgetoedge

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import javax.inject.Inject

/**
 * Standardised window-insets handling for edge-to-edge screens.
 *
 * Each method installs an [androidx.core.view.OnApplyWindowInsetsListener] that adds the relevant
 * system-bar inset on top of the view's *original* padding (captured once), so repeated inset
 * dispatches never accumulate padding. Insets are requested as soon as the view is attached.
 *
 * Callers should only invoke these when edge-to-edge is enabled for the screen's bucket
 * (see [EdgeToEdgeProvider]); when disabled the window reserves the bars itself and no padding
 * should be added.
 */
class EdgeToEdgeHandler @Inject constructor() {

    fun applyStatusBarInsets(view: View) {
        val initialTop = view.paddingTop
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val top = insets.getInsets(
                WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.displayCutout(),
            ).top
            v.updatePadding(top = initialTop + top)
            insets
        }
        ViewCompat.requestApplyInsets(view)
    }

    /**
     * Applies bottom padding for the navigation bar.
     *
     * When [drawBehindGestureNav] is true, the padding uses the *tappable* navigation inset, which is
     * 0 in gesture navigation (so content draws edge-to-edge behind the transparent gesture handle) and
     * the button-bar height in 2/3-button navigation (so content sits above the buttons). When false it
     * uses the full navigation-bar inset, keeping content clear of the bar in every navigation mode.
     */
    fun applyNavigationBarInsets(
        view: View,
        drawBehindGestureNav: Boolean = false,
    ) {
        val initialBottom = view.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val bottomType = if (drawBehindGestureNav) {
                WindowInsetsCompat.Type.tappableElement()
            } else {
                WindowInsetsCompat.Type.navigationBars()
            }
            val bottom = insets.getInsets(
                bottomType or WindowInsetsCompat.Type.displayCutout(),
            ).bottom

            v.updatePadding(bottom = initialBottom + bottom)
            insets
        }
        ViewCompat.requestApplyInsets(view)
    }

    /**
     * Horizontal inset: navigation bar + display cutout on the left and right edges. In landscape the
     * 3-button nav bar and the camera cutout move to a side edge and run the full height, so applying
     * this to a screen's root insets the whole column (toolbar, content, bottom bar) at once. No-op in
     * portrait. The listener is non-consuming, so child views still receive insets for their own
     * top/bottom handling.
     */
    fun applyHorizontalSystemBarInsets(view: View) {
        val initialLeft = view.paddingLeft
        val initialRight = view.paddingRight
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, insets ->
            val barsAndCutout = insets.getInsets(
                WindowInsetsCompat.Type.navigationBars() or WindowInsetsCompat.Type.displayCutout(),
            )
            v.updatePadding(
                left = initialLeft + barsAndCutout.left,
                right = initialRight + barsAndCutout.right,
            )
            insets
        }
        ViewCompat.requestApplyInsets(view)
    }
}
