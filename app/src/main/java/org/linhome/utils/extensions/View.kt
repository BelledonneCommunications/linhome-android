/*
 * Copyright (c) 2010-2020 Belledonne Communications SARL.
 *
 * This file is part of linhome-android
 * (see https://www.linhome.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.linhome.utils.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View


fun View.toogleVisible() {
    animate()
        .alpha(if (visibility == View.VISIBLE) 0f else 1f)
        .setDuration(250)
        .setListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                if (visibility == View.VISIBLE)
                    visibility = View.INVISIBLE
                else
                    visibility = View.VISIBLE
            }
        })
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.toogleGone() {
    if (visibility == View.VISIBLE)
        visibility = View.GONE
    else
        visibility = View.VISIBLE
}

fun View.forceVisible () {
    visibility = View.VISIBLE
    alpha = 1f
}