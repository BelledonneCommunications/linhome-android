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

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.linhome.LinhomeApplication.Companion.coreContext
import org.linhome.R
import org.linhome.compatibility.Compatibility
import org.linhome.databinding.ActivityCallIncomingBinding
import org.linphone.core.Call


class CallIncomingActivity : CallGenericActivity() {

    lateinit var binding: ActivityCallIncomingBinding
    private lateinit var callViewModel: CallViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Compatibility.setShowWhenLocked(this, true)
        Compatibility.setTurnScreenOn(this, true)
        Compatibility.requestDismissKeyguard(this)

        binding = DataBindingUtil.setContentView(
            this,
            R.layout.activity_call_incoming
        ) as ActivityCallIncomingBinding
        binding.lifecycleOwner = this

        call?.also {
            callViewModel =
                ViewModelProvider(this, CallViewModelFactory(it))[CallViewModel::class.java]
            binding.callmodel = callViewModel
            callViewModel.callState.observe(this, Observer { callState ->
                when (callState) {
                    Call.State.IncomingEarlyMedia, Call.State.IncomingReceived -> return@Observer
                    else -> finish()
                }
            })
            binding.chunkCallDeviceIconOrVideo.videotogglecollapsed.setOnClickListener {
                coreContext.core.nativeVideoWindowId = binding.videofullscreen
                callViewModel.toggleVideoFullScreen()
            }
            binding.videotogglefullscreen.setOnClickListener {
                coreContext.core.nativeVideoWindowId = binding.chunkCallDeviceIconOrVideo.videocollapsed
                callViewModel.toggleVideoFullScreen()
            }
        } ?: finish()
    }

    override fun onResume() {
        super.onResume()
        coreContext.core.nativeVideoWindowId =
            if (callViewModel.videoFullScreen.value!!) binding.videofullscreen else binding.chunkCallDeviceIconOrVideo.videocollapsed
    }

    override fun onPause() {
        coreContext.core.nativeVideoWindowId = null
        super.onPause()
    }

}
