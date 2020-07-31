package org.lindoor.ui.assistant

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_assistant_root.view.*
import kotlinx.android.synthetic.main.widget_round_rect_button.view.*
import org.lindoor.GenericFragment
import org.lindoor.R
import org.lindoor.databinding.FragmentAssistantRootBinding
import org.lindoor.entities.Account
import org.lindoor.utils.DialogUtil

class AssistantRootFragment : GenericFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentAssistantRootBinding.inflate(inflater, container, false)

        binding.root.create.root.setOnClickListener {
            navigateToCompotent(R.id.navigation_assistant_create_lindoor)
        }
        binding.root.use.root.setOnClickListener {
            navigateToCompotent(R.id.navigation_assistant_login_lindoor)
        }
        binding.root.sip.root.setOnClickListener {
            navigateToCompotent(R.id.navigation_assistant_login_sip)
        }
        binding.root.remote.root.setOnClickListener {
            navigateToCompotent(R.id.navigation_assistant_remote_root)
        }
        return binding.root
    }

    private fun navigateToCompotent(componentResource: Int)  {
        if (Account.configured()) {
            DialogUtil.confirm(
                "assistant_using_will_disconnect_title",
                "assistant_using_will_disconnect_message",
                { _: DialogInterface, _: Int ->
                    Account.disconnect()
                    mainactivity.navController.navigate(componentResource)
                })
        } else
            mainactivity.navController.navigate(componentResource)
    }

}
