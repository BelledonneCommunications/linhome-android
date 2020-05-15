package org.lindoor.ui.about

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_history.view.*
import org.lindoor.LindoorApplication
import org.lindoor.R
import org.lindoor.databinding.FragmentAboutBinding
import org.lindoor.databinding.FragmentAssistantCreateLindoorBinding

class AboutFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentAboutBinding.inflate(inflater, container, false)
        binding.coreContext = LindoorApplication.coreContext
        return binding.root
    }
}
