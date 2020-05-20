package org.lindoor.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.lindoor.managers.DeviceManager

@Parcelize
data class Device(var id:String?, var type:String?, var name:String?, var address:String?, var actionsMethodType:String?, var actions:ArrayList<Action>? ) :
    Parcelable {

    fun remove() {
        DeviceManager.removeDevice(this)
    }

}