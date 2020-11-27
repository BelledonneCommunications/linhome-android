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

package org.linhome.ui.assistant.remote

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.linhome.LinhomeApplication.Companion.coreContext
import org.linhome.entities.Account
import org.linphone.core.ConfiguringState
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub


class RemoteAnyAccountViewModel : ViewModel() {

    var url: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> = Pair(MutableLiveData(), MutableLiveData(false))

    var configurationResult = MutableLiveData<ConfiguringState>()
    val pushReady = MutableLiveData<Boolean>()


    private val coreListener = object : CoreListenerStub() {
        override fun onConfiguringStatus(core: Core, status: ConfiguringState, message: String?) {
            if (status == ConfiguringState.Successful) {
                if (Account.pushGateway() != null) {
                    Account.linkProxiesWithPushGateway(pushReady)
                } else
                    Account.createPushGateway(pushReady)
            }
            configurationResult.postValue(status)
        }

        override fun onQrcodeFound(core: Core, qr: String?) {
            GlobalScope.launch(context = Dispatchers.Main) {
                coreContext.core.enableQrcodeVideoPreview(false)
                coreContext.core.enableVideoPreview(false)
                url.first.value = qr
                startRemoteProvisionning()
            }
        }
    }

    fun valid(): Boolean {
        return url.second.value!!
    }

    init {
        coreContext.core.addListener(coreListener)
    }

    fun startRemoteProvisionning() {
        coreContext.core.provisioningUri = url.first.value
        coreContext.core.stop()
        coreContext.core.start()
    }

    override fun onCleared() {
        coreContext.core.removeListener(coreListener)
        super.onCleared()
    }


}
