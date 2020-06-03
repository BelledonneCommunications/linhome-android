package org.lindoor.ui.history

import androidx.lifecycle.MutableLiveData
import org.lindoor.customisation.Texts
import org.lindoor.entities.Device
import org.lindoor.entities.HistoryEvent
import org.lindoor.store.DeviceStore
import org.lindoor.store.HistoryEventStore
import org.lindoor.utils.DateUtil
import org.lindoor.utils.databindings.ViewModelWithTools
import org.linphone.core.Call
import org.linphone.core.CallLog
import java.text.SimpleDateFormat
import java.util.*

class CallLogViewModel(val callLog: CallLog, val showDate:Boolean, val historyViewModel: HistoryViewModel, val device: Device? = DeviceStore.findDeviceByAddress(callLog.remoteAddress)) : ViewModelWithTools() {

    val viewMedia = MutableLiveData(false)

    var historyEvent:HistoryEvent? = callLog.callId?.let {
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


    fun callTypeIcon():String {
        return when (callLog.status) {
            Call.Status.Missed -> "icons/missed"
            Call.Status.Declined, Call.Status.DeclinedElsewhere -> "icons/declined"
            Call.Status.Aborted,Call.Status.EarlyAborted -> "icons/declined"
            Call.Status.Success,Call.Status.AcceptedElsewhere -> {
                if (callLog.dir == Call.Dir.Incoming)
                    "icons/accepted"
                else
                    "icons/called"
            }
        }
    }

    fun callTypeAndDate():String {
        val simpleDateFormat = SimpleDateFormat(historyTimePattern,Locale.getDefault())
        val callTime = simpleDateFormat.format(Date(callLog.startDate * 1000))
        val typeText = when (callLog.status) {
            Call.Status.Missed -> "history_list_call_type_missed"
            Call.Status.Declined, Call.Status.DeclinedElsewhere -> "history_list_call_type_declined"
            Call.Status.Aborted,Call.Status.EarlyAborted -> "history_list_call_type_aborted"
            Call.Status.Success,Call.Status.AcceptedElsewhere -> {
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

    fun dayText():String {
        return DateUtil.todayYesterdayRealDay(callLog.startDate/86400)
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

}