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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.linhome.LinhomeApplication.Companion.coreContext
import org.linhome.R
import org.linhome.compatibility.Compatibility
import org.linhome.databinding.ActivityCallInProgressBinding
import org.linhome.utils.DialogUtil
import org.linhome.utils.extensions.forceVisible
import org.linhome.utils.extensions.toogleVisible
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

    @SuppressLint("ClickableViewAccessibility")
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
                    else -> {}
                }
            })
            binding.chunkCallDeviceIconOrVideo?.videotogglecollapsed?.setOnClickListener {
                coreContext.core.nativeVideoWindowId = binding.videofullscreen
                callViewModel.toggleVideoFullScreen()
            }
            binding.videotogglefullscreen.setOnClickListener {
                coreContext.core.nativeVideoWindowId = binding.chunkCallDeviceIconOrVideo?.videocollapsed
                callViewModel.toggleVideoFullScreen()
            }
            binding.videofullscreen.setOnTouchListener { _, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    binding.actions.toogleVisible()
                    binding.controls?.toogleVisible()
                    binding.timer.toogleVisible()
                }
                true
            }
            callViewModel.videoFullScreen.observe(this, Observer { full ->
                if (!full) {
                    binding.actions.forceVisible()
                    binding.controls?.forceVisible()
                    binding.timer.forceVisible()
                }
            })
            callViewModel.videoSize.observe(this, Observer { size ->
                binding.chunkCallVideoOrIcon.updateLayoutParams<ConstraintLayout.LayoutParams> {
                    dimensionRatio = "H,${size.width}:${size.height}"
                    val deviceHasButtons = callViewModel.device.value?.actions?.size?.let {
                        it > 0
                    } ?: false
                    matchConstraintPercentWidth = computePercentageWidth(size,if (deviceHasButtons) 280 else 180) //  180dp left for buttons and header if no actions, 300dp otherwise
                }
            })
            callViewModel.videoContent.observe(this, {
                callViewModel.videoFullScreen.value = it
            })
            startWithPermissionCheck()
        } ?: finish()
    }

    override fun onResume() {
        super.onResume()
        coreContext.core.nativeVideoWindowId =
            if (callViewModel.videoFullScreen.value!!) binding.videofullscreen else binding.chunkCallDeviceIconOrVideo?.videocollapsed

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
