package org.lindoor.entities

import org.lindoor.LindoorApplication
import org.linphone.core.AccountCreator
import org.linphone.core.Config
import org.linphone.core.Factory
import org.linphone.core.ProxyConfig
import java.io.File

enum class AccountType { Lindoor, External }

object Account {

    private var accountXml = File(LindoorApplication.instance.filesDir, "account.xml")
    private var accountConfig: Config

    init {
        if (!accountXml.exists())
            accountXml.createNewFile()
        accountConfig = Factory.instance().createConfig(null)
        accountConfig.loadFromXmlFile(accountXml.absolutePath)
    }

    fun configured(): Boolean { // TODO WIP - instead use special refkey in proxyconfig
        return accountConfig.getString("account","proxy_refkey",null) != null
    }

    fun configure(accountCreator:AccountCreator, type:AccountType) { // TODO WIP - set special refkey
        val proxyConfig: ProxyConfig? = accountCreator.createProxyConfig()
        accountConfig.setString("account","proxy_refkey",proxyConfig?.refKey)
        accountConfig.setString("account","type",type)
    }

    fun configure(accountCreator:AccountCreator, type:AccountType, proxy:String?, expiration:String) { // TODO WIP - set special refkey
        val proxyConfig: ProxyConfig? = accountCreator.createProxyConfig()
        proxyConfig?.expires = expiration.toInt()
        proxy?.also {
            proxyConfig?.setServerAddr(it)
        }
        accountConfig.setString("account","proxy_refkey",proxyConfig?.refKey)
        accountConfig.setString("account","type",type)
    }

    fun configure(proxyConfig:ProxyConfig, type:AccountType) { // TODO WIP - set special refkey
        accountConfig.setString("account","proxy_refkey",proxyConfig.refKey)
        accountConfig.setString("account","type",type)
    }



    fun clear() {
    }

    fun disconnect() {
    }

}

private fun Config.setString(s: String, s1: String, type: AccountType) {
    when (type) {
        AccountType.Lindoor -> setString(s,s1,"lindoor")
        AccountType.External -> setString(s,s1,"external")

    }
}
