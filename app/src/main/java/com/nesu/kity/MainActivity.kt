package com.nesu.kity

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2

class MainActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var pagerAdapter: ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewPager = findViewById(R.id.view_pager)
        pagerAdapter = ViewPagerAdapter(this)
        viewPager.adapter = pagerAdapter

        if (pagerAdapter.actualItemCount > 1) {
            val middlePosition = Integer.MAX_VALUE / 2
            val initialPage = middlePosition - (middlePosition % pagerAdapter.actualItemCount)
            viewPager.setCurrentItem(initialPage, false)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentActualPage = pagerAdapter.getActualPosition(viewPager.currentItem)
                if (currentActualPage == 1) {
                    navigateToUploadScreen()
                } else {
                    if (isEnabled) {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        })
    }

    fun navigateToUploadScreen() {
        if (pagerAdapter.actualItemCount <= 0) return
        val currentAdapterPosition = viewPager.currentItem
        val currentActualPosition = pagerAdapter.getActualPosition(currentAdapterPosition)

        if (currentActualPosition != 0) {
            viewPager.setCurrentItem(currentAdapterPosition - 1, true)
        }
    }

    fun navigateToFilesScreen() {
        if (pagerAdapter.actualItemCount <= 1) return
        val currentAdapterPosition = viewPager.currentItem
        val currentActualPosition = pagerAdapter.getActualPosition(currentAdapterPosition)

        if (currentActualPosition != 1) {
            viewPager.setCurrentItem(currentAdapterPosition + 1, true)
        }
    }
}