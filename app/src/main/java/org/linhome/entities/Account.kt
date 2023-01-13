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
import org.linhome.LinhomeApplication
import org.linhome.LinhomeApplication.Companion.coreContext
import org.linhome.LinhomeApplication.Companion.corePreferences
import org.linhome.linphonecore.extensions.cleanHistory
import org.linphone.core.*
import org.linphone.core.tools.Log

object Account {

    private const val PUSH_GW_ID_KEY = "linhome_pushgateway"

    fun configured(): Boolean {
        return coreContext.core.proxyConfigList.isNotEmpty()
    }

    fun get(): ProxyConfig? {
        coreContext.core.proxyConfigList.forEach {
            if (PUSH_GW_ID_KEY != it.idkey)
                return it
        }
        return null
    }

    fun linhomeAccountCreateProxyConfig(accountCreator: AccountCreator) {
        accountCreator.createProxyConfig()?.let { proxyConfig ->
            proxyConfig.findAuthInfo()?.algorithm = corePreferences.passwordAlgo
            proxyConfig.isPushNotificationAllowed = true
        }
    }

    fun sipAccountLogin(
        accountCreator: AccountCreator,
        proxy: String?,
        expiration: String,
        pushReady: MutableLiveData<Boolean>
    ) {
        val transports = arrayOf("udp","tcp","tls")
        accountCreator.createProxyConfig()
        coreContext.core.accountList.first()?.also { account ->
            Log.i("[Account] created proxyConfig with domain ${account.params.domain}")
            account.params.expires = expiration.toInt()
            if (!TextUtils.isEmpty(proxy)) {
                val address = (if (accountCreator.transport == TransportType.Tls)  "sips:" else "sip:") + proxy!! + ";transport="+transports.get(accountCreator.transport.toInt())
                account.params.serverAddr = address
                Log.i("[Account] Set proxyConfig server address to ${account.params.serverAddr} for proxyConfig with domain ${account.params.domain}")
            }
            if (pushGateway() != null)
                linkProxiesWithPushGateway(pushReady)
            else
                createPushGateway(pushReady)
        }

    }

    fun pushGateway(): ProxyConfig? {
        return coreContext.core.getProxyConfigByIdkey(PUSH_GW_ID_KEY)
    }

    fun createPushGateway(pushReady: MutableLiveData<Boolean>) {
        coreContext.core.loadConfigFromXml(corePreferences.linhomeAccountDefaultValuesPath)


        val xmlRpcSession = corePreferences.xmlRpcServerUrl?.let {
            LinhomeApplication.coreContext.core.createXmlRpcSession(
                it
            )
        }
        val xmlRpcRequest =
            xmlRpcSession?.createRequest(XmlRpcArgType.StringStruct, "create_push_account")
        xmlRpcRequest?.addStringArg(coreContext.core.userAgent)
        corePreferences.loginDomain?.let { xmlRpcRequest?.addStringArg(it) }
        corePreferences.passwordAlgo?.let { xmlRpcRequest?.addStringArg(it) }

        xmlRpcRequest?.addListener { request ->
            val status = request.status
            val responseValues = request.listResponse
            if (request.status == XmlRpcStatus.Ok) {
                val pushGw = coreContext.core.createProxyConfig()
                pushGw.idkey = PUSH_GW_ID_KEY
                pushGw.isRegisterEnabled = true
                pushGw.isPublishEnabled = false
                pushGw.expires = 31536000
                pushGw.serverAddr = "sips:${responseValues.get(1)};transport=tls"
                pushGw.setRoutes(arrayOf(pushGw.serverAddr))
                pushGw.isPushNotificationAllowed = true

                coreContext.core.createAddress("sip:${responseValues.get(0)}@${responseValues.get(1)}")?.let {
                    pushGw.setIdentityAddress(it)
                }
                val authInfo = Factory.instance().createAuthInfo(responseValues.get(0),responseValues.get(0),null,responseValues.get(2),responseValues.get(1),responseValues.get(1))
                coreContext.core.addAuthInfo(authInfo)
                coreContext.core.addProxyConfig(pushGw)
                linkProxiesWithPushGateway(pushReady)
            } else {
                pushReady.value = false
            }
        }
        if (xmlRpcRequest != null) {
            xmlRpcSession?.sendRequest(xmlRpcRequest)
        }


    }

    fun linkProxiesWithPushGateway(pushReady: MutableLiveData<Boolean>) {
        pushGateway()?.also { pgw ->
            coreContext.core.proxyConfigList.forEach {
                if (it.idkey != PUSH_GW_ID_KEY) {
                    it.dependency = pgw
                }
            }
        }
        pushReady.value = true
    }


    fun disconnect() {
        coreContext.core.proxyConfigList.forEach {
            it.edit()
            it.expires = 0
            it.isPushNotificationAllowed = false
            it.done()
            coreContext.core.removeProxyConfig(it)
        }
        coreContext.core.cleanHistory()
        coreContext.core.provisioningUri = null
        coreContext.core.config.setString("misc","config-uri",null)
        coreContext.core.stop()
        LinhomeApplication.ensureCoreExists(coreContext.context, force = true)
    }

}
