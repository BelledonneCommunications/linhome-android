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

package org.linhome.ui.tabbar

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.linhome.LinhomeApplication
import org.linhome.linphonecore.extensions.missedCount
import org.linphone.core.Call
import org.linphone.core.CallLog
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub

class TabbarViewModel : ViewModel() {
    var unreadCount = MutableLiveData(0)

    private val coreListener = object : CoreListenerStub() {
        override fun onCallStateChanged(
            core: Core,
            call: Call,
            state: Call.State?,
            message: String
        ) {
            updateUnreadCount()
        }
        override fun onCallLogUpdated(lc: Core, newcl: CallLog) {
            updateUnreadCount()
        }
    }

    fun updateUnreadCount() {
        unreadCount.value =  LinhomeApplication.coreContext.core.missedCount()
    }

    init {
        LinhomeApplication.coreContext.core.addListener(coreListener)
        updateUnreadCount()
    }

    override fun onCleared() {
        LinhomeApplication.coreContext.core.removeListener(coreListener)
        super.onCleared()
    }

}