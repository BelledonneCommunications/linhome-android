package org.lindoor.managers

import org.lindoor.LindoorApplication
import org.lindoor.entities.Action
import org.lindoor.entities.Device
import org.linphone.core.Config
import org.linphone.core.Factory
import org.linphone.mediastream.Log
import java.io.File

object DeviceManager {

    private var devicesXml = File(LindoorApplication.instance.filesDir, "devices.xml")
    private var devicesConfig: Config

    var devices:ArrayList<Device>

    init {
        if (!devicesXml.exists())
            devicesXml.createNewFile()
        devicesConfig = Factory.instance().createConfig(null)
        devicesConfig.loadFromXmlFile(devicesXml.absolutePath)
        devices = readFromXml()
        Log.i("devices: "+ devicesConfig.dumpAsXml())
    }

    fun readFromXml():ArrayList<Device> {
        val result= ArrayList<Device>()
        devicesConfig.sectionsNamesList.forEach {
            val actions = ArrayList<Action>()
            devicesConfig.getString(it,"actions",null)?.also { // Actions are type1,code1|type2,code2 ...
                it.split("|").forEach {
                    actions.add(Action(it.split(",").first(),it.split(",").last()))
                }
            }
            result.add(Device(
                it,
                devicesConfig.getString(it,"type",null),
                devicesConfig.getString(it,"name","missing"),
                devicesConfig.getString(it,"address","missing"),
                devicesConfig.getString(it,"actions_method_type",null),
                actions))
        }
        return result
    }

    fun syncToXml() {
        devicesConfig.sectionsNamesList.forEach {
            devicesConfig.cleanSection(it)
        }
        devices.forEach { device ->
            devicesConfig.setString(device.id,"type",device.type)
            devicesConfig.setString(device.id,"name",device.name)
            devicesConfig.setString(device.id,"address",device.address)
            devicesConfig.setString(device.id,"actions_method_type",device.actionsMethodType)
            var actionString = String()
            device.actions?.forEach {
                val separator =  if (actionString.isEmpty()) "" else "|"
                actionString += separator+it.type+","+it.code
            }
            devicesConfig.setString(device.id,"actions",actionString)
        }
        devicesXml.writeText(devicesConfig.dumpAsXml())
        System.out.println("devices: "+ devicesConfig.dumpAsXml())

    }

    fun addDevice(device:Device) {
        devices.add(device)
        syncToXml()
    }

    fun removeDevice(device:Device) {
        devices.remove(device)
        syncToXml()
    }
}