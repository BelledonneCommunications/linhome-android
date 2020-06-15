package org.lindoor.ui.devices.edit

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import org.lindoor.BR
import org.lindoor.customisation.ActionTypes
import org.lindoor.customisation.ActionsMethodTypes
import org.lindoor.customisation.DeviceTypes
import org.lindoor.entities.Action
import org.lindoor.entities.Device
import org.lindoor.store.DeviceStore
import org.lindoor.ui.validators.ValidatorFactory
import org.lindoor.ui.widgets.LSpinnerListener
import org.lindoor.ui.widgets.SpinnerItem
import org.lindoor.ui.widgets.indexByBackingKey
import org.lindoor.utils.databindings.ViewModelWithTools

class DeviceEditorViewModel : ViewModelWithTools() {

    var name: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> =
        Pair(MutableLiveData<String>(), MutableLiveData<Boolean>(false))
    var address: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> =
        Pair(MutableLiveData<String>(), MutableLiveData<Boolean>(false))

    var availableDeviceTypes: ArrayList<SpinnerItem> = ArrayList()
    var type: MutableLiveData<Int> = MutableLiveData<Int>(0)

    var availableMethodTypes: ArrayList<SpinnerItem> = ArrayList()
    var actionsMethod: MutableLiveData<Int> = MutableLiveData<Int>(0)

    var availableActionTypes: ArrayList<SpinnerItem> = ArrayList()
    val actionBindings = MutableLiveData<ArrayList<ViewDataBinding>>()
    val actionsViewModels = ArrayList<DeviceEditorActionViewModel>()


    var device: Device? = null
        set(value) {
            field = value
            value?.also {
                name.first.value = it.name
                address.first.value = it.address
                type.value = indexByBackingKey(it.type, availableDeviceTypes)
                actionsMethod.value = indexByBackingKey(it.actionsMethodType, availableMethodTypes)
            }
        }


    var refreshActions = MutableLiveData(true)

    val deviceTypeListener = object : LSpinnerListener {
        override fun onItemSelected(position: Int) {
            type.value = position
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
                if (type.value == 0) null else availableDeviceTypes.get(type.value!!).backingKey,
                name.first.value!!,
                address.first.value!!,
                if (actionsMethod.value == 0) null else availableMethodTypes.get(actionsMethod.value!!).backingKey,
                ArrayList()
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
                    if (type.value == 0) null else availableDeviceTypes.get(type.value!!).backingKey
                it.name = name.first.value!!
                it.address = address.first.value!!
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
            DeviceStore.sync()
        }

        return true
    }

    fun addAction(action: Action?, binding: ViewDataBinding) {
        val actionViewModel = DeviceEditorActionViewModel(this, binding, actionsViewModels.size + 1)
        binding.setVariable(BR.actionmodel, actionViewModel)
        binding.setVariable(BR.validators, ValidatorFactory.Companion)
        action?.also {
            actionViewModel.code.first.value = action.code
            actionViewModel.type.value = indexByBackingKey(action.type, availableActionTypes)
        }
        actionsViewModels.add(actionViewModel)
        actionBindings.value?.add(binding)
        refreshActions.postValue(true)
    }


    fun removeActionViewModel(viewModel: DeviceEditorActionViewModel) {
        actionsViewModels.remove(viewModel)
        actionBindings.value?.remove(viewModel.binding)
        refreshActions.postValue(true)
    }

}
