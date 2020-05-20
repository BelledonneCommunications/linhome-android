package org.lindoor.ui.devices

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_devices.view.*
import org.lindoor.LindoorFragment
import org.lindoor.databinding.FragmentDevicesBinding

class DevicesFragment :LindoorFragment() {

    private lateinit var devicesViewModel: DevicesViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentDevicesBinding.inflate(inflater, container, false)
        devicesViewModel = ViewModelProvider(this).get(DevicesViewModel::class.java)
        binding.lifecycleOwner = this
        binding.model = devicesViewModel

        binding.root.new_device.setOnClickListener{
            val actionDetail = DevicesFragmentDirections.editDevice()
            //actionDetail.setDevice(null)
            mainactivity.navController.navigate(actionDetail)
        }

        binding.root.device_list.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.root.device_list.adapter = DevicesAdapter(devicesViewModel.devices,binding.root.device_list)

        return binding.root
    }

}
