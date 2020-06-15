package org.lindoor.ui.devices.edit

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.fragment_device_edit.view.*
import kotlinx.android.synthetic.main.item_action_edit.view.*
import kotlinx.android.synthetic.main.widget_round_rect_button.view.*
import org.lindoor.GenericFragment
import org.lindoor.R
import org.lindoor.customisation.Texts
import org.lindoor.customisation.Theme
import org.lindoor.databinding.FragmentDeviceEditBinding
import org.lindoor.entities.Action
import org.lindoor.store.DeviceStore
import org.lindoor.ui.validators.ValidatorFactory
import org.lindoor.utils.DialogUtil

class DeviceEditorFragment : GenericFragment() {


    lateinit var model: DeviceEditorViewModel
    val args: DeviceEditorFragmentArgs by navArgs()
    lateinit var binding: FragmentDeviceEditBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        binding = FragmentDeviceEditBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        model = ViewModelProvider(this).get(DeviceEditorViewModel::class.java)
        binding.model = model
        binding.validators = ValidatorFactory.Companion

        binding.root.addaction.root.setOnClickListener {
            addAction(null)
        }

        model.device = args.device

        if (model.device == null) {
            addAction(null)
        } else {
            model.device!!.actions?.forEach {
                addAction(it)
            }
        }

        binding.root.delete.root.setOnClickListener {
            DialogUtil.confirm(
                "delete_device_confirm_message",
                { dialog: DialogInterface, which: Int ->
                    model.device?.let { it1 -> DeviceStore.removeDevice(it1) }
                    mainactivity.navController.navigate(R.id.device_deleted)
                }, model.device?.name
            )
        }

        return binding.root
    }

    private fun addAction(action: Action?) {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            LayoutInflater.from(requireContext()),
            R.layout.item_action_edit,
            null,
            false
        )
        binding.lifecycleOwner = this
        model.addAction(action, binding)
    }

    override fun onToolbarRightButtonClicked() {
        binding.root.name.validate()
        binding.root.address.validate()
        model.actionsViewModels.forEach {
            if (it.type.value != 0)
                it.binding.root.code.validate()
        }
        DeviceStore.findDeviceByAddress(model.address.first.value)?.also {
            if (args.device?.id != it.id) {
                binding.root.address.setError(
                    Texts.get(
                        "device_address_already_exists",
                        "${it.name}"
                    )
                )
                return
            }
        }
        if (model.saveDevice()) {
            mainactivity.navController.navigateUp()
        }
    }

    override fun onToolbarLeftButtonClicked() {
        mainactivity.navController.navigateUp()
    }

    override fun onResume() {
        super.onResume()

        Theme.setIcon("icons/save", mainactivity.toolbar_right_button_image)
        Theme.setIcon("icons/cancel", mainactivity.toolbar_left_button_image)
        mainactivity.toolbar_left_button_title.text = Texts.get("cancel")
        mainactivity.toolbar_right_button_title.text = Texts.get("save")

        mainactivity.pauseNavigation()

        mainactivity.toolbarViewModel.rightButtonVisible.value = true
        mainactivity.toolbarViewModel.leftButtonVisible.value = true
    }

}
