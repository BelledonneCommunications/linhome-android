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

package org.linhome.ui.assistant.loginlinhome

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.linhome.LinhomeApplication
import org.linhome.LinhomeApplication.Companion.coreContext
import org.linhome.LinhomeApplication.Companion.corePreferences
import org.linhome.R
import org.linhome.customisation.Texts
import org.linhome.databinding.FragmentAssistantLoginLinhomeBinding
import org.linhome.entities.LinhomeAccount
import org.linhome.ui.assistant.shared.CreatorAssistantFragment
import org.linhome.ui.validators.ValidatorFactory
import org.linhome.utils.DialogUtil
import org.linphone.core.AccountCreator
import org.linphone.core.Core
import org.linphone.core.XmlRpcArgType


class LoginLinhomeAccountFragment : CreatorAssistantFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding = FragmentAssistantLoginLinhomeBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this

        val model = ViewModelProvider(this).get(LoginLinhomeAccountViewModel::class.java)
        binding.model = model
        binding.validators = ValidatorFactory.Companion

        binding.login.setOnClickListener {
            binding.username.validate()
            binding.password.validate()
            updateField(model.setUsername(model.username), binding.username)
            updateField(model.setPassword(model.pass1), binding.password)
            if (model.fieldsValid()) {
                hideKeyboard()
                showProgress()
                model.accountCreatorResult.observe(viewLifecycleOwner) { response ->
                    hideProgress()
                    if (response == AccountCreator.Status.AccountExist) {
                        model.linhomeAccountCreateProxyConfig(model.accountCreator,true, model.sipRegistrationResult)
                    } else {
                        binding.username.setError(Texts.get("linhome_account_login_failed_unknown_user_or_wroong_password"))
                    }
                }
                model.fireLogin()
            }
        }

        model.sipRegistrationResult.observe(viewLifecycleOwner) { sipOk ->
            if (sipOk) {
                mainactivity.navController.popBackStack(R.id.navigation_devices, false)
                DialogUtil.info("linhome_account_loggedin")
                GlobalScope.launch(context = Dispatchers.Main) { // Fetch vcards
                    coreContext.core.stop()
                    coreContext.core.start()
                }
            } else {
                binding.username.setError(Texts.get("linhome_account_login_failed_unknown_user_or_wroong_password"))
            }
        }

        return binding.root
    }


}
