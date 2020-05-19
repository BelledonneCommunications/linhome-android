package org.lindoor.customisation

import org.lindoor.customisation.Customisation.actionTypesConfig
import org.lindoor.ui.widgets.SpinnerItem
import java.util.*

object ActionTypes {
    var spinnerItems: ArrayList<SpinnerItem> = ArrayList()
    init {
        actionTypesConfig.let { config ->
            config.sectionsNamesList.forEach {
                spinnerItems.add(
                    SpinnerItem(config.getString(it,"textkey","missing"),config.getString(it,"icon",null),it)
                )
            }
        }
    }
    fun spinnerIndexByKey(key:String?):Int {
        ActionsMethodTypes.spinnerItems.forEachIndexed { index, spinnerItem ->
            if (spinnerItem.backingKey == key)
                return index
        }
        return -1
    }
}
