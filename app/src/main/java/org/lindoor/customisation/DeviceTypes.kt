package org.lindoor.customisation

import org.lindoor.customisation.Customisation.deviceTypesConfig
import org.lindoor.ui.widgets.SpinnerItem
import java.util.*

object DeviceTypes {
    var spinnerItems: ArrayList<SpinnerItem> = ArrayList()
    init {
        deviceTypesConfig.let { config ->
            config.sectionsNamesList.forEach {
                spinnerItems.add(
                    SpinnerItem(config.getString(it,"textkey","missing"),config.getString(it,"icon",null),it)
                )
            }
        }
    }

    fun iconNameForDeviceType(typeKey:String, circle:Boolean = false):String? {
        return deviceTypesConfig.let { config ->
             config.getString(typeKey,"icon"+(if (circle) "_circle" else ""),null)
        }
    }

    fun typeNameForDeviceType(typeKey:String):String? {
        return deviceTypesConfig.let { config ->
            Texts.get(config.getString(typeKey,"textkey",null))
        }
    }

    fun supportsAudio(typeKey:String):Boolean  {
        return deviceTypesConfig.let { config ->
            config.getBool(typeKey,"hasaudio",true)
        }
    }

    fun supportsVideo(typeKey:String):Boolean  {
        return deviceTypesConfig.let { config ->
            config.getBool(typeKey,"hasvideo",false)
        }
    }

    fun detaultType():String? {
        return deviceTypesConfig.let { config ->
            config.getString("default","type",null)
        }
    }

}
