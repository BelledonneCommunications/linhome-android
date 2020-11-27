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

package org.linhome

import android.app.Activity
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import org.linhome.ui.toolbar.ToobarButtonClickedListener

abstract class GenericFragment : Fragment(),
    ToobarButtonClickedListener {

    val mainactivity get() = activity as MainActivity

    override fun onResume() {
        super.onResume()
        mainactivity.toobarButtonClickedListener = this
    }

    override fun onPause() {
        hideKeyboard()
        super.onPause()
    }

    override fun onToolbarLeftButtonClicked() {}

    override fun onToolbarRightButtonClicked() {}

    fun hideKeyboard() {
        mainactivity.currentFocus?.also {
            val imm =
                it.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    fun showProgress() {
        mainactivity.toolbarViewModel.activityInprogress.value = true
    }

    fun hideProgress() {
        mainactivity.toolbarViewModel.activityInprogress.value = false
    }


}