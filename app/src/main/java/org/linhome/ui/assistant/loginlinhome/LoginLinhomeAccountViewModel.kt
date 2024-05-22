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

package org.linhome.ui.assistant.loginlinhome

import androidx.lifecycle.MutableLiveData
import org.linhome.LinhomeApplication.Companion.corePreferences
import org.linhome.ui.assistant.shared.CreatorAssistantViewModel
import org.linphone.core.AccountCreator
import org.linphone.core.AccountCreatorListenerStub

class LoginLinhomeAccountViewModel :
    CreatorAssistantViewModel(corePreferences.linhomeAccountDefaultValuesPath) {

    var username: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> =
        Pair(MutableLiveData<String>(), MutableLiveData<Boolean>(false))
    var pass1: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> =
        Pair(MutableLiveData<String>(), MutableLiveData<Boolean>(false))

    var accountCreatorResult = MutableLiveData<AccountCreator.Status>()
    var sipRegistrationResult = MutableLiveData<Boolean>()


    fun fieldsValid(): Boolean {
        return username.second.value!! && pass1.second.value!!
    }

    init {
        creatorListener = object : AccountCreatorListenerStub() {
            override fun onIsAccountExist(
                creator: AccountCreator,
                status: AccountCreator.Status?,
                response: String?
            ) {
                status?.also {
                    accountCreatorResult.value = it
                }
                accountCreator.removeListener(creatorListener)
            }
        }
    }

    fun fireLogin() {
        if (accountCreator.isAccountExist() == AccountCreator.Status.RequestOk) {
            accountCreator.addListener(creatorListener)
        } else {
            accountCreatorResult.value = AccountCreator.Status.UnexpectedError
        }
    }

    // Login does not require token
    override fun onFlexiApiTokenReceived() {}

    override fun onFlexiApiTokenRequestError() {}

}
