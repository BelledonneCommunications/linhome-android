package org.lindoor.ui.devices.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_device_info.view.*
import org.lindoor.GenericFragment
import org.lindoor.customisation.Texts
import org.lindoor.customisation.Theme
import org.lindoor.databinding.FragmentDeviceInfoBinding
import org.lindoor.ui.devices.info.DeviceInfoActionsAdapter

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

        args.device?.actions?.also { them ->
            binding.root.actions_list.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
            binding.root.actions_list.adapter = DeviceInfoActionsAdapter(them)
        }

        return binding.root
    }

    override fun onToolbarRightButtonClicked() {
        val actionDetail = DeviceInfoFragmentDirections.deviceEdit()
        actionDetail.device = args.device
        mainactivity.navController.navigate(actionDetail)
    }

    override fun onResume() {
        super.onResume()
        Theme.setIcon("icons/edit", mainactivity.toolbar_right_button_image)
        mainactivity.toolbar_right_button_title.text = Texts.get("edit")
        mainactivity.toolbarViewModel.rightButtonVisible.value = true
        mainactivity.resumeNavigation()
    }

}
