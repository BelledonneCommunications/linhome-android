package org.lindoor.customisation

import android.graphics.Bitmap
import org.lindoor.customisation.Customisation.deviceTypesConfig
import org.lindoor.entities.Device
import org.lindoor.ui.widgets.SpinnerItem
import java.util.*
import org.lindoor.linphonecore.extensions.getString


object DeviceTypes {
    var deviceTypes: ArrayList<SpinnerItem> = ArrayList()
    lateinit var defaultType: String
    var defaultTypeIconAsBitmap: Bitmap? = null

    init {
        deviceTypesConfig.let { config ->
            config.sectionsNamesList.forEach {
                if (config.getBool(it, "default", false))
                    defaultType = it
                deviceTypes.add(
                    SpinnerItem(
                        config.getString(it, "textkey", nonNullDefault = "missing"),
                        config.getString(it, "icon", null),
                        it
                    )
                )
                defaultTypeIconAsBitmap = Device.typeIconAsBitmap(defaultType)
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
            config.getString(typeKey, "textkey", deviceTypes.get(0).backingKey)?.let {
                Texts.get(it)
            }
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
