/*
 * Copyright (c) 2010-2020 Belledonne Communications SARL.
 *
 * This file is part of linhome-android
 * (see https://www.linhome.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.linhome.customisation

import org.linhome.customisation.Customisation.actionTypesConfig
import org.linhome.linphonecore.extensions.getString
import org.linhome.ui.widgets.SpinnerItem
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
