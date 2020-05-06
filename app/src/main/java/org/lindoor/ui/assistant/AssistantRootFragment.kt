package org.lindoor.ui.assistant

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_assistant_root.view.*
import kotlinx.android.synthetic.main.widget_round_rect_button.view.*
import org.lindoor.LindoorFragment
import org.lindoor.R
import org.lindoor.databinding.FragmentAssistantRootBinding

class AssistantRootFragment :LindoorFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentAssistantRootBinding.inflate(inflater, container, false)

        binding.root.create.root.setOnClickListener{
            mainactivity.navController.navigate(R.id.navigation_assistant_create_lindoor)
        }
        binding.root.use.root.setOnClickListener{
            mainactivity.navController.navigate(R.id.navigation_assistant_login_lindoor)
        }
        binding.root.sip.root.setOnClickListener{
            mainactivity.navController.navigate(R.id.navigation_assistant_login_sip)
        }
        binding.root.remote.root.setOnClickListener{
            mainactivity.navController.navigate(R.id.navigation_assistant_remote_root)
        }
        return binding.root
    }

}
