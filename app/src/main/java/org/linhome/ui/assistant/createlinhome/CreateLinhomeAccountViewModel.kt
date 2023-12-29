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
import org.linhome.LinhomeApplication
import org.linhome.LinhomeApplication.Companion.corePreferences
import org.linhome.entities.LinhomeAccount
import org.linhome.linphonecore.CorePreferences
import org.linhome.ui.assistant.shared.CreatorAssistantViewModel
import org.linphone.core.AccountCreator
import org.linphone.core.AccountCreatorListenerStub
import org.linphone.core.tools.Log

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

    var creationResult = MutableLiveData<AccountCreator.Status?>()

    init {
        creatorListener = object : AccountCreatorListenerStub() {
            override fun onCreateAccount(
                creator: AccountCreator,
                status: AccountCreator.Status?,
                resp: String?
            ) {
                if (status == AccountCreator.Status.AccountCreated) {
                    Log.i("[Assistant] [Account Creation] Account created")
                    corePreferences.flexiApiToken = null
                    linhomeAccountCreateProxyConfig(creator)
                    creationResult.value = status
                } else if (status == AccountCreator.Status.MissingArguments) {
                    Log.i("[Assistant] [Account Creation] Creation request not authorized, requesting a new token.")
                    corePreferences.flexiApiToken = null
                    requestFlexiApiToken()
                } else {
                    creationResult.value = status!!
                    Log.e("[Assistant] [Account Creation] fail creating an account $status")
                }
            }

            override fun onIsAccountExist(
                creator: AccountCreator,
                status: AccountCreator.Status?,
                response: String?
            ) {
                if (status == AccountCreator.Status.AccountExist) {
                    Log.i("[Assistant] [Account Creation] Account exists")
                    creationResult.value = status
                } else if (status == AccountCreator.Status.AccountNotExist) {
                    val status = accountCreator.createAccount()
                    Log.i("[Assistant] [Account Creation] Account create returned $status")
                    if (status != AccountCreator.Status.RequestOk) {
                        creationResult.value = status
                    }
                } else {
                    creationResult.value = status
                    Log.e("[Assistant] [Account Creation] fail verifying if account exists $status")
                }
            }

            override fun onSendToken(
                creator: AccountCreator,
                status: AccountCreator.Status?,
                response: String?
            ) {
                Log.i("[Assistant] [Account Creation] get push token $status $response")
                if (status == AccountCreator.Status.RequestTooManyRequests) {
                    creationResult.value = status
                }
            }
        }
        accountCreator.addListener(creatorListener)
    }

    fun valid(): Boolean {
        return username.second.value!! && email.second.value!! && pass1.second.value!! && pass2.second.value!!
    }

    override fun onCleared() {
        accountCreator.removeListener(creatorListener)
        super.onCleared()
    }


    fun create() {
        val token = corePreferences.flexiApiToken
        if (token != null) {
            accountCreator.token = token
            Log.i("[Assistant] [Account Creation] We already have an auth token from FlexiAPI ${token}, continue")
            onFlexiApiTokenReceived()
        } else {
            Log.i("[Assistant] [Account Creation] Requesting an auth token from FlexiAPI")
            requestFlexiApiToken()
        }
    }
    override fun onFlexiApiTokenReceived() {
        Log.i("[Assistant] [Account Creation] Using FlexiAPI auth token ${accountCreator.token}]")
        val status = accountCreator.isAccountExist()
        Log.i("[Assistant] [Account Creation] Account exists returned ${status})")
        if (status != AccountCreator.Status.RequestOk) {
            creationResult.value = status
        }
    }

    override fun onFlexiApiTokenRequestError() {
        Log.e("[Assistant] [Account Creation] Failed to get an auth token from FlexiAPI")
        creationResult.value = AccountCreator.Status.UnexpectedError
    }


}
