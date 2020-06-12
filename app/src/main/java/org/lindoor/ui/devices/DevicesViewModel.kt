package org.lindoor.ui.devices

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.lindoor.entities.Device
import org.lindoor.store.DeviceStore

class DevicesViewModel : ViewModel() {
    val devices = MutableLiveData<ArrayList<Device>>().apply {
        value = DeviceStore.devices
    }
    val selectedDevice = MutableLiveData<Device>()

}
