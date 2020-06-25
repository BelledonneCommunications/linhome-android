package org.lindoor.entities

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import org.lindoor.LindoorApplication
import org.lindoor.LindoorApplication.Companion.coreContext
import org.lindoor.LindoorApplication.Companion.corePreferences
import org.lindoor.utils.cdlog
import org.lindoor.utils.extensions.xDigitsUUID
import org.linphone.core.AccountCreator
import org.linphone.core.AccountCreatorListenerStub
import org.linphone.core.ProxyConfig
import java.util.*

object Account {

    private const val PUSH_GW_ID_KEY = "lindoor_pushgateway"
    private const val PUSH_GW_USER_PREFIX = "lindoor_generated"
    private const val PUSH_GW_DISPLAY_NAME = "Lindoor"

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

    fun lindoorAccountCreate(accountCreator: AccountCreator) {
        accountCreator.createProxyConfig().idkey = PUSH_GW_ID_KEY
    }

    fun lindoorAccountLogin(accountCreator: AccountCreator) {
        accountCreator.createProxyConfig().idkey = PUSH_GW_ID_KEY
    }

    fun sipAccountLogin(
        accountCreator: AccountCreator,
        proxy: String?,
        expiration: String,
        pushReady: MutableLiveData<Boolean>
    ) {
        val proxyConfig: ProxyConfig? = accountCreator.createProxyConfig()
        proxyConfig?.expires = expiration.toInt()
        proxyConfig?.serverAddr = if (!TextUtils.isEmpty(proxy)) proxy else accountCreator.domain
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
        val accountCreator =
            coreContext.core.createAccountCreator(corePreferences.xmlRpcServerUrl)
        accountCreator.language = Locale.getDefault().language
        val user: String = PUSH_GW_USER_PREFIX + xDigitsUUID()
        val pass = UUID.randomUUID().toString()
        accountCreator.domain = corePreferences.loginDomain
        accountCreator.username = user
        accountCreator.password = pass
        accountCreator.email = "$user@${accountCreator.domain}"
        accountCreator.displayName = PUSH_GW_DISPLAY_NAME

        accountCreator.addListener(object : AccountCreatorListenerStub() {
            override fun onCreateAccount(
                creator: AccountCreator?,
                status: AccountCreator.Status?,
                resp: String?
            ) {
                if (status == AccountCreator.Status.AccountCreated) // TODO Adjust when server setup
                    creator?.also {
                        val pushGw = it.createProxyConfig()
                        pushGw.idkey = PUSH_GW_ID_KEY
                        pushGw.enableRegister(true)
                        pushGw.enablePublish(false)
                        pushGw.expires = 31536000
                        pushGw.serverAddr = "sips:${it.domain}:5061;transport=tls"
                        pushGw.route = pushGw.serverAddr
                        pushGw.isPushNotificationAllowed = true
                        linkProxiesWithPushGateway(pushReady)
                    }
            }
        })
        if ( accountCreator.createAccount()!= AccountCreator.Status.RequestOk)
            pushReady.value = false
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
