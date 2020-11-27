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

package org.linhome.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.linhome.customisation.ActionTypes

@Parcelize
data class Action(val type: String?, val code: String?) : Parcelable {

    fun typeName(): String? {
        return type?.let {
            ActionTypes.typeNameForActionType(it)
        }
    }

    fun iconName(): String? {
        return type?.let {
            ActionTypes.iconNameForActionType(it)
        }
    }

    fun actionText(): String? {
        return type?.let {
            ActionTypes.typeNameForActionType(it)
        }
    }

    fun actionTextWithCode(): String? {
        return type?.let {
            "${ActionTypes.typeNameForActionType(it)} - ${code}"
        }
    }


}