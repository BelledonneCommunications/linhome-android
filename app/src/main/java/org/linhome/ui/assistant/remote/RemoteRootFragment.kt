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

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.linhome.GenericFragment
import org.linhome.R
import org.linhome.databinding.FragmentAssistantRemoteRootBinding.inflate
import org.linhome.utils.DialogUtil
import org.linhome.utils.extensions.invisible
import org.linhome.utils.extensions.toogleVisible
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnNeverAskAgain
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions

@RuntimePermissions
class RemoteRootFragment : GenericFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = inflate(inflater, container, false)

        binding.url.setOnClickListener {
            mainactivity.navController.navigate(R.id.navigation_assistant_remote_url)
        }

        binding.qr.setOnClickListener {
            openQRCodeViewWithPermissionCheck()
        }

        binding.infobutton.setOnClickListener {
            binding.infotext.toogleVisible()
        }
        binding.root.setOnClickListener {
            binding.infotext.invisible()
        }


        return binding.root
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        onRequestPermissionsResult(requestCode, grantResults)
    }

    @NeedsPermission(Manifest.permission.CAMERA)
    fun openQRCodeView() {
        mainactivity.navController.navigate(R.id.navigation_assistant_remote_qr)
    }

    @OnPermissionDenied(Manifest.permission.CAMERA)
    fun onCameraDenied() {
        DialogUtil.error("camera_permission_denied")
    }

    @OnNeverAskAgain(Manifest.permission.CAMERA)
    fun onCameraNeverAskAgain() {
        DialogUtil.error("camera_permission_denied_dont_ask_again")
    }


}
