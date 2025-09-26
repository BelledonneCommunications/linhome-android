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

package org.linhome.ui.assistant.shared

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import org.linhome.LinhomeApplication.Companion.coreContext
import org.linhome.LinhomeApplication.Companion.corePreferences
import org.linphone.core.Account
import org.linphone.core.AccountCreator
import org.linphone.core.AccountCreatorListenerStub
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.ProxyConfig
import org.linphone.core.RegistrationState
import org.linphone.core.TransportType
import org.linphone.core.tools.Log
import java.util.Locale

abstract class CreatorAssistantViewModel(defaultValuePath: String) : ViewModel() {

    var accountCreator: AccountCreator = coreContext.core.createAccountCreator(null)
    lateinit var creatorListener : AccountCreatorListenerStub

    private var waitingForPushToken = false
    private var waitForPushJob: Job? = null

    private var sipRegistrationCoreListener : CoreListenerStub? = null

    private val coreListener = object : CoreListenerStub() {
        override fun onPushNotificationReceived(core: Core, payload: String?) {
            Log.i("[Assistant] Push received: [$payload]")

            val data = payload.orEmpty()
            if (data.isNotEmpty()) {
                try {
                    // This is because JSONObject.toString() done by the SDK will result in payload looking like {"custom-payload":"{\"token\":\"value\"}"}
                    val cleanPayload = data.replace("\\\"", "\"").replace("\"{", "{").replace(
                        "}\"",
                        "}"
                    )
                    Log.i("[Assistant] Cleaned payload is: [$cleanPayload]")
                    val json = JSONObject(cleanPayload)
                    val customPayload = json.getJSONObject("custom-payload")
                    if (customPayload.has("token")) {
                        waitForPushJob?.cancel()
                        waitingForPushToken = false

                        val token = customPayload.getString("token")
                        if (token.isNotEmpty()) {
                            Log.i("[Assistant] Extracted token [$token] from push payload")
                            accountCreator.token = token
                            corePreferences.flexiApiToken = token
                            onFlexiApiTokenReceived()
                        } else {
                            Log.e("[Assistant] Push payload JSON object has an empty 'token'!")
                            onFlexiApiTokenRequestError()
                        }
                    } else {
                        Log.e("[Assistant] Push payload JSON object has no 'token' key!")
                        onFlexiApiTokenRequestError()
                    }
                } catch (e: JSONException) {
                    Log.e("[Assistant] Exception trying to parse push payload as JSON: [$e]")
                    onFlexiApiTokenRequestError()
                }
            } else {
                Log.e("[Assistant] Push payload is null or empty, can't extract auth token!")
                onFlexiApiTokenRequestError()
            }
        }
    }

    init {
        coreContext.core.loadConfigFromXml(defaultValuePath)
        accountCreator.language = Locale.getDefault().language
        accountCreator.domain = corePreferences.loginDomain
        accountCreator.algorithm = corePreferences.passwordAlgo
        coreContext.core.addListener(coreListener)
    }

    override fun onCleared() {
        coreContext.core.removeListener(coreListener)
        waitForPushJob?.cancel()
    }

    fun setUsername(field: Pair<MutableLiveData<String>, MutableLiveData<Boolean>>): AccountCreator.UsernameStatus? {
        if (TextUtils.isEmpty(field.first.value))
            return null
        val result = accountCreator.setUsername(field.first.value)
        field.second.value = result == AccountCreator.UsernameStatus.Ok
        return result
    }

    fun setPassword(field: Pair<MutableLiveData<String>, MutableLiveData<Boolean>>): AccountCreator.PasswordStatus? {
        if (TextUtils.isEmpty(field.first.value))
            return null
        val result = accountCreator.setPassword(field.first.value)
        field.second.value = result == AccountCreator.PasswordStatus.Ok
        return result
    }

