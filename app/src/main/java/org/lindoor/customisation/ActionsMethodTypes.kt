package org.lindoor.customisation

import org.lindoor.customisation.Customisation.actionsMethodTypesConfig
import org.lindoor.ui.widgets.SpinnerItem
import java.util.*
import org.lindoor.linphonecore.extensions.getString


object ActionsMethodTypes {
    var spinnerItems: ArrayList<SpinnerItem> = ArrayList()

    init {
        actionsMethodTypesConfig.let { config ->
            config.sectionsNamesList.forEach {
                spinnerItems.add(
                    SpinnerItem(config.getString(it, "textkey", nonNullDefault = "missing"), null, it)
                )
            }
        }
    }

}
