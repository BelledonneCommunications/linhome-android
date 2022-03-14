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

package org.linhome.ui.account

import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import org.linhome.LinhomeApplication
import org.linhome.databinding.FragmentAccountBinding
import org.linhome.entities.Account
import org.linhome.utils.DialogUtil


class AccountFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentAccountBinding.inflate(inflater, container, false)
        val model = ViewModelProvider(this).get(AccountViewModel::class.java)
        binding.model = model
        binding.lifecycleOwner  = this

        binding.refreshRegisters.setOnClickListener {
            model.refreshRegisters()
        }

        binding.changeLinhomePassword.setOnClickListener {
            gotoFreeSip()
        }

        binding.deleteLinhomeAccount.setOnClickListener {
            gotoFreeSip()
        }

        binding.disconnect.setOnClickListener {
            DialogUtil.confirm(
                "menu_disconnect",
                "disconnect_confirm_message",
                { _: DialogInterface, _: Int ->
                    Account.disconnect()
                    findNavController().navigateUp()
                })
        }


        return binding.root
    }

    fun gotoFreeSip() {
        DialogUtil.confirm(
            "account_manage_on_freesip_title",
            "account_manage_on_freesip_message",
            { _: DialogInterface, _: Int ->
                val browserIntent =
                    Intent(Intent.ACTION_VIEW, Uri.parse(LinhomeApplication.corePreferences.config.getString("assistant","freesip_url","https://subscribe.linhome.org/login")))
                startActivity(browserIntent)
            })
    }
}
