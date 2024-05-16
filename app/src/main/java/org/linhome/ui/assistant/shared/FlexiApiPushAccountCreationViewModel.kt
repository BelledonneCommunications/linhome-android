/*
 * Copyright (c) 2010-2023 Belledonne Communications SARL.
 *
 * This file is part of linphone-android
 * (see https://www.linphone.org).
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
package org.linhome.ui.assistant.shared

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.linhome.LinhomeApplication
import org.linhome.entities.LinhomeAccount
import org.linphone.core.AccountCreator
import org.linphone.core.AccountCreatorListenerStub
import org.linphone.core.tools.Log

open class  FlexiApiPushAccountCreationViewModel(defaultValuePath: String) : CreatorAssistantViewModel(defaultValuePath) {

    val pushReady = MutableLiveData<Boolean>()

    init {
        creatorListener = object : AccountCreatorListenerStub() {
            override fun onCreateAccount(
                creator: AccountCreator,
                status: AccountCreator.Status?,
                response: String?
            ) {
                Log.i("[Assistant] [Push Account Creation] Account creation response $status")
                if (status == AccountCreator.Status.AccountCreated) {
                    LinhomeApplication.corePreferences.flexiApiToken = null
                    creator.createAccountInCore()?.also { pushAccount ->
                        pushAccount.params.clone().also { clonedParams ->
                            clonedParams.idkey = LinhomeAccount.PUSH_GW_ID_KEY
                            pushAccount.params = clonedParams
                        }
                        LinhomeAccount.linkProxiesWithPushAccount(pushReady)
                    }
                } else if (status == AccountCreator.Status.MissingArguments) {
                    Log.i("[Assistant] [Push Account Creation] Creation request not authorized, requesting a new token.")
                    LinhomeApplication.corePreferences.flexiApiToken = null
                    requestFlexiApiToken()
                } else {
                    pushReady.value = false
                    Log.e("[Assistant] [Push Account Creation] fail creating a push account $status")
                }
            }

            override fun onSendToken(
                creator: AccountCreator,
                status: AccountCreator.Status?,
                response: String?
            ) {
                Log.i("[Assistant] [Push Account Creation] get push token $status $response")
                if (status == AccountCreator.Status.RequestTooManyRequests) {
                    pushReady.value = false
                }
            }
        }
        accountCreator.addListener(creatorListener)
    }

    override fun onCleared() {
        accountCreator.removeListener(creatorListener)
    }

    fun createPushAccount() {
        if (!LinhomeApplication.corePreferences.automaticallyCreatePushGatewayAccount) {
            Log.i("[Assistant] [Push Account Creation] skipping as automaticallyCreatePushGatewayAccount is set to false")
            pushReady.value = true
            return
        }

        val token = LinhomeApplication.corePreferences.flexiApiToken
        if (token != null) {
            accountCreator.token = token
            Log.i("[Assistant] [Push Account Creation] We already have an auth token from FlexiAPI $token, continue")
            onFlexiApiTokenReceived()
        } else {
            Log.i("[Assistant] [Push Account Creation] Requesting an auth token from FlexiAPI")
            requestFlexiApiToken()
        }
    }

    override fun onFlexiApiTokenReceived() {
        Log.i("[Assistant] [Push Account Creation] Using FlexiAPI auth token ${accountCreator.token}]")
        accountCreator.domain = LinhomeApplication.corePreferences.loginDomain
        accountCreator.algorithm = "SHA-256"
        val status = accountCreator.createPushAccount()
        Log.i("[Assistant] [Push Account Creation] create Account returned $status")
        if (status != AccountCreator.Status.RequestOk) {
            pushReady.value = false
        }
    }

    override fun onFlexiApiTokenRequestError() {
        Log.e("[Assistant] [Push Account Creation] Failed to get an auth token from FlexiAPI")
        pushReady.value = false
    }

    fun handlePushAccount() {
        GlobalScope.launch(context = Dispatchers.Main) {
            if (LinhomeAccount.get()?.params?.domain != LinhomeApplication.corePreferences.loginDomain) {
                if (LinhomeAccount.pushGateway() != null) {
                    LinhomeAccount.linkProxiesWithPushAccount(pushReady)
                } else
                    createPushAccount()
            } else {
                pushReady.value = true
                Log.i("[Assistant] - no need to create/link push gateway, as domain ${LinhomeApplication.corePreferences.loginDomain} is managed by flexisip already.")
            }
        }
    }

}
