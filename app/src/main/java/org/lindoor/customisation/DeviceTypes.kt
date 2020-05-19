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
}
