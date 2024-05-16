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
import org.linhome.LinhomeApplication
import org.linhome.LinhomeApplication.Companion.coreContext
import org.linhome.LinhomeApplication.Companion.corePreferences
import org.linhome.entities.LinhomeAccount
import org.linhome.linphonecore.CorePreferences
import org.linhome.ui.assistant.shared.FlexiApiPushAccountCreationViewModel
import org.linphone.core.ConfiguringState
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub
import org.linphone.core.tools.Log


class RemoteAnyAccountViewModel : FlexiApiPushAccountCreationViewModel(LinhomeApplication.corePreferences.linhomeAccountDefaultValuesPath) {

    var url: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> = Pair(MutableLiveData(), MutableLiveData(false))

    var configurationResult = MutableLiveData<ConfiguringState>()


    private val coreListener = object : CoreListenerStub() {
        override fun onConfiguringStatus(core: Core, status: ConfiguringState, message: String?) {
            if (status == ConfiguringState.Successful) {
                GlobalScope.launch(context = Dispatchers.Main) {
                    if (LinhomeAccount.get()?.params?.domain != corePreferences.loginDomain) {
                        if (LinhomeAccount.pushGateway() != null) {
                            LinhomeAccount.linkProxiesWithPushAccount(pushReady)
                        } else
                            createPushAccount()
                    } else {
                        pushReady.value = true
                        Log.i("Remote provisioning - no need to create/link push gateway, as domain ${corePreferences.loginDomain} is managed by flexisip already.")
                    }
                }
            }
            configurationResult.postValue(status)
        }

        override fun onQrcodeFound(core: Core, qr: String?) {
            GlobalScope.launch(context = Dispatchers.Main) {
                coreContext.core.isQrcodeVideoPreviewEnabled = false
                coreContext.core.isVideoPreviewEnabled = false
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
