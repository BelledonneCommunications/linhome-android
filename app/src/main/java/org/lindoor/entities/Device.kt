package org.lindoor.entities

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.FileUtils
import android.os.Parcelable
import com.caverock.androidsvg.SVG
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.lindoor.LindoorApplication
import org.lindoor.customisation.DeviceTypes
import org.lindoor.store.StorageManager
import org.lindoor.utils.DialogUtil
import org.lindoor.utils.extensions.existsAndIsNotEmpty
import org.lindoor.utils.extensions.xDigitsUUID
import org.linphone.core.CallParams
import java.io.File
import java.io.FileInputStream
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

    var typeIconAsBitmap: Bitmap? = null

    constructor(
        type: String?,
        name: String,
        address: String,
        actionsMethodType: String?,
        actions: ArrayList<Action>?
    ) : this(
        xDigitsUUID(), type, name, address, actionsMethodType, actions
    )

    init {
        GlobalScope.launch() {
            typeIconAsBitmap = typeIconAsBitmap(type)
        }
    }


    val thumbNail: File
        get() {
            return File(StorageManager.devicesThumnailPath, "${id}.jpg")
        }


    fun supportsVideo(): Boolean {
        return type?.let {
            DeviceTypes.supportsVideo(it)
        } ?: false
    }

    fun call() {
        val params: CallParams? = LindoorApplication.coreContext.core.createCallParams(null)
        type?.also {
            params?.enableVideo(DeviceTypes.supportsVideo(it))
            params?.enableAudio(DeviceTypes.supportsAudio(it))
        }
        val historyEvent = HistoryEvent()
        params?.recordFile = historyEvent.mediaFileName
        LindoorApplication.coreContext.core.createAddress(address)?.let {
            val call = params?.let { parameters ->
                LindoorApplication.coreContext.core.inviteAddressWithParams(it,
                    parameters
                )
            }
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

    companion object {
        fun typeIconAsBitmap(type: String?): Bitmap? {
            return type?.let {
                val svgFile = File(
                    LindoorApplication.instance.filesDir,
                    "images/${DeviceTypes.iconNameForDeviceType(it)}.svg"
                )
                val targetStream = FileInputStream(svgFile)
                val svg = SVG.getFromInputStream(targetStream)
                if (svg.documentWidth != -1f) {
                    val newBM = Bitmap.createBitmap(
                        Math.ceil(svg.documentWidth.toDouble()).toInt(),
                        Math.ceil(svg.documentHeight.toDouble()).toInt(),
                        Bitmap.Config.ARGB_8888
                    )
                    val bmcanvas = Canvas(newBM)
                    bmcanvas.drawRGB(255, 255, 255)
                    svg.renderToCanvas(bmcanvas)
                    newBM
                } else
                    null
            }
        }
    }

    fun hasThumbNail(): Boolean {
        return thumbNail.existsAndIsNotEmpty()
    }

}