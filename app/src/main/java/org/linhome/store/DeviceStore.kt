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

package org.linhome.store

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.linhome.LinhomeApplication
import org.linhome.entities.Action
import org.linhome.entities.Device
import org.linhome.linphonecore.extensions.getString
import org.linhome.linphonecore.extensions.isValid
import org.linhome.store.StorageManager.devicesXml
import org.linphone.core.*
import org.linphone.mediastream.Log

object DeviceStore {

    private var devicesConfig: Config

    var devices: ArrayList<Device>

    val local_devices_fl_name = "local_devices"
    var localDevicesFriendList = LinhomeApplication.coreContext.core.getFriendListByName(local_devices_fl_name)

    private val coreListener: CoreListenerStub = object : CoreListenerStub() {
        override fun onFriendListCreated(core: Core, friendList: FriendList) {
            if (core.globalState == GlobalState.On) {
                Log.i("[DeviceStore] friend list created. ${friendList.displayName}")
                devices = readFromFriends()
            }
        }
    }

    init {
        if (localDevicesFriendList == null) {
            localDevicesFriendList = LinhomeApplication.coreContext.core.createFriendList()
            localDevicesFriendList?.also {
                it.displayName = local_devices_fl_name
                LinhomeApplication.coreContext.core.addFriendList(it)
            }
        }
        devicesConfig = Factory.instance().createConfig(null)
        if (devicesXml.exists()) {
            GlobalScope.launch(context = Dispatchers.Main) {
                delay(1000)
                devicesConfig.loadFromXmlFile(devicesXml.absolutePath)
                devices = readFromXml()
                saveLocalDevices()
                devicesXml.delete()
            }
        }
        devices = readFromFriends()
        LinhomeApplication.coreContext.core.addListener(coreListener)
    }

    fun readFromFriends(): ArrayList<Device> {
        val result = ArrayList<Device>()
        LinhomeApplication.coreContext.core.getFriendListByName(local_devices_fl_name)?.friends?.forEach { friend ->
            val card = friend.vcard
            if (card != null && card.isValid()) {
                result.add(Device(card, false))
                Log.i("[DeviceStore] added local device : ${friend.vcard?.asVcard4String()} ")
            } else {
                Log.e("[DeviceStore] unable to create device from card (card is null or invdalid) : ${friend.vcard?.asVcard4String()} ")
            }
        }
        LinhomeApplication.coreContext.core.config.getString("misc","contacts-vcard-list",null)?.also { url ->
            Log.i("[DeviceStore] Found contacts-vcard-list url : ${url} ")
            LinhomeApplication.coreContext.core.getFriendListByName(url)?.also { serverFriendList ->
                Log.i("[DeviceStore] Found remote friend list : ${serverFriendList.displayName} ")
                serverFriendList.friends.forEach { friend ->
                    Log.i("[DeviceStore] Found remote friend  : ${friend.name} ")
                    val card = friend.vcard
                    if (card != null && card.isValid()) {
                        val device = Device(card!!, true)
                        if (result.filter { it.address == device.address }.isEmpty())
                            result.add(device)
                        Log.i("[DeviceStore] added remote device : ${friend.vcard?.asVcard4String()} ")
                    } else {
                        Log.e("[DeviceStore] received invalid or malformed vCard from remote : ${friend.vcard?.asVcard4String()} ")
                    }
                }
            }
        }

        result.sortWith(compareBy({ it.name }, { it.address }))
        return result
    }

    fun readFromXml(): ArrayList<Device> {
        val result = ArrayList<Device>()
        devicesConfig.sectionsNamesList.forEach {
            val actions = ArrayList<Action>()
            devicesConfig.getString(it, "actions", null)
                ?.also { // Actions are type1,code1|type2,code2 ...
                    it.split("|").forEach {
                        actions.add(Action(it.split(",").first(), it.split(",").last()))
                    }
                }
            result.add(
                Device(
                    it,
                    devicesConfig.getString(it, "type", null),
                    devicesConfig.getString(it, "name", nonNullDefault = "missing"),
                    devicesConfig.getString(it, "address", nonNullDefault = "missing"),
                    devicesConfig.getString(it, "actions_method_type", null),
                    actions,
                    false
                )
            )
        }
        result.sortWith(compareBy({ it.name }, { it.address }))
        return result
    }

    fun saveLocalDevices() {
        localDevicesFriendList?.friends?.forEach {
            localDevicesFriendList!!.removeFriend(it)
        }

        devices.sortWith(compareBy({ it.name }, { it.address }))
        devices.forEach { device ->
            val friend = device.friend
            if (!device.isRemotelyProvisionned) {
                localDevicesFriendList?.addFriend (friend)
            }
        }
    }

    fun persistDevice(device: Device) {
        devices.add(device)
        saveLocalDevices()
    }

    fun removeDevice(device: Device) {
        device.thumbNail.also {
            if (it.exists())
                it.delete()
        }
        devices.remove(device)
        saveLocalDevices()
    }

    fun findDeviceByAddress(address: Address): Device? {
        devices.forEach {
            if (it.address.equals(address.asStringUriOnly()))
                return it
        }
        return null
    }

    fun findDeviceByAddress(address: String?): Device? {
        return address?.let { addressString ->
            LinhomeApplication.coreContext.core.createAddress(addressString)
                ?.let { addressAddress ->
                    return findDeviceByAddress(addressAddress)
                }
        }
    }

    fun clearRemoteProvisionnedDevicesUponLogout() {
        LinhomeApplication.coreContext.core.config.getString("misc","contacts-vcard-list",null)?.also { url ->
            Log.i("[DeviceStore] Found contacts-vcard-list url : ${url} ")
            LinhomeApplication.coreContext.core.getFriendListByName(url)?.also { serverFriendList ->
                LinhomeApplication.coreContext.core.removeFriendList(serverFriendList)
                devices = readFromFriends()
            }
        }
    }

}