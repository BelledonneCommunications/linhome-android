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
                    SpinnerItem(
                        config.getString(it, "textkey", "missing"),
                        config.getString(it, "icon", null),
                        it
                    )
                )
            }
        }
    }

    fun typeNameForActionType(typeKey: String): String {
        return actionTypesConfig.let { config ->
            Texts.get(config.getString(typeKey, "textkey", null))
        }
    }

    fun iconNameForActionType(typeKey: String): String {
        return actionTypesConfig.let { config ->
            config.getString(typeKey, "icon", null)
        }
    }

}
