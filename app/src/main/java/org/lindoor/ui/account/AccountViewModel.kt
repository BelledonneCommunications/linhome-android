package org.lindoor.ui.account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.lindoor.LindoorApplication
import org.lindoor.customisation.Texts
import org.lindoor.entities.Account
import org.lindoor.entities.Device
import org.lindoor.linphonecore.extensions.callLogsWithNonEmptyCallId
import org.lindoor.linphonecore.extensions.isNew
import org.lindoor.linphonecore.extensions.toHumanReadable
import org.lindoor.store.DeviceStore
import org.lindoor.utils.cdlog
import org.linphone.core.*

class AccountViewModel : ViewModel() {
    val account = Account.get()
    val pushGw = Account.pushGateway()
    val accountDesc = MutableLiveData(getDescription("account_info",account))
    val pushGWDesc = MutableLiveData(getDescription("push_account_info",pushGw))

    private val coreListener = object : CoreListenerStub() {
        override fun onRegistrationStateChanged(
            lc: Core?,
            cfg: ProxyConfig?,
            cstate: RegistrationState?,
            message: String?
        ) {
            if (cfg != null) {
                if (cfg == account)
                    accountDesc.value = getDescription("account_info",account)
                if (cfg == pushGw)
                    pushGWDesc.value = getDescription("push_account_info",pushGw)
            }
        }
    }

    init {
        LindoorApplication.coreContext.core.proxyConfigList.forEach {
            cdlog("${it.identityAddress.asString()} ${it.idkey}")
        }
        LindoorApplication.coreContext.core.addListener(coreListener)
    }

    override fun onCleared() {
        LindoorApplication.coreContext.core.removeListener(coreListener)
        super.onCleared()
    }

    fun refreshRegisters() {
        account?.refreshRegister()
        pushGw?.refreshRegister()
    }


    fun getDescription(key:String, proxyConfig: ProxyConfig?): String? {
        return account?.state?.toHumanReadable()?.let {
            proxyConfig?.identityAddress?.asStringUriOnly()?.let { it1 ->
                Texts.get(key, it1,
                    it
                )
            }
        } ?: Texts.get("no_account_configured")
    }


}
