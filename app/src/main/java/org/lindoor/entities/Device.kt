package org.lindoor.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.lindoor.LindoorApplication
import org.lindoor.customisation.DeviceTypes
import org.linphone.core.Call
import org.linphone.core.CallParams
import java.io.File
import java.util.*

@Parcelize
data class Device(var id:String, var type:String?, var name:String, var address:String, var actionsMethodType:String?, var actions:ArrayList<Action>? ) :
    Parcelable {

    constructor( type:String?, name:String, address:String, actionsMethodType:String?, actions:ArrayList<Action>? ) : this(UUID.randomUUID().toString().replace("-", "").toLowerCase(),type,name,address,actionsMethodType,actions)

    var firstImageFileName: File?  = null
            get() {
                if (supportsVideo())
                    return File(LindoorApplication.instance.filesDir, "images/demo-image.jpg")
                else
                    return null
            }

    fun supportsVideo ():Boolean {
        return type?.let {
            DeviceTypes.supportsVideo(it)
        }?:false
    }

    fun supportsAudio ():Boolean {
        return type?.let {
            DeviceTypes.supportsAudio(it)
        }?:true
    }

    fun call(): Call? {
        val params : CallParams = LindoorApplication.coreContext.core.createCallParams(null)
        type?.also {
            params.enableVideo(DeviceTypes.supportsVideo(it))
            params.enableAudio(DeviceTypes.supportsAudio(it))
        }
        return LindoorApplication.coreContext.core.createAddress(address)?.let {
            LindoorApplication.coreContext.core.inviteAddressWithParams(it,params)
        } ?: null
    }

    fun typeName():String? {
        return type?.let {
            DeviceTypes.typeNameForDeviceType(it)
        } ?: null
    }



}