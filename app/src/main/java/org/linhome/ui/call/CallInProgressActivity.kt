/*
 * Copyright (c) 2010-2020 Belledonne Communications SARL.
 *
 * This file is part of linhome-android
 * (see https://www.linhome.org).
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.linhome.ui.call

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.activity_call_in_progress.view.*
import kotlinx.android.synthetic.main.chunk_call_device_icon_or_video.view.*
import org.linhome.LinhomeApplication.Companion.coreContext
import org.linhome.R
import org.linhome.databinding.ActivityCallInProgressBinding
import org.linhome.utils.DialogUtil
import org.linhome.utils.extensions.forceVisible
import org.linhome.utils.extensions.toogleVisible
import org.linphone.compatibility.Compatibility
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

        Compatibility.setShowWhenLocked(this, true)
        Compatibility.setTurnScreenOn(this, true)

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
                    binding.root.controls?.toogleVisible()
                    binding.root.timer.toogleVisible()
                }
                true
            }
            callViewModel.videoFullScreen.observe(this, Observer { full ->
                if (!full) {
                    binding.root.actions.forceVisible()
                    binding.root.controls?.forceVisible()
                    binding.root.timer.forceVisible()
                }
            })
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
        if (coreContext.core.callsNb > 0) {
            coreContext.createCallOverlay()
        }
	coreContext.core.nativeVideoWindowId = null
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
