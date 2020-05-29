package org.lindoor.ui.call

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_call_incoming.view.*
import kotlinx.android.synthetic.main.chunk_call_device_icon_or_video.view.*
import org.lindoor.LindoorApplication.Companion.coreContext
import org.lindoor.R
import org.lindoor.databinding.ActivityCallIncomingBinding
import org.lindoor.databinding.ActivityCallOutgoingBinding
import org.linphone.core.Call


class CallOutgoingActivity : AppCompatActivity () {

    lateinit var binding : ActivityCallOutgoingBinding
    private lateinit var callViewModel: CallViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val decorView: View = window.decorView
        val uiOptions: Int =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.systemUiVisibility = uiOptions

        binding = DataBindingUtil.setContentView(this, R.layout.activity_call_outgoing) as ActivityCallOutgoingBinding
        binding.lifecycleOwner = this

        intent.getSerializableExtra("call")?.let {
            callViewModel = ViewModelProvider(this, CallViewModelFactory(it as Call))[CallViewModel::class.java]
            binding.callmodel = callViewModel
            callViewModel.callState.observe(this, Observer { callState ->
                when (callState) {
                    Call.State.OutgoingInit,Call.State.OutgoingProgress,Call.State.OutgoingRinging -> return@Observer
                    else -> finish()
                }
            })

        }  ?: finish()
    }

}
