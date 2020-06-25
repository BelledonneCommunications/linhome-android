package org.lindoor.ui.tabbar

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.lindoor.LindoorApplication
import org.lindoor.linphonecore.extensions.callLogsWithNonEmptyCallId
import org.lindoor.linphonecore.extensions.isNew
import org.lindoor.linphonecore.extensions.missedCount
import org.linphone.core.CallLog
import org.linphone.core.Core
import org.linphone.core.CoreListenerStub

class TabbarViewModel : ViewModel() {
    var unreadCount = MutableLiveData(0)

    private val coreListener = object : CoreListenerStub() {
        override fun onCallLogUpdated(lc: Core?, newcl: CallLog?) {
            updateUnreadCount()
        }
    }

    fun updateUnreadCount() {
        unreadCount.value =  LindoorApplication.coreContext.core.missedCount()
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