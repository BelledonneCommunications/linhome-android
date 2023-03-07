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

package org.linhome.ui.devices

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.linhome.LinhomeApplication
import org.linhome.entities.Device
import org.linhome.store.DeviceStore
import org.linphone.core.*
import org.linphone.mediastream.Log

class DevicesViewModel : ViewModel() {
    var devices = MutableLiveData<ArrayList<Device>>().apply {
        value = DeviceStore.devices
    }
    var selectedDevice = MutableLiveData<Device?>()
    var friendListUpdatedOk = MutableLiveData<Boolean>()

    val friendListListener: FriendListListenerStub = object : FriendListListenerStub() {
        override fun onSyncStatusChanged(
            friendList: FriendList,
            status: FriendList.SyncStatus?,
            message: String?
        ) {
            if (status == FriendList.SyncStatus.Successful || status == FriendList.SyncStatus.Failure) {
                devices.value = DeviceStore.devices
                friendListUpdatedOk.value = status == FriendList.SyncStatus.Successful
            }
        }
    }

    private val coreListener: CoreListenerStub = object : CoreListenerStub() {
        override fun onGlobalStateChanged(core: Core, state: GlobalState, message: String) {
            Log.i("[Context] Global state changed [$state]")
            if (state == GlobalState.On) {
                DeviceStore.devices = DeviceStore.readFromFriends()
                devices.value = DeviceStore.devices
            }
        }
        override fun onFriendListCreated(core: Core, friendList: FriendList) {
            Log.i("[DeviceStore] friend list created. ${friendList.displayName}")
            DeviceStore.devices = DeviceStore.readFromFriends()
            devices.value = DeviceStore.devices
            friendListUpdatedOk.value = true
        }
    }

    init {
        LinhomeApplication.coreContext.core.addListener(coreListener)
    }

    override fun onCleared() {
        LinhomeApplication.coreContext.core.removeListener(coreListener)
    }

}
