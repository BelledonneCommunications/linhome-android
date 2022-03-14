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

package org.linhome.ui.settings

import android.content.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import org.linhome.BR
import org.linhome.GenericFragment
import org.linhome.LinhomeApplication
import org.linhome.R
import org.linhome.customisation.Texts
import org.linhome.databinding.FragmentSettingsBinding
import org.linhome.utils.DialogUtil
import org.linphone.core.Core
import org.linphone.core.PayloadType
import org.linphone.core.tools.Log


class SettingsFragment : GenericFragment() {

    private lateinit var settingsViewModel: SettingsViewModel
    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        settingsViewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)
        initCodecsList(
            LinhomeApplication.coreContext.core.audioPayloadTypes,
            settingsViewModel.audioCodecs,
            true
        )
        initCodecsList(
            LinhomeApplication.coreContext.core.videoPayloadTypes,
            settingsViewModel.videCodecs
        )
        binding.model = settingsViewModel
        binding.view = this
        binding.lifecycleOwner = this
        return binding.root
    }


    private fun initCodecsList(
        payloads: Array<PayloadType>,
        target: ArrayList<ViewDataBinding>,
        showRate: Boolean = false
    ) {
        for (payload in payloads) {
            val binding = DataBindingUtil.inflate<ViewDataBinding>(
                LayoutInflater.from(requireContext()),
                R.layout.settings_widget_switch,
                null,
                false
            )
            binding.setVariable(BR.title, payload.mimeType)
            if (showRate)
                binding.setVariable(BR.subtitle, "${payload.clockRate} Hz")
            binding.setVariable(BR.checked, payload.enabled())
            binding.setVariable(BR.listener, object : SettingListenerStub() {
                override fun onBoolValueChanged(newValue: Boolean) {
                    payload.enable(newValue)
                }
            })
            binding.lifecycleOwner = this
            target.add(binding)
        }
    }


    val sendLogsListener = object : SettingListenerStub() {
        override fun onClicked() {
            showProgress()
            binding.sendLogs.root.isEnabled = false
            settingsViewModel.logUploadResult.observe(viewLifecycleOwner, Observer { result ->
                when (result.first) {
                    Core.LogCollectionUploadState.InProgress -> {
                    }
                    Core.LogCollectionUploadState.NotDelivered -> {
                        hideProgress()
                        binding.sendLogs.root.isEnabled = true
                        DialogUtil.error("log_upload_failed")
                    }
                    Core.LogCollectionUploadState.Delivered -> {
                        hideProgress()
                        binding.sendLogs.root.isEnabled = true
                        val clipboard =
                            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Logs url", result.second)
                        clipboard.setPrimaryClip(clip)
                        DialogUtil.toast("log_upload_success")
                        shareUploadedLogsUrl(result.second)
                    }
                }
            })
            LinhomeApplication.coreContext.core.uploadLogCollection()
        }
    }

    val clearLogsListener = object : SettingListenerStub() {
        override fun onClicked() {
            LinhomeApplication.coreContext.core.resetLogCollection()
            DialogUtil.toast("log_clear_success")
        }
    }

    private fun shareUploadedLogsUrl(info: String) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(
            Intent.EXTRA_EMAIL,
            arrayOf(Texts.get("support_email_android"))
        )
        intent.putExtra(Intent.EXTRA_SUBJECT, "${Texts.appName} Logs")
        intent.putExtra(Intent.EXTRA_TEXT, info)
        intent.type = "application/zip"

        try {
            startActivity(Intent.createChooser(intent, "Send mail..."))
        } catch (ex: ActivityNotFoundException) {
            Log.e(ex)
        }
    }


}
