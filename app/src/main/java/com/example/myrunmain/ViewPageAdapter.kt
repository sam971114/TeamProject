package com.example.myrunmain

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.myrunmain.AllFragment
import com.example.myrunmain.FirFragment
import com.example.myrunmain.SecFragment
import com.example.myrunmain.TrdFragment

private const val NUM_TABS = 4

class ViewPageAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle) :
    FragmentStateAdapter(fragmentManager, lifecycle) {

    override fun getItemCount(): Int {
        return NUM_TABS
    }

    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return AllFragment()
            1 -> return FirFragment()
            2 -> return SecFragment()
            3 -> return TrdFragment()
            else -> return AllFragment()
        }
    }
}