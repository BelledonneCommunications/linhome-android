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

package org.linhome.linphonecore.extensions

import org.linhome.customisation.ActionTypes
import org.linhome.customisation.ActionsMethodTypes
import org.linhome.customisation.DeviceTypes
import org.linhome.entities.Device
import org.linphone.core.Vcard
import org.linphone.core.tools.Log


fun Vcard.isValid():  Boolean {
   val card = this
    val validType =  card.getExtendedPropertiesValuesByName(Device.vcard_device_type_header)
        .component1()
        ?.let { typeKey ->
            DeviceTypes.deviceTypeSupported(typeKey)
        } ?:  false
    if (!validType) {
        Log.e("[Device] vCard validation : invalid type ${
            card.getExtendedPropertiesValuesByName(Device.vcard_device_type_header).component1()
        }")
        return false
    }

    val validDtmf = card.getExtendedPropertiesValuesByName(Device.vcard_action_method_type_header)
        .component1()?.let { remoteDtmfMethod ->
            Device.vCardActionMethodsToDeviceMethods.get(remoteDtmfMethod)?.let { localDtmfMethod ->
                ActionsMethodTypes.methodTypeIsSupported(localDtmfMethod)
            }?:false
        }?:false
    if (!validDtmf) {
        Log.e("[Device] vCard validation : invalid dtmf sending method ${
            card.getExtendedPropertiesValuesByName(Device.vcard_action_method_type_header)
                .component1()
        }")
        return false
    }

    var validActions = true
    card.getExtendedPropertiesValuesByName(Device.vcard_actions_list_header).forEach { action ->
        val components = action.split(";")
        if (components.size == 2) {
            validActions = validActions && ActionTypes.isValid(components.component1())
        } else {
            validActions = false
        }
        if (!validActions) {
            Log.e("[Device] vCard validation : invalid action $action")
        }
    }

    return validActions

}



