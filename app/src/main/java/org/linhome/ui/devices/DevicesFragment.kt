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

package org.linhome.ui.devices

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_device_info.view.*
import kotlinx.android.synthetic.main.fragment_devices.view.*
import org.linhome.GenericFragment
import org.linhome.LinhomeApplication
import org.linhome.databinding.FragmentDevicesBinding
import org.linhome.entities.Device


class DevicesFragment : GenericFragment() {

    private lateinit var devicesViewModel: DevicesViewModel
    private lateinit var binding: FragmentDevicesBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDevicesBinding.inflate(inflater, container, false)
        devicesViewModel = ViewModelProvider(this).get(DevicesViewModel::class.java)
        devicesViewModel.selectedDevice =
            MutableLiveData<Device?>() // Android bug - onCreateView called on navigateUp()
        binding.lifecycleOwner = this
        binding.model = devicesViewModel

        binding.root.new_device.setOnClickListener {
            binding.root.new_device.visibility = View.INVISIBLE
            val actionDetail = DevicesFragmentDirections.deviceNew()
            mainactivity.navController.navigate(actionDetail)
        }

        binding.root.new_device_none_configured?.setOnClickListener {
            binding.root.new_device_none_configured.visibility = View.INVISIBLE
            val actionDetail = DevicesFragmentDirections.deviceNew()
            mainactivity.navController.navigate(actionDetail)
        }

        binding.root.device_list.layoutManager =
            LinearLayoutManager(activity, RecyclerView.VERTICAL, false)

        binding.root.device_list.adapter = DevicesAdapter(
            devicesViewModel.devices,
            binding.root.device_list,
            devicesViewModel.selectedDevice,
            this
        )

        if (LinhomeApplication.instance.smartPhone()) {
            devicesViewModel.selectedDevice.observe(viewLifecycleOwner, Observer { device ->
                val actionDetail = DevicesFragmentDirections.deviceInfo(device!!)
                mainactivity.navController.navigate(actionDetail)
            })
        }

        if (LinhomeApplication.instance.tablet()) {
            binding.root.edit_device?.setOnClickListener {
                val actionDetail = DevicesFragmentDirections.deviceEditTablet()
                actionDetail.device = devicesViewModel.selectedDevice.value
                mainactivity.navController.navigate(actionDetail)
            }
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        if (LinhomeApplication.instance.tablet()) {
            if (devicesViewModel.devices.value!!.size == 0) {
                binding.root.new_device.visibility = View.GONE
                binding.root.new_device_none_configured.visibility = View.VISIBLE
            } else {
                binding.root.new_device.visibility = View.VISIBLE
                binding.root.new_device_none_configured.visibility = View.GONE
            }

        } else {
            binding.root.new_device.visibility = View.VISIBLE
        }

    }

}
