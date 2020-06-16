package org.lindoor.entities

import androidx.lifecycle.MutableLiveData
import org.lindoor.LindoorApplication
import org.lindoor.LindoorApplication.Companion.coreContext
import org.lindoor.LindoorApplication.Companion.corePreferences
import org.lindoor.utils.extensions.xDigitsUUID
import org.linphone.core.AccountCreator
import org.linphone.core.AccountCreatorListenerStub
import org.linphone.core.ProxyConfig
import java.util.*

object Account {

    private const val PUSH_GW_REF_KEY = "lindoor_pushgateway"
    private const val PUSH_GW_USER_PREFIX = "lindoor_generated"
    private const val PUSH_GW_DISPLAY_NAME = "Lindoor"

    fun configured(): Boolean {
        return coreContext.core.proxyConfigList.isNotEmpty()
    }

    fun get(): ProxyConfig? {
        return coreContext.core.defaultProxyConfig
    }

    fun lindoorAccountCreate(accountCreator: AccountCreator) {
        accountCreator.createProxyConfig().refKey = PUSH_GW_REF_KEY
    }

    fun lindoorAccountLogin(accountCreator: AccountCreator) {
        accountCreator.createProxyConfig().refKey = PUSH_GW_REF_KEY
    }

    fun sipAccountLogin(
        accountCreator: AccountCreator,
        proxy: String?,
        expiration: String,
        pushReady: MutableLiveData<Boolean>
    ) {
        val proxyConfig: ProxyConfig? = accountCreator.createProxyConfig()
        proxyConfig?.expires = expiration.toInt()
        proxy?.also {
            proxyConfig?.serverAddr = it
        }
        if (pushGateway() != null)
            linkProxiesWithPushGateway()
        else
            createPushGateway(pushReady)
    }

    fun pushGateway(): ProxyConfig? {
        return coreContext.core.getProxyConfigByIdkey(PUSH_GW_REF_KEY)
    }

    fun createPushGateway(pushReady: MutableLiveData<Boolean>) {
        coreContext.core.loadConfigFromXml(LindoorApplication.corePreferences.lindoorAccountDefaultValuesPath)
        val accountCreator =
            coreContext.core.createAccountCreator(LindoorApplication.corePreferences.xmlRpcServerUrl)
        accountCreator.language = Locale.getDefault().language
        val user: String = PUSH_GW_USER_PREFIX + xDigitsUUID()
        val pass = UUID.randomUUID().toString()
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
                        pushGw.refKey = PUSH_GW_REF_KEY
                        pushGw.enableRegister(true)
                        pushGw.enablePublish(false)
                        pushGw.expires = 31536000
                        pushGw.serverAddr = "sips:${it.domain}:5061;transport=tls"
                        pushGw.route = pushGw.serverAddr
                        pushGw.isPushNotificationAllowed = true
                        linkProxiesWithPushGateway()
                        pushReady.value = true
                    }
            }
        })
        accountCreator.createAccount()
    }

    fun linkProxiesWithPushGateway() {
        pushGateway()?.also { pgw ->
            coreContext.core.proxyConfigList.forEach {
                if (it.refKey != PUSH_GW_REF_KEY) {
                    it.dependency = pgw
                }
            }
        }
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
