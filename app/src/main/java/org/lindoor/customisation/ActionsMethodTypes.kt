package org.lindoor.customisation

import org.lindoor.customisation.Customisation.actionsMethodTypesConfig
import org.lindoor.ui.widgets.SpinnerItem
import java.util.*

object ActionsMethodTypes {
    var spinnerItems: ArrayList<SpinnerItem> = ArrayList()
    init {
        actionsMethodTypesConfig.let { config ->
            config.sectionsNamesList.forEach {
                spinnerItems.add(
                    SpinnerItem(config.getString(it,"textkey","missing"),null,it)
                )
            }
        }
    }
    fun spinnerIndexByKey(key:String?):Int {
        spinnerItems.forEachIndexed { index, spinnerItem ->
            if (spinnerItem.backingKey == key)
                return index
        }
        return -1
    }

}
