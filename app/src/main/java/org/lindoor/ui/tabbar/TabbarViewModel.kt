package org.lindoor.ui.tabbar

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.lindoor.LindoorApplication
import org.lindoor.linphonecore.callLogsWithCallId
import org.lindoor.linphonecore.isNew
import org.lindoor.store.HistoryEventStore
import org.lindoor.utils.cdlog
import org.linphone.core.Call
import org.linphone.core.CallLog
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub

class TabbarViewModel   : ViewModel() {
    var unreadCount =  MutableLiveData(0)

    private val coreListener = object : CoreListenerStub() {
        override fun onCallLogUpdated(lc: Core?, newcl: CallLog?) {
            updateUnreadCount()
        }
    }

    fun updateUnreadCount() {
        var count = 0
        LindoorApplication.coreContext.core.callLogsWithCallId().forEach{
            if (it.isNew())
                count++
        }
        unreadCount.value = count
    }

    init {
        LindoorApplication.coreContext.core.addListener(coreListener)
        updateUnreadCount()
    }

    override fun onCleared() {
        LindoorApplication.coreContext.core.removeListener(coreListener)
        super.onCleared()
    }

}