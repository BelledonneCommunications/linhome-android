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

import androidx.lifecycle.MutableLiveData
import org.linhome.LinhomeApplication
import org.linhome.LinhomeApplication.Companion.coreContext
import org.linhome.LinhomeApplication.Companion.corePreferences
import org.linhome.linphonecore.extensions.cleanHistory
import org.linhome.store.DeviceStore
import org.linphone.core.Account
import org.linphone.core.CoreListenerStub

object LinhomeAccount {

    const val PUSH_GW_ID_KEY = "linhome_pushgateway"

    private lateinit var coreListener : CoreListenerStub

    fun configured(): Boolean {
        return coreContext.core.accountList.isNotEmpty()
    }

    fun get(): Account? {
        coreContext.core.accountList.forEach {
            if (PUSH_GW_ID_KEY != it.params.idkey)
                return it
        }
        return null
    }

    fun pushGateway(): Account? {
        return coreContext.core.getAccountByIdkey(PUSH_GW_ID_KEY)
    }

    fun linkProxiesWithPushAccount(pushReady: MutableLiveData<Boolean>) {
        pushGateway()?.also { pushAccount ->
            coreContext.core.accountList.forEach {
                if (it.params.idkey != PUSH_GW_ID_KEY) {
                    it.dependency = pushAccount
                    pushAccount.params.clone().also { clonedParams ->
                        clonedParams.expires = it.params.expires
                        pushAccount.params = clonedParams
                        pushAccount.refreshRegister()
                    }
                }
            }
            enablePushAccount(pushAccount)
        }
        pushReady.value = true
    }


    fun disconnect() {
        coreContext.core.accountList.forEach {
            if (it.params?.idkey == PUSH_GW_ID_KEY) {
                disablePushAccount(it)
            } else {
                it.params.clone().also { newParams ->
                    newParams.expires = 0
                    it.params = newParams
                }
                it.refreshRegister()
                coreContext.core.removeAccount(it)
            }
        }
        coreContext.core.cleanHistory()
        coreContext.core.provisioningUri = null
        coreContext.core.config.setString("misc","config-uri",null)
        coreContext.core.stop()
        LinhomeApplication.ensureCoreExists(coreContext.context, force = true)
        DeviceStore.clearRemoteProvisionnedDevicesUponLogout()
    }

     fun disablePushAccount(pushAccount:Account) {
        pushAccount.params.clone().also {
            it.expires = 0
            pushAccount.params = it
            pushAccount.refreshRegister()
        }
    }

    fun enablePushAccount(pushAccount:Account) {
        pushAccount.params.clone().also {
            it.expires = corePreferences.config.getInt( "proxy_default_values","reg_expires", 31536000)
            pushAccount.params = it
            pushAccount.refreshRegister()
        }
    }

}
