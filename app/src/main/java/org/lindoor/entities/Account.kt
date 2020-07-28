package org.lindoor.entities

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import org.lindoor.LindoorApplication
import org.lindoor.LindoorApplication.Companion.coreContext
import org.lindoor.LindoorApplication.Companion.corePreferences
import org.lindoor.utils.cdlog
import org.linphone.core.*

object Account {

    private const val PUSH_GW_ID_KEY = "lindoor_pushgateway"

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

    fun lindoorAccountCreateProxyConfig(accountCreator: AccountCreator) {
        accountCreator.createProxyConfig()
    }

    fun sipAccountLogin(
        accountCreator: AccountCreator,
        proxy: String?,
        expiration: String,
        pushReady: MutableLiveData<Boolean>
    ) {
        val proxyConfig: ProxyConfig? = accountCreator.createProxyConfig()
        proxyConfig?.expires = expiration.toInt()
        if (!TextUtils.isEmpty(proxy)) {
            proxyConfig?.serverAddr = proxy
        }
        if (pushGateway() != null)
            linkProxiesWithPushGateway(pushReady)
        else
            createPushGateway(pushReady)
    }

    fun pushGateway(): ProxyConfig? {
        return coreContext.core.getProxyConfigByIdkey(PUSH_GW_ID_KEY)
    }

    fun createPushGateway(pushReady: MutableLiveData<Boolean>) {
        coreContext.core.loadConfigFromXml(corePreferences.lindoorAccountDefaultValuesPath)


        val xmlRpcSession = corePreferences.xmlRpcServerUrl?.let {
            LindoorApplication.coreContext.core.createXmlRpcSession(
                it
            )
        }
        val xmlRpcRequest =
            xmlRpcSession?.createRequest(XmlRpcArgType.StringStruct, "create_push_account")
        xmlRpcRequest?.addStringArg(coreContext.core.userAgent)

        xmlRpcRequest?.setListener { request ->
            val status = request.status
            val responseValues = request.listResponse
            if (request.status == XmlRpcStatus.Ok) {
                val pushGw = coreContext.core.createProxyConfig()
                pushGw.idkey = PUSH_GW_ID_KEY
                pushGw.enableRegister(true)
                pushGw.enablePublish(false)
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
    }

}
