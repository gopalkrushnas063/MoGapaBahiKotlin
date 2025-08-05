// ui/view/OnboardingActivity.kt
package com.example.storybookapiintegration.ui.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.storybookapiintegration.R
import com.example.storybookapiintegration.databinding.ActivityOnboardingBinding
import com.example.storybookapiintegration.data.model.OnboardingItem
import com.example.storybookapiintegration.ui.adapter.OnboardingAdapter

class OnboardingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOnboardingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val onboardingItems = listOf(
            OnboardingItem(
                android.R.drawable.ic_dialog_info,
                "Welcome to StoryBook",
                "Discover amazing stories from around the world"
            ),
            OnboardingItem(
                android.R.drawable.ic_dialog_email,
                "Read Anytime",
                "Access your favorite stories anytime, anywhere"
            ),
            OnboardingItem(
                android.R.drawable.ic_dialog_alert,
                "Get Started",
                "Start exploring wonderful stories now"
            )
        )

        val adapter = OnboardingAdapter(onboardingItems)
        binding.viewPager.adapter = adapter

        setupDotIndicators(onboardingItems.size)

        binding.buttonGetStarted.setOnClickListener {
            getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("onboarding_completed", true)
                .apply()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.buttonGetStarted.visibility =
                    if (position == onboardingItems.size - 1) View.VISIBLE else View.GONE
                updateDotIndicators(position)
            }
        })
    }

    private fun setupDotIndicators(count: Int) {
        binding.layoutDots.removeAllViews()

        for (i in 0 until count) {
            val imageView = ImageView(this).apply {
                setImageDrawable(ContextCompat.getDrawable(
                    this@OnboardingActivity,
                    if (i == 0) R.drawable.dot_active else R.drawable.dot_inactive
                ))
                layoutParams = LinearLayout.LayoutParams(
                    resources.getDimensionPixelSize(R.dimen.dot_size),
                    resources.getDimensionPixelSize(R.dimen.dot_size)
                ).apply {
                    setMargins(
                        resources.getDimensionPixelSize(R.dimen.dot_margin),
                        0,
                        resources.getDimensionPixelSize(R.dimen.dot_margin),
                        0
                    )
                }
            }
            binding.layoutDots.addView(imageView)
        }
    }

    private fun updateDotIndicators(position: Int) {
        for (i in 0 until binding.layoutDots.childCount) {
            val imageView = binding.layoutDots.getChildAt(i) as ImageView
            imageView.setImageDrawable(ContextCompat.getDrawable(
                this,
                if (i == position) R.drawable.dot_active else R.drawable.dot_inactive
            ))
        }
    }
}