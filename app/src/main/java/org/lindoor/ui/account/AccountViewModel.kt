package org.lindoor.ui.account

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.lindoor.entities.Device
import org.lindoor.store.DeviceStore

class AccountViewModel : ViewModel() {
    val devices = MutableLiveData<ArrayList<Device>>().apply {
        value = DeviceStore.devices
    }
}
