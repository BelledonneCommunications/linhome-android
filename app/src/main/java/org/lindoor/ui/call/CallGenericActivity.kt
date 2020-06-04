package org.lindoor.ui.call

import android.os.Bundle
import android.view.View
import org.lindoor.GenericActivity
import org.lindoor.utils.DialogUtil

abstract class CallGenericActivity : GenericActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val decorView: View = window.decorView
        val uiOptions: Int =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.systemUiVisibility = uiOptions
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