/*
 * Copyright (c) 2010-2020 Belledonne Communications SARL.
 *
 * This file is part of linhome-android
 * (see https://www.linhome.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.linhome.entities

import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Parcelable
import com.caverock.androidsvg.SVG
import kotlinx.android.parcel.Parcelize
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.linhome.LinhomeApplication
import org.linhome.customisation.DeviceTypes
import org.linhome.store.StorageManager
import org.linhome.utils.DialogUtil
import org.linhome.utils.extensions.existsAndIsNotEmpty
import org.linhome.utils.extensions.md5
import org.linhome.utils.extensions.xDigitsUUID
import org.linphone.core.CallParams
import org.linphone.core.Friend
import org.linphone.core.Vcard
import org.linphone.core.tools.Log
import java.io.File
import java.io.FileInputStream


@Parcelize
data class Device(
    var id: String,
    var type: String?,
    var name: String,
    var address: String,
    var actionsMethodType: String?,
    var actions: ArrayList<Action>?,
    var isRemotelyProvisionned: Boolean
) :
    Parcelable {

    val friend: Friend
        get() =
            LinhomeApplication.coreContext.core.createFriend().let { friend ->
                friend.createVcard(name)
                friend.vcard?.addExtendedProperty(vcard_device_type_header, type!!)
                friend.vcard?.addSipAddress(address)
                friend.vcard?.addExtendedProperty(vcard_action_method_type_header,
                    deviceActionMethodsTovCardActionMethods().get(actionsMethodType!!)!!)
                actions?.forEach { it ->
                    friend.vcard?.addExtendedProperty(vcard_actions_list_header,it.type!! + ";" + it.code!!)
                }
                Log.i("[Device] created vCard for device: $name ${friend.vcard?.asVcard4String()}")
                friend
            }

    var typeIconAsBitmap: Bitmap? = null

    constructor(
        type: String?,
        name: String,
        address: String,
        actionsMethodType: String?,
        actions: ArrayList<Action>?,
        isRemotelyProvisionned: Boolean
    ) : this(
        xDigitsUUID(), type, name, address, actionsMethodType, actions, isRemotelyProvisionned
    )

    constructor (card:Vcard, isRemotelyProvisionned:Boolean) : this(card.sipAddresses.component1()?.asStringUriOnly()!!.md5(),
        card.getExtendedPropertiesValuesByName(vcard_device_type_header).component1(),
        card.fullName!!,
        card.sipAddresses.component1()?.asStringUriOnly()!!,
        vCardActionMethodsToDeviceMethods.get(card.getExtendedPropertiesValuesByName(vcard_action_method_type_header).component1()),
        ArrayList(),
        isRemotelyProvisionned)
    {
        card.getExtendedPropertiesValuesByName(vcard_actions_list_header).forEach { action ->
            val components = action.split(";")
            if (components.size != 2)  {
                Log.e("Unable to create action from VCard $action")
            } else {
                actions?.add(Action(components.component1(), components.component2()))
            }
        }
    }

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

        if (LinhomeApplication.coreContext.core.callsNb > 0) {
            return
        }
        val params: CallParams? = LinhomeApplication.coreContext.core.createCallParams(null)
        type?.also {
            params?.isVideoEnabled = DeviceTypes.supportsVideo(it)
            params?.isAudioEnabled = DeviceTypes.supportsAudio(it)
        }
        val historyEvent = HistoryEvent()
        params?.recordFile = historyEvent.mediaFileName
        LinhomeApplication.coreContext.core.createAddress(address)?.let {
            val call = params?.let { parameters ->
                LinhomeApplication.coreContext.core.useRfc2833ForDtmf = actionsMethodType == "method_dtmf_rfc_4733"
                LinhomeApplication.coreContext.core.useInfoForDtmf = actionsMethodType == "method_dtmf_sip_info"
                LinhomeApplication.coreContext.core.inviteAddressWithParams(it,
                    parameters
                )
            }
            if (call != null) {
                call.isCameraEnabled = false
                call.callLog.userData =
                    historyEvent // Retrieved in CallViewModel and bound with call ID when available
            } else
                DialogUtil.error("unable_to_call_device")
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

        const val vcard_device_type_header = "X-LINPHONE-ACCOUNT-TYPE"
        const val vcard_actions_list_header = "X-LINPHONE-ACCOUNT-ACTION"
        const val vcard_action_method_type_header = "X-LINPHONE-ACCOUNT-DTMF-PROTOCOL"
        val vCardActionMethodsToDeviceMethods = mapOf( "sipinfo" to "method_dtmf_sip_info","rfc2833" to "method_dtmf_rfc_4733","sipmessage" to "method_sip_message") // Server side method names to local app names
        fun deviceActionMethodsTovCardActionMethods () : HashMap<String,String> {
            var result = hashMapOf<String,String>()
            vCardActionMethodsToDeviceMethods.forEach {
                result.put(it.value,it.key)
            }
            return result
        }

        fun typeIconAsBitmap(type: String?): Bitmap? {
            return type?.let {
                val svgFile = File(
                    LinhomeApplication.instance.filesDir,
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