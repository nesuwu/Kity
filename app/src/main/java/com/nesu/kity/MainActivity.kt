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

        val middlePosition = Integer.MAX_VALUE / 2
        val initialPage = middlePosition - (middlePosition % 2)
        viewPager.setCurrentItem(initialPage, false)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentActualPage = pagerAdapter.getActualPosition(viewPager.currentItem)
                if (currentActualPage == 1) {
                    viewPager.setCurrentItem(viewPager.currentItem - 1, true)
                } else {
                    if (isEnabled) {
                        isEnabled = false
                        onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        })
    }
}

