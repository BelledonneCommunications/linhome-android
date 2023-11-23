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

package org.linhome.entities

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.linhome.LinhomeApplication
import org.linhome.LinhomeApplication.Companion.coreContext
import org.linhome.LinhomeApplication.Companion.corePreferences
import org.linhome.linphonecore.CorePreferences
import org.linhome.linphonecore.extensions.cleanHistory
import org.linhome.store.DeviceStore
import org.linphone.core.*
import org.linphone.core.Account
import org.linphone.core.tools.Log

object LinhomeAccount {

    const val PUSH_GW_ID_KEY = "linhome_pushgateway"

    private lateinit var coreListener : CoreListenerStub

    fun configured(): Boolean {
        return coreContext.core.accountList.isNotEmpty()
    }

    fun get(): org.linphone.core.Account? {
        coreContext.core.accountList.forEach {
            if (PUSH_GW_ID_KEY != it.params.idkey)
                return it
        }
        return null
    }

    fun linhomeAccountCreateProxyConfig(accountCreator: AccountCreator) {
        accountCreator.createProxyConfig()
        coreContext.core.accountList.first()?.also { account ->
            account.findAuthInfo()?.also { authInfo ->
                authInfo.clone().also { clonedAuthInfo ->
                    coreContext.core.removeAuthInfo(authInfo)
                    clonedAuthInfo.algorithm = corePreferences.passwordAlgo
                    coreContext.core.addAuthInfo(clonedAuthInfo)
                }
            }
            val params = account.params.clone()
            params.pushNotificationAllowed = true
            account.params = params
        }
    }

    fun sipAccountLogin(
        accountCreator: AccountCreator,
        proxy: String?,
        expiration: String,
        pushReady: MutableLiveData<Boolean>,
        sipRegistered: MutableLiveData<Boolean>
    ) {
        val transports = arrayOf("udp","tcp","tls")
        accountCreator.createProxyConfig()
        if (coreContext.core.accountList.isEmpty()) {
            sipRegistered.value = false
            return
        }
        coreContext.core.accountList.first()?.also { account ->
            Log.i("[Account] created proxyConfig with domain ${account.params.domain}")
            account.params.clone().also { newParams ->
                newParams.expires = expiration.toInt()
                if (!TextUtils.isEmpty(proxy)) {
                    Factory.instance().createAddress((if (accountCreator.transport == TransportType.Tls)  "sips:" else "sip:") + proxy!! + ";transport="+transports.get(accountCreator.transport.toInt())).also {
                        newParams.setRoutesAddresses(arrayOf(it))
                    }
                    Log.i("[Account] Set proxyConfig server address to ${newParams.routesAddresses} for proxyConfig with domain $newParams.domain}")
                }
                account.params = newParams
            }

            coreListener = object : CoreListenerStub() {
                override fun onAccountRegistrationStateChanged(
                    core: Core,
                    account: Account,
                    state: RegistrationState?,
                    message: String
                ) {
                    if (state == RegistrationState.Ok) {
                        coreContext.core.removeListener(coreListener)
                        sipRegistered.value = true
                        if (pushGateway() != null)
                            linkProxiesWithPushGateway(pushReady)
                        else
                            createPushGateway(pushReady)
                        GlobalScope.launch(context = Dispatchers.Main) {
                            DeviceStore.fetchVCards()
                        }
                    }
                    if (state == RegistrationState.Failed) {
                        coreContext.core.removeListener(coreListener)
                        sipRegistered.value = false
                    }
                }
            }
            coreContext.core.addListener(coreListener)
            account.refreshRegister()
        }
    }

    fun pushGateway(): org.linphone.core.Account? {
        return coreContext.core.getAccountByIdkey(PUSH_GW_ID_KEY)
    }

    fun createPushGateway(pushReady: MutableLiveData<Boolean>) {

        if (get()?.params?.domain == corePreferences.loginDomain) {
            get()?.params?.clone()?.also { newParams ->
                newParams?.pushNotificationAllowed = true
                get()?.params = newParams
            }
            Log.i("No need to create a push gateway for this account, as it supports push notifications. setting push notification parameters instead")
            return
        }

        if (!corePreferences.automaticallyCreatePushGatewayAccount) {
            Log.i("Skipping push gateway creation as disabled in config (section app/auto_create_push_gateway_account)")
            pushReady.value = true
            return
        }


        coreContext.core.loadConfigFromXml(corePreferences.linhomeAccountDefaultValuesPath)


        val xmlRpcSession = corePreferences.xmlRpcServerUrl?.let {
            coreContext.core.createXmlRpcSession(
                it
            )
        }
        val xmlRpcRequest =
            xmlRpcSession?.createRequest(XmlRpcArgType.StringStruct, "create_push_account")
        xmlRpcRequest?.addStringArg(coreContext.core.userAgent)
        corePreferences.loginDomain?.let { xmlRpcRequest?.addStringArg(it) }
        corePreferences.passwordAlgo?.let { xmlRpcRequest?.addStringArg(it) }

        xmlRpcRequest?.addListener { request ->
            val responseValues = request.listResponse
            if (request.status == XmlRpcStatus.Ok) {
                coreContext.core.createAccountParams().also { params ->
                    params.idkey = PUSH_GW_ID_KEY
                    params.isRegisterEnabled = true
                    params.isPublishEnabled = false
                    params.expires = 31536000
                    Factory.instance().createAddress("sips:${responseValues.get(1)};transport=tls")?.also {
                        params.setRoutesAddresses(arrayOf(it))
                    }
                    params.pushNotificationAllowed = true
                    coreContext.core.createAddress("sip:${responseValues.get(0)}@${responseValues.get(1)}")?.let {
                        params.setIdentityAddress(it)
                    }
                    val authInfo = Factory.instance().createAuthInfo(responseValues.get(0),responseValues.get(0),null,responseValues.get(2),responseValues.get(1),responseValues.get(1))
                    coreContext.core.addAuthInfo(authInfo)
                    val pushGw = coreContext.core.createAccount(params)
                    coreContext.core.addAccount(pushGw)
                    linkProxiesWithPushGateway(pushReady)
                }
            } else {
                pushReady.value = false
            }
        }
        if (xmlRpcRequest != null) {
            xmlRpcSession.sendRequest(xmlRpcRequest)
        }

    }

    fun linkProxiesWithPushGateway(pushReady: MutableLiveData<Boolean>) {
        pushGateway()?.also { pgw ->
            coreContext.core.accountList.forEach {
                if (it.params.idkey != PUSH_GW_ID_KEY) {
                    it.dependency = pgw
                    pgw.params.clone().also { clonedParams ->
                        clonedParams.expires = it.params.expires
                        pgw.params = clonedParams
                        pgw.refreshRegister()
                    }
                }
            }
        }
        pushReady.value = true
    }


    fun disconnect() {
        coreContext.core.accountList.forEach {
            it.params.clone().also { newParams ->
                newParams.expires = 0
                newParams.pushNotificationAllowed = false
                it.params = newParams
            }
            coreContext.core.removeAccount(it)
        }
        coreContext.core.cleanHistory()
        coreContext.core.provisioningUri = null
        coreContext.core.config.setString("misc","config-uri",null)
        coreContext.core.stop()
        LinhomeApplication.ensureCoreExists(coreContext.context, force = true)
        DeviceStore.clearRemoteProvisionnedDevicesUponLogout()
    }

    fun delete() {
        coreContext.core.accountList.forEach {
            coreContext.core.removeAccount(it)
        }
    }

}
