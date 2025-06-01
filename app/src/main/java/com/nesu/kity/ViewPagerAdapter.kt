package com.nesu.kity

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    val actualItemCount = 2

    override fun getItemCount(): Int {
        return if (actualItemCount > 1) Integer.MAX_VALUE else actualItemCount
    }

    override fun createFragment(position: Int): Fragment {
        val actualPosition = getActualPosition(position)
        return when (actualPosition) {
            0 -> UploadScreenFragment()
            1 -> FilesScreenFragment()
            else -> throw IllegalStateException("Invalid actual position: $actualPosition for virtual position $position")
        }
    }

    fun getActualPosition(position: Int): Int {
        return if (actualItemCount == 0) 0 else position % actualItemCount
    }
}