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

package org.linhome.ui.assistant.loginsip

import android.text.TextUtils
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.linhome.LinhomeApplication
import org.linhome.LinhomeApplication.Companion.coreContext
import org.linhome.LinhomeApplication.Companion.corePreferences
import org.linhome.entities.LinhomeAccount
import org.linhome.store.DeviceStore
import org.linhome.ui.assistant.shared.CreatorAssistantViewModel
import org.linhome.ui.assistant.shared.FlexiApiPushAccountCreationViewModel
import org.linphone.core.Account
import org.linphone.core.AccountCreator
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.Factory
import org.linphone.core.RegistrationState
import org.linphone.core.TransportType
import org.linphone.core.tools.Log

class LoginSipAccountViewModel :
    FlexiApiPushAccountCreationViewModel(corePreferences.sipAccountDefaultValuesPath) {

    var username: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> =
        Pair(MutableLiveData(), MutableLiveData<Boolean>(false))
    var domain: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> =
        Pair(MutableLiveData(), MutableLiveData<Boolean>(false))
    var pass1: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> =
        Pair(MutableLiveData(), MutableLiveData<Boolean>(false))
    var transport: MutableLiveData<Int> = MutableLiveData<Int>(TransportType.Tls.toInt())
    var proxy: Pair<MutableLiveData<String?>, MutableLiveData<Boolean>> =
        Pair(MutableLiveData(), MutableLiveData<Boolean>(false))
    var expiration: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> = Pair(
        MutableLiveData<String>(
            corePreferences.config.getString(
                "proxy_default_values",
                "reg_expires",
                "31536000"
            )
        ), MutableLiveData<Boolean>(false)
    )

    val moreOptionsOpened = MutableLiveData(false)
    val sipRegistered = MutableLiveData<Boolean>()
    var coreListener : CoreListenerStub? = null

    protected fun transportToString(transport: TransportType) : String {
        when (transport) {
            TransportType.Tls -> return "tls"
            TransportType.Tcp -> return "tcp"
            TransportType.Udp -> return "udp"
            TransportType.Dtls -> return "dtls"
        }
    }

    fun sipAccountLogin(
        proxy: String?,
        expiration: String,
        sipRegistered: MutableLiveData<Boolean>
    ) {
        val transports = arrayOf("udp","tcp","tls")
        accountCreator.createAccountInCore()
        if (LinhomeApplication.coreContext.core.accountList.isEmpty()) {
            sipRegistered.value = false
            return
        }
        LinhomeApplication.coreContext.core.accountList.first()?.also { account ->
            Log.i("[Account] created Account with domain ${account.params.domain}")
            account.params.clone().also { newParams ->
                newParams.expires = expiration.toInt()
                newParams.transport = accountCreator.transport
                if (!TextUtils.isEmpty(proxy)) {
                    val cleanProxy = if (proxy?.startsWith("sip") == true) proxy.replace("sip:","")
                        .replace("sips:","") else proxy
                    Factory.instance().createAddress((if (accountCreator.transport == TransportType.Tls)  "sips:" else "sip:") + cleanProxy!! + ";transport="+transportToString(accountCreator.transport))?.also {
                        Log.i("[Account] Setting route address to ${it.asString()}")
                        newParams.setRoutesAddresses(arrayOf(it))
                    }
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
                        LinhomeApplication.coreContext.core.removeListener(coreListener)
                        sipRegistered.value = true
                        handlePushAccount()
                        GlobalScope.launch(context = Dispatchers.Main) {
                            DeviceStore.fetchVCards()
                        }
                    }
                    if (state == RegistrationState.Failed) {
                        LinhomeApplication.coreContext.core.removeListener(coreListener)
                        sipRegistered.value = false
                    }
                }
            }
            LinhomeApplication.coreContext.core.addListener(coreListener)
            account.refreshRegister()
        }
    }

    fun valid(): Boolean {
        return username.second.value!! && domain.second.value!! && pass1.second.value!! && expiration.second.value!!
    }

}
