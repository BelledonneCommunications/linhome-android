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

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import org.linhome.BR
import org.linhome.customisation.ActionTypes
import org.linhome.customisation.ActionsMethodTypes
import org.linhome.customisation.DeviceTypes
import org.linhome.databinding.ItemActionEditBinding
import org.linhome.entities.Action
import org.linhome.entities.Device
import org.linhome.store.DeviceStore
import org.linhome.ui.validators.ValidatorFactory
import org.linhome.ui.widgets.LSpinnerListener
import org.linhome.ui.widgets.SpinnerItem
import org.linhome.ui.widgets.indexByBackingKey
import org.linhome.utils.databindings.ViewModelWithTools

class DeviceEditorViewModel : ViewModelWithTools() {

    val defaultDeviceType = 2 	// Video Intercom
    val defaulMethodType = 1 	// DTMF SIP INFO


    var name: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> =
        Pair(MutableLiveData<String>(), MutableLiveData<Boolean>(false))
    var address: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> =
        Pair(MutableLiveData<String>(), MutableLiveData<Boolean>(false))

    var availableDeviceTypes: ArrayList<SpinnerItem> = ArrayList()
    var deviceType: MutableLiveData<Int> = MutableLiveData<Int>(defaultDeviceType)

    var availableMethodTypes: ArrayList<SpinnerItem> = ArrayList()
    var actionsMethod: MutableLiveData<Int> = MutableLiveData<Int>(defaulMethodType)

    var availableActionTypes: ArrayList<SpinnerItem> = ArrayList()
    val actionBindings = MutableLiveData<ArrayList<ViewDataBinding>>()
    val actionsViewModels = ArrayList<DeviceEditorActionViewModel>()


    var device: Device? = null
        set(value) {
            field = value
            value?.also {
                name.first.value = it.name
                address.first.value = it.address
                deviceType.value = indexByBackingKey(it.type, availableDeviceTypes)
                actionsMethod.value = indexByBackingKey(it.actionsMethodType, availableMethodTypes)
            }
        }


    var refreshActions = MutableLiveData(true)

    val deviceTypeListener = object : LSpinnerListener {
        override fun onItemSelected(position: Int) {
            deviceType.value = position
        }
    }

    val actionsMethodListener = object : LSpinnerListener {
        override fun onItemSelected(position: Int) {
            actionsMethod.value = position
        }
    }

    init {

        availableDeviceTypes.add(SpinnerItem("device_type_select_prompt"))
        availableDeviceTypes.addAll(DeviceTypes.deviceTypes)

        availableMethodTypes.add(SpinnerItem("action_method_prompt"))
        availableMethodTypes.addAll(ActionsMethodTypes.spinnerItems)

        availableActionTypes.add(SpinnerItem("action_prompt"))
        availableActionTypes.addAll(ActionTypes.spinnerItems)

        actionBindings.value = ArrayList()

    }

    fun valid(): Boolean {
        return name.second.value!! && address.second.value!!
    }

    fun saveDevice(): Boolean {
        if (!valid())
            return false
        actionsViewModels.forEach {
            if (!it.valid())
                return false
        }

        if (device == null) {
            device = Device(
                if (deviceType.value == 0) null else availableDeviceTypes.get(deviceType.value!!).backingKey,
                name.first.value!!,
                if (address.first.value!!.startsWith("sip:") || address.first.value!!.startsWith("sips:")) address.first.value!! else "sip:${address.first.value}",
                if (actionsMethod.value == 0) null else availableMethodTypes.get(actionsMethod.value!!).backingKey,
                ArrayList(),
                false
            )
            actionsViewModels.forEach {
                if (it.notEmpty())
                    device?.actions?.add(
                        Action(
                            availableActionTypes.get(it.type.value!!).backingKey,
                            it.code.first.value!!
                        )
                    )
            }
            DeviceStore.persistDevice(device!!)
        } else {
            device?.also {
                it.type =
                    if (deviceType.value == 0) null else availableDeviceTypes.get(deviceType.value!!).backingKey
                it.name = name.first.value!!
                it.address =  if (address.first.value!!.startsWith("sip:") || address.first.value!!.startsWith("sips:")) address.first.value!! else "sip:${address.first.value}"
                it.actionsMethodType =
                    if (actionsMethod.value == 0) null else availableMethodTypes.get(actionsMethod.value!!).backingKey
                it.actions = ArrayList()
                actionsViewModels.forEach { action ->
                    if (action.notEmpty())
                        it.actions?.add(
                            Action(
                                availableActionTypes.get(action.type.value!!).backingKey,
                                action.code.first.value!!
                            )
                        )
                }
            }
            DeviceStore.saveLocalDevices()
        }

        return true
    }

    fun addAction(action: Action?, binding: ItemActionEditBinding) {
        val actionViewModel = DeviceEditorActionViewModel(this, binding, MutableLiveData(actionsViewModels.size + 1))
        action?.also {
            actionViewModel.code.first.value = action.code
            actionViewModel.type.value = indexByBackingKey(action.type, availableActionTypes)
        }
        actionsViewModels.add(actionViewModel)
        bindAction(binding,actionViewModel)
        refreshActions.postValue(true)
    }

    fun bindAction(binding: ItemActionEditBinding, actionViewModel:DeviceEditorActionViewModel) {
        binding.setVariable(BR.actionmodel, actionViewModel)
        binding.setVariable(BR.validators, ValidatorFactory.Companion)
        actionViewModel.binding = binding
        actionBindings.value?.add(binding)
        refreshActions.postValue(true)
    }

    fun removeActionViewModel(viewModel: DeviceEditorActionViewModel) {
        actionsViewModels.remove(viewModel)
        actionBindings.value?.remove(viewModel.binding)
        refreshActions.postValue(true)
    }

}
