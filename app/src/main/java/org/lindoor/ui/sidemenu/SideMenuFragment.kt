package org.lindoor.ui.sidemenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_sidemenu.view.*
import kotlinx.android.synthetic.main.item_sidemenu.view.*
import org.lindoor.MainActivity
import org.lindoor.customisation.Texts
import org.lindoor.customisation.Theme
import org.lindoor.databinding.FragmentSidemenuBinding

class SideMenuFragment : Fragment() {

    private lateinit var sideMenuViewModel: SideMenuViewModel

    private fun getMainActivity() : MainActivity {
        return activity as MainActivity
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        sideMenuViewModel =
                ViewModelProvider(this).get(SideMenuViewModel::class.java)

        val binding = FragmentSidemenuBinding.inflate(inflater, container, false)


        binding.root.list.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        binding.root.list.adapter = SideMenuAdapter(sideMenuViewModel.sideMenuOptions,getMainActivity().navController)

        binding.root.label.text = Texts.get(sideMenuViewModel.sideMenuDisconnectOption.textKey)
        Theme.setImage(sideMenuViewModel.sideMenuDisconnectOption.iconFile,binding.root.icon)
        binding.root.top_separator.setBackgroundColor(Theme.getColor("color_h"))
        binding.root.top_separator.visibility = View.VISIBLE
        binding.root.bottom_separator.visibility = View.GONE
        binding.root.disconnect.background = Theme.selectionEffectAsStateListDrawable("sidemenu_option")
        binding.root.setBackgroundColor(Theme.getColor("color_b"))
        return binding.root
    }


}
