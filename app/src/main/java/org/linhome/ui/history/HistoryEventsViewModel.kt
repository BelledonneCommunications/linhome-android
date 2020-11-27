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

package org.linhome.ui.history

import androidx.lifecycle.MutableLiveData
import org.linhome.customisation.Texts
import org.linhome.entities.Device
import org.linhome.entities.HistoryEvent
import org.linhome.linphonecore.extensions.isNew
import org.linhome.store.DeviceStore
import org.linhome.store.HistoryEventStore
import org.linhome.utils.DateUtil
import org.linhome.utils.databindings.ViewModelWithTools
import org.linphone.core.Call
import org.linphone.core.CallLog
import java.text.SimpleDateFormat
import java.util.*

class HistoryEventsViewModel(
    val callLog: CallLog,
    val showDate: Boolean,
    val historyViewModel: HistoryViewModel,
    val device: Device? = DeviceStore.findDeviceByAddress(callLog.remoteAddress)
) : ViewModelWithTools() {

    val viewMedia = MutableLiveData(false)

    var historyEvent: HistoryEvent? = callLog.callId?.let {
        HistoryEventStore.findHistoryEventByCallId(it)
    }


    companion object {
        val historyTimePattern = "HH:mm:ss"
    }


    override fun onCleared() {
        historyEvent?.viewedByUser = true
        historyEvent?.let { HistoryEventStore.persistHistoryEvent(it) }
        super.onCleared()
    }


    fun callTypeIcon(): String {
        return when (callLog.status) {
            Call.Status.Missed -> "icons/missed"
            Call.Status.Declined, Call.Status.DeclinedElsewhere -> "icons/declined"
            Call.Status.Aborted, Call.Status.EarlyAborted -> "icons/declined"
            Call.Status.Success, Call.Status.AcceptedElsewhere -> {
                if (callLog.dir == Call.Dir.Incoming)
                    "icons/accepted"
                else
                    "icons/called"
            }
        }
    }

    fun callTypeAndDate(): String {
        val simpleDateFormat = SimpleDateFormat(historyTimePattern, Locale.getDefault())
        val callTime = simpleDateFormat.format(Date(callLog.startDate * 1000))
        val typeText = when (callLog.status) {
            Call.Status.Missed -> "history_list_call_type_missed"
            Call.Status.Declined, Call.Status.DeclinedElsewhere -> "history_list_call_type_declined"
            Call.Status.Aborted, Call.Status.EarlyAborted -> "history_list_call_type_aborted"
            Call.Status.Success, Call.Status.AcceptedElsewhere -> {
                if (callLog.dir == Call.Dir.Incoming)
                    "history_list_call_type_accepted"
                else
                    "history_list_call_type_called"
            }
        }
        return Texts.get(
            "history_list_call_date_type",
            Texts.get(typeText),
            callTime
        )
    }

    fun dayText(): String {
        return DateUtil.todayYesterdayRealDay(callLog.startDate / 86400)
    }

    fun toggleSelect() {
        if (historyViewModel.selectedForDeletion.value!!.contains(callLog.callId))
            historyViewModel.selectedForDeletion.value!!.remove(callLog.callId)
        else
            historyViewModel.selectedForDeletion.value!!.add(callLog.callId)
        historyViewModel.notifyDeleteSelectionListUpdated()
    }

    fun viewMedia() {
        viewMedia.value = true
    }

    fun isNew(): Boolean {
        return callLog.isNew()
    }

}