package org.lindoor.customisation

import org.lindoor.customisation.Customisation.deviceTypesConfig
import org.lindoor.ui.widgets.SpinnerItem
import java.util.*

object DeviceTypes {
    var deviceTypes: ArrayList<SpinnerItem> = ArrayList()
    lateinit var defaultType: String

    init {
        deviceTypesConfig.let { config ->
            config.sectionsNamesList.forEach {
                if (config.getBool(it, "default", false))
                    defaultType = it
                deviceTypes.add(
                    SpinnerItem(
                        config.getString(it, "textkey", "missing"),
                        config.getString(it, "icon", null),
                        it
                    )
                )
            }
        }
    }

    fun iconNameForDeviceType(typeKey: String, circle: Boolean = false): String? {
        return deviceTypesConfig.let { config ->
            config.getString(typeKey, "icon" + (if (circle) "_circle" else ""), null)
        }
    }

    fun typeNameForDeviceType(typeKey: String): String? {
        return deviceTypesConfig.let { config ->
            Texts.get(config.getString(typeKey, "textkey", deviceTypes.get(0).backingKey))
        }
    }

    fun supportsAudio(typeKey: String): Boolean {
        return deviceTypesConfig.let { config ->
            config.getBool(typeKey, "hasaudio", true)
        }
    }

    fun supportsVideo(typeKey: String): Boolean {
        return deviceTypesConfig.let { config ->
            config.getBool(typeKey, "hasvideo", false)
        }
    }

}
