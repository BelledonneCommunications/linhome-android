package org.lindoor.store

import org.lindoor.LindoorApplication
import org.lindoor.entities.Action
import org.lindoor.entities.Device
import org.lindoor.store.StorageManager.devicesXml
import org.linphone.core.Address
import org.linphone.core.Config
import org.linphone.core.Factory
import org.lindoor.linphonecore.extensions.getString

object DeviceStore {

    private var devicesConfig: Config

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
            LindoorApplication.coreContext.core.createAddress(addressString)
                ?.let { addressAddress ->
                    return findDeviceByAddress(addressAddress)
                }
        }
    }

}