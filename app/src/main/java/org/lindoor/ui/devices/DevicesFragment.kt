package org.lindoor.ui.devices

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_devices.view.*
import org.lindoor.LindoorFragment
import org.lindoor.R
import org.lindoor.databinding.FragmentAssistantRootBinding
import org.lindoor.databinding.FragmentDevicesBinding

class DevicesFragment :LindoorFragment() {

    private lateinit var devicesViewModel: DevicesViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentDevicesBinding.inflate(inflater, container, false)
        devicesViewModel =
                ViewModelProvider(this).get(DevicesViewModel::class.java)

        return binding.root
    }
}
