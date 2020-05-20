package org.lindoor.ui.devices.create

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import org.lindoor.BR
import org.lindoor.customisation.ActionTypes
import org.lindoor.customisation.ActionsMethodTypes
import org.lindoor.customisation.DeviceTypes
import org.lindoor.entities.Action
import org.lindoor.entities.Device
import org.lindoor.ui.validators.ValidatorFactory
import org.lindoor.ui.widgets.SpinnerItem
import org.lindoor.utils.databindings.ViewModelWithTools

class DeviceEditorViewModel : ViewModelWithTools() {

    var name: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> =  Pair(MutableLiveData<String>(),MutableLiveData<Boolean>(false))
    var address: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> =  Pair( MutableLiveData<String>(),MutableLiveData<Boolean>(false))

    var availableDeviceTypes: ArrayList<SpinnerItem> = ArrayList()
    var type: MutableLiveData<Int> = MutableLiveData<Int>(0)

    var availableMethodTypes: ArrayList<SpinnerItem> = ArrayList()
    private var actionMethods: MutableLiveData<Int> = MutableLiveData<Int>(0)

    var availableActionTypes: ArrayList<SpinnerItem> = ArrayList()
    val actionBindings = MutableLiveData<ArrayList<ViewDataBinding>>()
    val actionsViewModels = ArrayList<ActionViewModel>()


    var device: Device ? = null
        set(value) {
            field=value
            value?.also {
                name.first.value = it.name
                address.first.value = it.address
                actionMethods.value = ActionsMethodTypes.spinnerIndexByKey(it.actionsMethodType)
            }
        }


    var refreshActions = MutableLiveData(true)


    init {

        availableDeviceTypes.add(SpinnerItem("device_type_select_prompt"))
        availableDeviceTypes.addAll(DeviceTypes.spinnerItems)

        availableMethodTypes.add(SpinnerItem("action_method_prompt"))
        availableMethodTypes.addAll(ActionsMethodTypes.spinnerItems)

        availableActionTypes.add(SpinnerItem("action_prompt"))
        availableActionTypes.addAll(ActionTypes.spinnerItems)

        actionBindings.value = ArrayList()

    }

    fun valid(): Boolean {
        return name.second.value!! &&  address.second.value!!
    }

    fun saveDevice():Boolean {
        if (!valid())
            return false
        actionsViewModels.forEach {
            if (!it.valid())
                return false
        }
        return true
    }

    fun addAction(action: Action?, binding:ViewDataBinding) {
        val actionViewModel =  ActionViewModel(this,binding,actionsViewModels.size+1)
        binding.setVariable(BR.actionmodel, actionViewModel)
        binding.setVariable(BR.validators, ValidatorFactory.Companion)
        action?.also {
            actionViewModel.code.first.value = action.code
            actionViewModel.type.value = ActionTypes.spinnerIndexByKey(action.type)
        }
        actionsViewModels.add(actionViewModel)
        actionBindings.value?.add(binding)
        refreshActions.postValue(true)
    }


    fun removeActionViewModel(viewModel:ActionViewModel) {
        actionsViewModels.remove(viewModel)
        actionBindings.value?.remove(viewModel.binding)
        refreshActions.postValue(true)
    }

}
