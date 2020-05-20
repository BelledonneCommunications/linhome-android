package org.lindoor.ui.devices.create

import androidx.databinding.ViewDataBinding
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ActionViewModel (val owningViewModel: DeviceEditorViewModel, val binding: ViewDataBinding, val displayIndex:Int): ViewModel() {
    var type: MutableLiveData<Int> = MutableLiveData<Int>(0)
    var code: Pair<MutableLiveData<String>, MutableLiveData<Boolean>> =  Pair(MutableLiveData<String>(),MutableLiveData<Boolean>(false))

    fun valid():Boolean {
        return (type.value==0 && code.first.value.isNullOrEmpty()) || (type.value!=0 && code.second.value!!)
    }
    fun removeAction() {
        owningViewModel.removeActionViewModel(this)
    }


}
