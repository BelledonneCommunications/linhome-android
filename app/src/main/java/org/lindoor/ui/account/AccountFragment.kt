package org.lindoor.ui.account

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
import kotlinx.android.synthetic.main.fragment_account.view.*
import kotlinx.android.synthetic.main.widget_round_rect_button.view.*
import org.lindoor.LindoorApplication
import org.lindoor.databinding.FragmentAccountBinding
import org.lindoor.entities.Account
import org.lindoor.utils.DialogUtil


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

        binding.root.refresh_registers.root.setOnClickListener {
            model.refreshRegisters()
        }

        binding.root.change_lindoor_password.root.setOnClickListener {
            gotoFreeSip()
        }

        binding.root.delete_lindoor_account.root.setOnClickListener {
            gotoFreeSip()
        }

        binding.root.disconnect.root.setOnClickListener {
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
                    Intent(Intent.ACTION_VIEW, Uri.parse(LindoorApplication.corePreferences.config.getString("assistant","freesip_url","www.linphone.org")))
                startActivity(browserIntent)
            },confirmTextKey="continue")
    }
}