    fun setEmail(field: Pair<MutableLiveData<String>, MutableLiveData<Boolean>>): AccountCreator.EmailStatus? {
        if (TextUtils.isEmpty(field.first.value))
            return null
        val result = accountCreator.setEmail(field.first.value)
        field.second.value = result == AccountCreator.EmailStatus.Ok
        return result
    }

    fun setDomain(field: Pair<MutableLiveData<String>, MutableLiveData<Boolean>>): AccountCreator.DomainStatus? {
        if (TextUtils.isEmpty(field.first.value))
            return null
        val result = accountCreator.setDomain(field.first.value)
        field.second.value = result == AccountCreator.DomainStatus.Ok
        return result
    }

    fun setTransport(transport: TransportType) {
        accountCreator.transport = transport
    }

    // Local proxy config creation

    fun linhomeAccountCreateProxyConfig(accountCreator: AccountCreator, checkRegistration:Boolean = false, registrationOk:MutableLiveData<Boolean>? = null) {
        val account = accountCreator.createAccountInCore()
        account?.findAuthInfo()?.also { authInfo ->
            authInfo.clone().also { clonedAuthInfo ->
                coreContext.core.removeAuthInfo(authInfo)
                clonedAuthInfo.algorithm = corePreferences.passwordAlgo
                coreContext.core.addAuthInfo(clonedAuthInfo)
            }
        }
        account?.params?.clone()?.also { params ->
            params.pushNotificationAllowed = true
            account.params = params
        }
        if (checkRegistration) {
            sipRegistrationCoreListener = object : CoreListenerStub() {
                override fun onAccountRegistrationStateChanged(
                    core: Core,
                    account: Account,
                    state: RegistrationState?,
                    message: String
                ) {
                    if (state == RegistrationState.Ok) {
                        core.removeListener(sipRegistrationCoreListener)
                        registrationOk?.value = true
                    }
                    if (state == RegistrationState.Failed) {
                        core.removeListener(sipRegistrationCoreListener)
                        registrationOk?.value = false
                    }
                }
            }
            coreContext.core.addListener(sipRegistrationCoreListener)
            account?.refreshRegister()
        }
    }

    // FlexiApi Account Token Request

    abstract fun onFlexiApiTokenReceived()
    abstract fun onFlexiApiTokenRequestError()

    protected fun requestFlexiApiToken() {
        if (!coreContext.core.isPushNotificationAvailable) {
            Log.e(
                "[Assistant] Core says push notification aren't available, can't request a token from FlexiAPI"
            )
            onFlexiApiTokenRequestError()
            return
        }

        val pushConfig = coreContext.core.pushNotificationConfig
        if (pushConfig != null) {
            Log.i(
                "[Assistant] Found push notification info: provider [${pushConfig.provider}], param [${pushConfig.param}] and prid [${pushConfig.prid}]"
            )
            accountCreator.pnProvider = pushConfig.provider
            accountCreator.pnParam = pushConfig.param
            accountCreator.pnPrid = pushConfig.prid

            // Request an auth token, will be sent by push
            val result = accountCreator.requestAuthToken()
            if (result == AccountCreator.Status.RequestOk) {
                val waitFor = corePreferences.flexiApiTimeOutSeconds * 1000
                waitingForPushToken = true
                waitForPushJob?.cancel()
                Log.i("[Assistant] Waiting push with auth token for $waitFor ms")
                waitForPushJob = viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        delay(waitFor.toLong())
                    }
                    withContext(Dispatchers.Main) {
                        if (waitingForPushToken) {
                            waitingForPushToken = false
                            Log.e("[Assistant] Auth token wasn't received by push in $waitFor ms")
                            onFlexiApiTokenRequestError()
                        }
                    }
                }
            } else {
                Log.e("[Assistant] Failed to require a push with an auth token: [$result]")
                onFlexiApiTokenRequestError()
            }
        } else {
            Log.e("[Assistant] No push configuration object in Core, shouldn't happen!")
            onFlexiApiTokenRequestError()
        }
    }
}