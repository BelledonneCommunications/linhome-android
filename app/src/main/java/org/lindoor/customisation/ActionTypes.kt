package org.lindoor.customisation

import org.lindoor.customisation.Customisation.actionTypesConfig
import org.lindoor.linphonecore.extensions.getString
import org.lindoor.ui.widgets.SpinnerItem
import java.util.*

object ActionTypes {
    var spinnerItems: ArrayList<SpinnerItem> = ArrayList()

    init {
        actionTypesConfig.let { config ->
            config.sectionsNamesList.forEach {
                spinnerItems.add(
                    SpinnerItem(
                        config.getString(it, "textkey", nonNullDefault = "missing"),
                        config.getString(it, "icon", null),
                        it
                    )
                )
            }
        }
    }

    fun typeNameForActionType(typeKey: String): String {
        return actionTypesConfig.let { config ->
            Texts.get(config.getString(typeKey, "textkey", nonNullDefault = typeKey))
        }
    }

    fun iconNameForActionType(typeKey: String): String {
        return actionTypesConfig.let { config ->
            config.getString(typeKey, "icon", nonNullDefault = typeKey )
        }
    }

}
