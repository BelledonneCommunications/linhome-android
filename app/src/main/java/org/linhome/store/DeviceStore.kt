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
import org.linhome.entities.LinhomeAccount
import org.linhome.linphonecore.CorePreferences
import org.linhome.linphonecore.extensions.getString
import org.linhome.linphonecore.extensions.isValid
import org.linhome.store.DeviceStore.devices
import org.linhome.store.DeviceStore.local_devices_fl_name
import org.linhome.store.DeviceStore.readDevicesFromFriends
import org.linhome.store.DeviceStore.saveLocalDevices
import org.linhome.store.StorageManager.devicesXml
import org.linphone.core.*
import org.linphone.mediastream.Log
import kotlin.text.Typography.section

object DeviceStore {

    private lateinit var devicesConfig: Config

    var devices: ArrayList<Device> =  ArrayList()

    val local_devices_fl_name = "local_devices"

    var storageMigrated = false


    private val coreListener: CoreListenerStub = object : CoreListenerStub() {

        override fun onGlobalStateChanged(core: Core, state: GlobalState?, message: String) {
            if (state == GlobalState.On) {
                core.friendsDatabasePath = LinhomeApplication.instance.applicationContext.filesDir.absolutePath+"/devices.db"
                if (core.getFriendListByName(local_devices_fl_name) == null) {
                    val localDevicesFriendList = core.createFriendList()
                    localDevicesFriendList?.displayName = local_devices_fl_name
                    core.addFriendList(localDevicesFriendList)
                }
            }
            GlobalScope.launch(context = Dispatchers.Main) { // Leave one cycle to the core to create the friend list
                if (!storageMigrated) {
                    migrateFromXmlStorage()
                } else {
                    readDevicesFromFriends()
                }
            }
        }
        override fun onConfiguringStatus(core: Core, status: ConfiguringState?, message: String?) {
            if (status == ConfiguringState.Successful) {
                readDevicesFromFriends()
            }
        }
        override fun onFriendListCreated(core: Core, friendList: FriendList) {
            if (core.globalState == GlobalState.On) {
                Log.i("[DeviceStore] friend list created. ${friendList.displayName}")
                readDevicesFromFriends()
            }
        }
    }

    fun migrateFromXmlStorage() {
        val core = LinhomeApplication.coreContext.core
        storageMigrated = true
        if (!devicesXml.exists()) {
            Log.i("[DeviceStore] no xml migration storage to perform")
            return
        }
        devicesConfig = Factory.instance().createConfig(null)
        devicesConfig.loadFromXmlFile(devicesXml.absolutePath)
        devices = readFromXml()
        saveLocalDevices()
        readDevicesFromFriends()
        devicesXml.delete()
        fetchVCards()
        Log.i("[DeviceStore] migration done")
    }

    fun fetchVCards() {
        val core = LinhomeApplication.coreContext.core
        val isLinhomeAccount =
            !core.accountList.filter { it.params?.idkey != LinhomeAccount.PUSH_GW_ID_KEY }
                .isEmpty() && core.accountList.filter { it.params?.idkey != LinhomeAccount.PUSH_GW_ID_KEY }
                .first()?.params?.domain == LinhomeApplication.corePreferences.loginDomain
        if (isLinhomeAccount) {
            Log.i("[DeviceStore] fetching vCards")
            core.config?.setString(
                "misc",
                "contacts-vcard-list",
                "https://subscribe.linhome.org/contacts/vcard"
            )
            core.config?.sync()
            core.stop()
            core.start()
        } else
            Log.i("[DeviceStore] No vards to fetch, as account not from the main domain ${LinhomeApplication.corePreferences.loginDomain}")
    }

    init {
        LinhomeApplication.coreContext.core.addListener(coreListener)
    }

    fun readDevicesFromFriends() {
        devices = ArrayList<Device>()
        LinhomeApplication.coreContext.core.getFriendListByName(local_devices_fl_name)?.friends?.forEach { friend ->
            val card = friend.vcard
            if (card != null && card.isValid()) {
                devices.add(Device(card, false))
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
                        if (devices.filter { it.address == device.address }.isEmpty())
                            devices.add(device)
                        Log.i("[DeviceStore] added remote device : ${friend.vcard?.asVcard4String()} ")
                    } else {
                        Log.e("[DeviceStore] received invalid or malformed vCard from remote : ${friend.vcard?.asVcard4String()} ")
                    }
                }
            }
        }

        devices.sortWith(compareBy({ it.name }, { it.address }))
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
        val core = LinhomeApplication.coreContext.core
        core.getFriendListByName(local_devices_fl_name)?.friends?.forEach {
            core.getFriendListByName(local_devices_fl_name)?.removeFriend(it)
        }

        devices.sortWith(compareBy({ it.name }, { it.address }))
        devices.forEach { device ->
            val friend = device.friend
            if (!device.isRemotelyProvisionned) {
                var localDevicesFriendList = core.getFriendListByName(local_devices_fl_name)
                if (localDevicesFriendList == null) {
                    Log.w("[DeviceStore] could not retrieve local friend list, creating a new one.")
                    localDevicesFriendList = core.createFriendList()
                    localDevicesFriendList.displayName = local_devices_fl_name
                    core.addFriendList(localDevicesFriendList)
                }
                val addResult = localDevicesFriendList.addFriend(friend)
                if (addResult != FriendList.Status.OK)
                    Log.e("[DeviceStore] unable to save device to local friend list. status : $addResult")
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
                readDevicesFromFriends()
            }
        }
    }

}