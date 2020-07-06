package org.lindoor.ui.call

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.SystemClock
import android.view.MotionEvent
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_call_in_progress.view.*
import kotlinx.android.synthetic.main.chunk_call_device_icon_or_video.view.*
import org.lindoor.LindoorApplication.Companion.coreContext
import org.lindoor.R
import org.lindoor.databinding.ActivityCallInProgressBinding
import org.lindoor.utils.DialogUtil
import org.lindoor.utils.extensions.toogleVisible
import org.linphone.core.Call
import org.linphone.core.tools.Log
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnNeverAskAgain
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class CallInProgressActivity : CallGenericActivity() {

    lateinit var binding: ActivityCallInProgressBinding
    lateinit var callViewModel: CallViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_call_in_progress
        ) as ActivityCallInProgressBinding
        binding.lifecycleOwner = this

        call?.also {
            callViewModel =
                ViewModelProvider(this, CallViewModelFactory(it))[CallViewModel::class.java]
            binding.callmodel = callViewModel
            binding.callTimer.base = SystemClock.elapsedRealtime() - (1000 * it.duration)
            binding.callTimer.start()
            callViewModel.callState.observe(this, Observer { callState ->
                when (callState) {
                    Call.State.End, Call.State.Released -> finish()
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
            binding.root.videofullscreen.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    binding.root.actions.toogleVisible()
                    binding.root.controls.toogleVisible()
                    binding.root.timer.toogleVisible()
                }
                true
            }
            startWithPermissionCheck()
        } ?: finish()
    }

    override fun onResume() {
        super.onResume()
        coreContext.core.nativeVideoWindowId =
            if (callViewModel.videoFullScreen.value!!) binding.root.videofullscreen else binding.root.videocollapsed

        if (coreContext.core.callsNb == 0) {
            Log.w("[Call Activity] Resuming but no call found...")
            finish()
        } else {
            coreContext.removeCallOverlay()
        }

    }

    override fun onPause() {
        coreContext.core.nativeVideoWindowId = null
        if (coreContext.core.callsNb > 0) {
            coreContext.createCallOverlay()
        }
        super.onPause()
    }

    @SuppressLint("NeedOnRequestPermissionsResult")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (permissions.size > 0)
            onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.RECORD_AUDIO)
    fun start() {
    }

    @OnPermissionDenied(Manifest.permission.RECORD_AUDIO)
    fun onRecordAudioDenied() {
        DialogUtil.error("record_audio_permission_denied")
    }

    @OnNeverAskAgain(Manifest.permission.RECORD_AUDIO)
    fun onRecordAudioDeniedNeverAskAgain() {
        DialogUtil.error("record_audio_permission_denied_dont_ask_again")
    }

}
