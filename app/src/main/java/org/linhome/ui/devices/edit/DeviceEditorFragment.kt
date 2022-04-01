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

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import org.linhome.GenericFragment
import org.linhome.R
import org.linhome.customisation.Texts
import org.linhome.customisation.Theme
import org.linhome.databinding.FragmentDeviceEditBinding
import org.linhome.databinding.ItemActionEditBinding
import org.linhome.entities.Action
import org.linhome.store.DeviceStore
import org.linhome.ui.validators.ValidatorFactory
import org.linhome.utils.DialogUtil
import org.linhome.utils.cdlog

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

        binding.addaction.setOnClickListener {
            addAction(null)
        }

        model.device = args.device

        if (model.actionsViewModels.isEmpty()) {
            if (model.device == null) {
                addAction(null)
            } else {
                model.device!!.actions?.forEach {
                    addAction(it)
                }
            }
        }

        binding.delete.setOnClickListener {
            DialogUtil.confirm(
                "delete_device_confirm_message",
                { _: DialogInterface, _: Int ->
                    model.device?.let { it1 -> DeviceStore.removeDevice(it1) }
                    mainactivity.navController.navigate(R.id.device_deleted)
                }, model.device?.name
            )
        }

        model.refreshActions.observe(viewLifecycleOwner, Observer { refresh ->
            for ((index, actionViewModel) in model.actionsViewModels.withIndex()) {
                actionViewModel.displayIndex.value = index+1
            }
        })

        return binding.root
    }

    private fun addAction(action: Action?) {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            LayoutInflater.from(requireContext()),
            R.layout.item_action_edit,
            null,
            false
        ) as ItemActionEditBinding
        binding.lifecycleOwner = this
        model.addAction(action, binding)
    }

    override fun onToolbarRightButtonClicked() {
        binding.name.validate()
        binding.address.validate()
        model.actionsViewModels.forEach {
            if (it.type.value != 0)
                it.binding.code.validate()
        }
        DeviceStore.findDeviceByAddress(model.address.first.value)?.also {
            cdlog("${args.device?.id} ${it.id}")
            if (args.device?.id != it.id) {
                binding.address.setError(
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

        Theme.setIcon("icons/save", mainactivity.binding.appbar.toolbarRightButtonImage)
        Theme.setIcon("icons/cancel", mainactivity.binding.appbar.toolbarLeftButtonImage)
        mainactivity.binding.appbar.toolbarLeftButtonTitle.text = Texts.get("cancel")
        mainactivity.binding.appbar.toolbarRightButtonTitle.text = Texts.get("save")

        mainactivity.pauseNavigation()

        mainactivity.toolbarViewModel.rightButtonVisible.value = true
        mainactivity.toolbarViewModel.leftButtonVisible.value = true
    }

}
