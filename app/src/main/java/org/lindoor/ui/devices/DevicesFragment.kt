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

class DevicesFragment :LindoorFragment() {

    private lateinit var devicesViewModel: DevicesViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        devicesViewModel =
                ViewModelProvider(this).get(DevicesViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_devices, container, false)
        devicesViewModel.text.observe(viewLifecycleOwner, Observer {
            root.text_dashboard.text = it
        })
        return root
    }
}
