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

package org.linhome.ui.call

import android.os.Bundle
import android.view.View
import org.linhome.GenericActivity
import org.linhome.LinhomeApplication.Companion.coreContext
import org.linphone.core.Call
import org.linphone.core.tools.Log


abstract class CallGenericActivity : GenericActivity() {

    var call: Call? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val decorView: View = window.decorView
        val uiOptions: Int =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.systemUiVisibility = uiOptions

        call = intent.getStringExtra("callId")?.let { callId ->
            if (intent.getBooleanExtra("closeAppUponCallFinish", false)) {
                coreContext.closeAppUponCallFinish = true
            }
            coreContext.core.calls.filter { it.callLog.callId.equals(callId) }.firstOrNull()
        }



    }


}