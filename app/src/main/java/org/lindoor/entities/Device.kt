package org.lindoor.entities

import org.lindoor.LindoorApplication
import org.linphone.core.Config
import org.linphone.core.Factory
import java.io.File

class Device {

    companion object {
        private var devicesXml = File(LindoorApplication.instance.filesDir, "devices.xml")
        private var devicesConfig: Config

        init {
            if (!devicesXml.exists())
                devicesXml.createNewFile()
            devicesConfig = Factory.instance().createConfig(null)
            devicesConfig.loadFromXmlFile(devicesXml.absolutePath)
        }
    }

    fun add() {

    }

    fun remove() {

    }

    fun export()  {

    }

    fun import() {

    }

    fun getActions() {

    }

}