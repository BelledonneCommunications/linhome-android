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

package org.linhome.ui.devices.edit

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.linhome.databinding.ItemActionEditBinding
import org.linhome.ui.widgets.LSpinnerListener

class DeviceEditorActionViewModel(
    val owningViewModel: DeviceEditorViewModel,
    var binding: ItemActionEditBinding,
    val displayIndex: MutableLiveData<Int> = MutableLiveData(0)
) : ViewModel() {
    var type: MutableLiveData<Int> = MutableLiveData<Int>(0)
    var code: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> =
        Pair(MutableLiveData<String>(), MutableLiveData<Boolean>(false))

    fun valid(): Boolean {
        return (type.value == 0 && code.first.value.isNullOrEmpty()) || (type.value != 0 && code.second.value!!)
    }

    fun removeAction() {
        owningViewModel.removeActionViewModel(this)
    }


    val actionTypeListener = object : LSpinnerListener {
        override fun onItemSelected(position: Int) {
            type.value = position
        }
    }

    fun notEmpty(): Boolean {
        return type.value != 0 && !code.first.value.isNullOrEmpty()
    }

}
