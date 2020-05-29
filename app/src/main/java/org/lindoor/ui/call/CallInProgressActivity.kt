package org.lindoor.ui.call

import android.os.Bundle
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_call_in_progress.view.*
import kotlinx.android.synthetic.main.chunk_call_device_icon_or_video.view.*
import org.lindoor.LindoorApplication.Companion.coreContext
import org.lindoor.R
import org.lindoor.databinding.ActivityCallInProgressBinding
import org.lindoor.utils.toogleVisible
import org.linphone.core.Call


class CallInProgressActivity : AppCompatActivity () {

    lateinit var binding : ActivityCallInProgressBinding
    lateinit var callViewModel: CallViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val decorView: View = window.decorView
        val uiOptions: Int =
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        decorView.setSystemUiVisibility(uiOptions)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_call_in_progress) as ActivityCallInProgressBinding
        binding.lifecycleOwner = this

        intent.getSerializableExtra("call")?.let {
            callViewModel = ViewModelProvider(this, CallViewModelFactory(it as Call))[CallViewModel::class.java]
            binding.callmodel = callViewModel
            binding.callTimer.base = SystemClock.elapsedRealtime() - (1000 * it.duration)
            binding.callTimer.start()
            callViewModel.callState.observe(this, Observer { callState ->
                when (callState) {
                    Call.State.End,Call.State.Released -> finish()
                }
            })
            binding.root.videotogglecollapsed.setOnClickListener {
                coreContext.core.nativeVideoWindowId = binding.root.videofullscreen
                callViewModel.toggleVideoFullScreen()
            }
            binding.root.videotogglefullscreen.setOnClickListener {
                coreContext.core.nativeVideoWindowId = binding.root.videocollapsed
                callViewModel.toggleVideoFullScreen()
            }
            binding.root.videofullscreen.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    binding.root.actions.toogleVisible()
                    binding.root.controls.toogleVisible()
                    binding.root.timer.toogleVisible()
                }
                true
            }

        }  ?: finish()
    }

    override fun onResume() {
        super.onResume()
        coreContext.core.nativeVideoWindowId = if (callViewModel.videoFullScreen.value!!) binding.root.videofullscreen else binding.root.videocollapsed
    }

    override fun onPause() {
        coreContext.core.nativeVideoWindowId = null
        super.onPause()
    }

}
