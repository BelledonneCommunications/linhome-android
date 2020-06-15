package org.lindoor.ui.devices.edit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.app_bar_main.*
import org.lindoor.GenericFragment
import org.lindoor.customisation.Texts
import org.lindoor.customisation.Theme
import org.lindoor.databinding.FragmentDeviceInfoBinding

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
