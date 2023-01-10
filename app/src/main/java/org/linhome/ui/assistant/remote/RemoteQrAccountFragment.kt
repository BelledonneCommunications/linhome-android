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

package org.linhome.ui.assistant.remote

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.linhome.GenericFragment
import org.linhome.LinhomeApplication.Companion.coreContext
import org.linhome.R
import org.linhome.databinding.FragmentAssistantRemoteQrBinding
import org.linhome.utils.DialogUtil
import org.linhome.utils.extensions.invisible
import org.linhome.utils.extensions.toogleVisible
import org.linphone.core.ConfiguringState
import org.linphone.core.tools.Log

class RemoteQrAccountFragment : GenericFragment() {

    lateinit var binding: FragmentAssistantRemoteQrBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAssistantRemoteQrBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        val model = ViewModelProvider(this).get(RemoteAnyAccountViewModel::class.java)
        model.configurationResult.observe(viewLifecycleOwner, Observer { status ->
            hideProgress()
            when (status) {
                ConfiguringState.Failed -> {
                    DialogUtil.error("remote_configuration_failed",{ _: DialogInterface, _: Int ->
                        startScanner()
                    })
                }
                ConfiguringState.Skipped -> {
                    DialogUtil.error("remote_configuration_failed",
                        { _: DialogInterface, _: Int ->
                        startScanner()
                    })
                }
                else -> {}
            }
        })
        model.pushReady.observe(viewLifecycleOwner, Observer { status ->
            mainactivity.navController.popBackStack(R.id.navigation_devices, false)
            if (status) {
                DialogUtil.info("remote_configuration_success")
            } else
                DialogUtil.error("failed_creating_pushgateway")
        })


        binding.infobutton.setOnClickListener {
            binding.infotext.toogleVisible()
        }

        binding.root.setOnClickListener {
            binding.infotext.invisible()
        }


        return binding.root
    }


    fun startScanner() {
        coreContext.core.reloadVideoDevices()
        setBackCamera()
        coreContext.core.nativePreviewWindowId = binding.qrcode
        coreContext.core.isQrcodeVideoPreviewEnabled = true
        coreContext.core.isVideoPreviewEnabled = true
    }

    override fun onResume() {
        super.onResume()
        startScanner()
    }

    private fun setBackCamera() {
        for (camera in coreContext.core.videoDevicesList) {
            if (camera.contains("Back")) {
                Log.i("[QR Code] Found back facing camera: $camera")
                coreContext.core.videoDevice = camera
                return
            }
        }

        val first = coreContext.core.videoDevicesList[0]
        Log.i("[QR Code] Using first camera found: $first")
        coreContext.core.videoDevice = first
    }

    override fun onPause() {
        coreContext.core.nativePreviewWindowId = null
        coreContext.core.isQrcodeVideoPreviewEnabled = false
        coreContext.core.isVideoPreviewEnabled = false
        super.onPause()
    }


}



