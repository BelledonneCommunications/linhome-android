package org.lindoor.ui.call

import android.os.Bundle
import android.view.View
import org.lindoor.GenericActivity
import org.lindoor.utils.DialogUtil
import org.linphone.core.Call
import org.lindoor.LindoorApplication.Companion.coreContext


abstract class CallGenericActivity : GenericActivity() {


    var call: Call? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val decorView: View = window.decorView
        val uiOptions: Int =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.systemUiVisibility = uiOptions

        call = intent.getStringExtra("callId")?.let { callId ->
            coreContext.core.calls.filter {  it.callLog.callId.equals(callId) }.firstOrNull()
        }

    }


    override fun onResume() {
        super.onResume()
        DialogUtil.context = this
    }

    override fun onPause() {
        if (DialogUtil.context == this)
            DialogUtil.context = null
        super.onPause()
    }

}