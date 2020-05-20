package org.lindoor.ui.devices.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.fragment_device_edit.view.*
import kotlinx.android.synthetic.main.item_action.view.*
import kotlinx.android.synthetic.main.widget_round_rect_button.view.*
import org.lindoor.LindoorFragment
import org.lindoor.R
import org.lindoor.databinding.FragmentDeviceEditBinding
import org.lindoor.entities.Action
import org.lindoor.ui.validators.ValidatorFactory

class DeviceEditorFragment : LindoorFragment() {


    lateinit var model:DeviceEditorViewModel
    val args: DeviceEditorFragmentArgs by navArgs()
    lateinit var binding : FragmentDeviceEditBinding
    
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        editionMode()

        binding = FragmentDeviceEditBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        model = ViewModelProvider(this).get(DeviceEditorViewModel::class.java)
        binding.model = model
        binding.validators  = ValidatorFactory.Companion

        binding.root.addaction.root.setOnClickListener {
            addAction(null)
        }

        if (model.device == null) {
            addAction(null)
        } else {
            model.device = args.device
            model.device!!.actions?.forEach {
                addAction(it)
            }
        }

        return binding.root
    }

    private fun addAction(action:Action?) {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(LayoutInflater.from(requireContext()), R.layout.item_action, null, false)
        binding.lifecycleOwner = this
        model.addAction(action,binding)
    }

    override fun onToolbarRightButtonClicked() {
        binding.root.name.validate()
        binding.root.address.validate()
        model.actionsViewModels.forEach {
            if (it.binding.root.type.liveIndex?.value != 0)
                it.binding.root.code.validate()
        }
        if (model.saveDevice()) {
            mainactivity.navController.navigateUp()
        }
    }

    override fun onToolbarLeftButtonClicked() {
        mainactivity.navController.navigateUp()
    }

}
