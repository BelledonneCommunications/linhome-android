package org.lindoor.ui.devices

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.lindoor.entities.Device
import org.lindoor.managers.DeviceManager

class DevicesViewModel : ViewModel() {
    val devices = MutableLiveData<ArrayList<Device>>().apply {
        value = DeviceManager.devices
    }
}
