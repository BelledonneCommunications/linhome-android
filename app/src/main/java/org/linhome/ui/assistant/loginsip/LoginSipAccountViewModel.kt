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

package org.linhome.ui.assistant.loginsip

import androidx.lifecycle.MutableLiveData
import org.linhome.LinhomeApplication.Companion.corePreferences
import org.linhome.ui.assistant.CreatorAssistantViewModel

class LoginSipAccountViewModel :
    CreatorAssistantViewModel(corePreferences.sipAccountDefaultValuesPath) {

    var username: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> =
        Pair(MutableLiveData(), MutableLiveData<Boolean>(false))
    var domain: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> =
        Pair(MutableLiveData(), MutableLiveData<Boolean>(false))
    var pass1: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> =
        Pair(MutableLiveData(), MutableLiveData<Boolean>(false))
    var transport: MutableLiveData<Int> = MutableLiveData<Int>(0)
    var proxy: Pair<MutableLiveData<String?>, MutableLiveData<Boolean>> =
        Pair(MutableLiveData(), MutableLiveData<Boolean>(false))
    var expiration: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> = Pair(
        MutableLiveData<String>(
            corePreferences.config.getString(
                "proxy_default_values",
                "reg_expires",
                "31536000"
            )
        ), MutableLiveData<Boolean>(false)
    )

    val moreOptionsOpened = MutableLiveData(false)
    val pushReady = MutableLiveData<Boolean>()
    val sipRegistered = MutableLiveData<Boolean>()


    fun valid(): Boolean {
        return username.second.value!! && domain.second.value!! && pass1.second.value!! && expiration.second.value!!
    }
}
