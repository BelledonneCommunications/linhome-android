package org.lindoor.ui.call.incoming

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.navigation.navArgs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.lindoor.R
import org.lindoor.customisation.Theme
import org.lindoor.databinding.ActivityCallIncomingBinding
import org.lindoor.databinding.ActivityMainBinding
import org.lindoor.databinding.ActivitySplashBinding
import org.lindoor.ui.call.CallViewModel
import org.lindoor.ui.call.CallViewModelFactory
import org.lindoor.ui.devices.edit.DeviceEditorFragmentArgs
import org.lindoor.ui.toolbar.ToolbarViewModel
import org.linphone.core.Call

class CallIncomingActivity : AppCompatActivity () {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val decorView: View = window.decorView
        val uiOptions: Int =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.setSystemUiVisibility(uiOptions)

        val binding = DataBindingUtil.setContentView(this, R.layout.activity_call_incoming) as ActivityCallIncomingBinding
        binding.lifecycleOwner = this

        intent.getSerializableExtra("call")?.let {
            val callViewModel = ViewModelProvider(this, CallViewModelFactory(it as Call))[CallViewModel::class.java]
            binding.callmodel = callViewModel
            callViewModel.callState?.observe(this, Observer { callState ->
                when (callState) {
                    Call.State.End -> finish()
                    Call.State.IncomingEarlyMedia -> finish()
                    Call.State.Connected -> finish()
                }
            })
        }  ?: finish()

    }

}
