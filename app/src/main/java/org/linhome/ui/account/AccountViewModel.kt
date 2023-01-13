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

package org.linhome.ui.account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.linhome.LinhomeApplication
import org.linhome.customisation.Texts
import org.linhome.entities.Account
import org.linhome.linphonecore.extensions.toHumanReadable
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.ProxyConfig
import org.linphone.core.RegistrationState

class AccountViewModel : ViewModel() {
    val account = Account.get()
    val pushGw = Account.pushGateway()
    val accountDesc = MutableLiveData(getDescription("account_info",account))
    val pushGWDesc = MutableLiveData(getDescription("push_account_info",pushGw))

    private val coreListener = object : CoreListenerStub() {
        override fun onRegistrationStateChanged(
            lc: Core,
            cfg: ProxyConfig,
            cstate: RegistrationState?,
            message: String
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
        LinhomeApplication.coreContext.core.addListener(coreListener)
    }

    override fun onCleared() {
        LinhomeApplication.coreContext.core.removeListener(coreListener)
        super.onCleared()
    }

    fun refreshRegisters() {
        account?.refreshRegister()
        pushGw?.refreshRegister()
    }


    fun getDescription(key:String, proxyConfig: ProxyConfig?): String? {
        return proxyConfig?.state?.toHumanReadable()?.let {
            proxyConfig?.identityAddress?.asStringUriOnly()?.let { it1 ->
                Texts.get(key, it1,
                    it
                )
            }
        } ?: Texts.get("no_account_configured")
    }


}
