package org.lindoor.ui.call

import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import org.lindoor.R
import org.lindoor.customisation.Theme
import org.lindoor.databinding.ActivityCallOutgoingBinding
import org.lindoor.utils.DialogUtil
import org.linphone.core.Call

abstract class CallGenericActivity : AppCompatActivity() {


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