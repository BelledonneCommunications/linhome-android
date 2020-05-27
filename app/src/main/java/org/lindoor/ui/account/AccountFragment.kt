package org.lindoor.ui.account

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.lindoor.LindoorApplication
import org.lindoor.databinding.FragmentAboutBinding
import org.lindoor.databinding.FragmentAccountBinding

class AccountFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentAccountBinding.inflate(inflater, container, false)
        binding.coreContext = LindoorApplication.coreContext
        return binding.root
    }
}
