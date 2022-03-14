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

package org.linhome.ui.devices.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import org.linhome.GenericFragment
import org.linhome.customisation.Texts
import org.linhome.customisation.Theme
import org.linhome.databinding.FragmentDeviceInfoBinding

class DeviceInfoFragment : GenericFragment() {

    val args: DeviceEditorFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val binding = FragmentDeviceInfoBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.device = args.device
        mainactivity.toolbarViewModel.rightButtonVisible.value = args.device?.isRemotelyProvisionned != true
        return binding.root
    }

    override fun onToolbarRightButtonClicked() {
        val actionDetail = DeviceInfoFragmentDirections.deviceEdit()
        actionDetail.device = args.device
        mainactivity.navController.navigate(actionDetail)
    }

    override fun onResume() {
        super.onResume()
        Theme.setIcon("icons/edit",  mainactivity.binding.appbar.toolbarRightButtonImage)
        mainactivity.binding.appbar.toolbarRightButtonTitle.text = Texts.get("edit")
        mainactivity.resumeNavigation()
    }

}
