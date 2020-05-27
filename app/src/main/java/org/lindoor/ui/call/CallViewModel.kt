package org.lindoor.ui.call

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.lindoor.LindoorApplication
import org.lindoor.entities.Device
import org.lindoor.managers.DeviceManager
import org.linphone.core.*


class CallViewModelFactory(private val call: Call) :
    ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return CallViewModel(call) as T
    }
}

class CallViewModel(call:Call) : ViewModel() {
    val call: MutableLiveData<Call> = MutableLiveData(call)
    val device: MutableLiveData<Device?> = MutableLiveData(DeviceManager.findDeviceByAddress(call.remoteAddress))
    var callState: MutableLiveData<Call.State> = MutableLiveData(call.state)

    private var callListener = object : CallListenerStub() {
        override fun onStateChanged(call: Call?, cstate: Call.State?, message: String?) {
            callState.postValue(cstate)
        }
    }

    init {
        call.addListener(callListener)
    }


    override fun onCleared() {
        call.value?.removeListener(callListener)
        super.onCleared()
    }

}
