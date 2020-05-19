package org.lindoor.entities

import android.content.Context
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.lindoor.LindoorApplication
import org.lindoor.managers.DeviceManager
import org.linphone.core.Config
import org.linphone.core.Factory
import java.io.File

@Parcelize
data class Device(var id:String?, var type:String?, var name:String?, var address:String?, var actionsMethodType:String?, var actions:ArrayList<Action>? ) :
    Parcelable {

    fun remove() {
        DeviceManager.removeDevice(this)
    }

}