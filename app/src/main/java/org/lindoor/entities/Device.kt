package org.lindoor.entities

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import org.lindoor.LindoorApplication
import org.lindoor.customisation.DeviceTypes
import org.lindoor.store.StorageManager
import org.lindoor.utils.DialogUtil
import org.lindoor.utils.extensions.existsAndIsNotEmpty
import org.lindoor.utils.extensions.xDigitsUUID
import org.linphone.core.CallParams
import java.io.File
import java.util.*


@Parcelize
data class Device(
    var id: String,
    var type: String?,
    var name: String,
    var address: String,
    var actionsMethodType: String?,
    var actions: ArrayList<Action>?
) :
    Parcelable {

    constructor(
        type: String?,
        name: String,
        address: String,
        actionsMethodType: String?,
        actions: ArrayList<Action>?
    ) : this(
        xDigitsUUID(), type, name, address, actionsMethodType, actions
    )


    val thumbNail: File
        get() {
            return File(StorageManager.devicesThumnailPath, "${id}.jpg")
        }


    fun supportsVideo(): Boolean {
        return type?.let {
            DeviceTypes.supportsVideo(it)
        } ?: false
    }

    fun supportsAudio(): Boolean {
        return type?.let {
            DeviceTypes.supportsAudio(it)
        } ?: true
    }

    fun call() {
        val params: CallParams = LindoorApplication.coreContext.core.createCallParams(null)
        type?.also {
            params.enableVideo(DeviceTypes.supportsVideo(it))
            params.enableAudio(DeviceTypes.supportsAudio(it))
        }
        val historyEvent = HistoryEvent()
        params.recordFile = historyEvent.mediaFileName
        LindoorApplication.coreContext.core.createAddress(address)?.let {
            val call = LindoorApplication.coreContext.core.inviteAddressWithParams(it, params)
            if (call != null)
                call.callLog.userData =
                    historyEvent // Retrieved in CallViewModel and bound with call ID when available
            else
                DialogUtil.toast("unable_to_call_device")
        }
    }

    fun typeName(): String? {
        return type?.let {
            DeviceTypes.typeNameForDeviceType(it)
        }
    }

    fun typeIcon(): String? {
        return type?.let {
            DeviceTypes.iconNameForDeviceType(it)
        }
    }

    fun hasThumbNail(): Boolean {
        return thumbNail.existsAndIsNotEmpty()
    }


}