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

import org.linhome.LinhomeApplication
import org.linhome.entities.Action
import org.linhome.entities.Device
import org.linhome.store.StorageManager.devicesXml
import org.linphone.core.Address
import org.linphone.core.Config
import org.linphone.core.Factory
import org.linhome.linphonecore.extensions.getString

object DeviceStore {

    private var devicesConfig: Config

    val vcard_device_type_header = "X-LINPHONE-ACCOUNT-TYPE"
    val vcard_actions_list_header = "X-LINPHONE-ACCOUNT-ACTION"
    val vcard_action_method_type_header = "X-LINPHONE-ACCOUNT-DTMF-PROTOCOL"

    var devices: ArrayList<Device>

    init {
        if (!devicesXml.exists())
            devicesXml.createNewFile()
        devicesConfig = Factory.instance().createConfig(null)
        devicesConfig.loadFromXmlFile(devicesXml.absolutePath)
        devices = readFromXml()
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
                    actions
                )
            )
        }
        result.sortWith(compareBy({ it.name }, { it.address }))
        return result
    }

    fun sync() {
        devicesConfig.sectionsNamesList.forEach {
            devicesConfig.cleanSection(it)
        }
        devices.sortWith(compareBy({ it.name }, { it.address }))
        devices.forEach { device ->
            devicesConfig.setString(device.id, "type", device.type)
            devicesConfig.setString(device.id, "name", device.name)
            devicesConfig.setString(device.id, "address", device.address)
            devicesConfig.setString(device.id, "actions_method_type", device.actionsMethodType)
            var actionString = String()
            device.actions?.forEach {
                val separator = if (actionString.isEmpty()) "" else "|"
                actionString += separator + it.type + "," + it.code
            }
            devicesConfig.setString(device.id, "actions", actionString)
        }
        devicesXml.writeText(devicesConfig.dumpAsXml())

    }

    fun persistDevice(device: Device) {
        devices.add(device)
        sync()
    }

    fun removeDevice(device: Device) {
        device.thumbNail.also {
            if (it.exists())
                it.delete()
        }
        devices.remove(device)
        sync()
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

}