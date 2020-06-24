package org.lindoor.ui.devices

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
import org.lindoor.GenericFragment
import org.lindoor.LindoorApplication
import org.lindoor.databinding.FragmentDevicesBinding
import org.lindoor.entities.Device


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
            MutableLiveData<Device>() // Android bug - onCreateView called on navigateUp()
        binding.lifecycleOwner = this
        binding.model = devicesViewModel

        binding.root.new_device.setOnClickListener {
            binding.root.new_device.visibility = View.INVISIBLE
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

        if (LindoorApplication.instance.smartPhone()) {
            devicesViewModel.selectedDevice.observe(viewLifecycleOwner, Observer { device ->
                val actionDetail = DevicesFragmentDirections.deviceInfo(device)
                mainactivity.navController.navigate(actionDetail)
            })
        }

        if (LindoorApplication.instance.tablet()) {
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
        binding.root.new_device.visibility = View.VISIBLE
    }

}
