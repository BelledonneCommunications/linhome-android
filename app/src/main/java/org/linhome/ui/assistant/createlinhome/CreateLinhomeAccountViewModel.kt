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

package org.linhome.ui.assistant.createlinhome

import androidx.lifecycle.MutableLiveData
import org.linhome.LinhomeApplication.Companion.corePreferences
import org.linhome.entities.Account
import org.linhome.ui.assistant.CreatorAssistantViewModel
import org.linphone.core.AccountCreator
import org.linphone.core.AccountCreatorListenerStub

class CreateLinhomeAccountViewModel :
    CreatorAssistantViewModel(corePreferences.linhomeAccountDefaultValuesPath) {

    var username: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> =
        Pair(MutableLiveData<String>(), MutableLiveData<Boolean>(false))
    var email: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> =
        Pair(MutableLiveData<String>(), MutableLiveData<Boolean>(false))
    var pass1: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> =
        Pair(MutableLiveData<String>(), MutableLiveData<Boolean>(false))
    var pass2: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> =
        Pair(MutableLiveData<String>(), MutableLiveData<Boolean>(false))

    var creationResult = MutableLiveData<AccountCreator.Status>()

    private val creatorListener = object : AccountCreatorListenerStub() {
        override fun onCreateAccount(
            creator: AccountCreator,
            status: AccountCreator.Status?,
            resp: String?
        ) {
            if (status == AccountCreator.Status.AccountCreated)
                creator?.also {
                    Account.linhomeAccountCreateProxyConfig(creator)
                }
            creationResult.postValue(status)
        }
    }

    init {
        accountCreator.addListener(creatorListener)
    }

    fun valid(): Boolean {
        return username.second.value!! && email.second.value!! && pass1.second.value!! && pass2.second.value!!
    }

    override fun onCleared() {
        accountCreator.removeListener(creatorListener)
        super.onCleared()
    }


}
