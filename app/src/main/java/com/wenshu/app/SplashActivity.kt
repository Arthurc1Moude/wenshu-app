package com.wenshu.app

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.wenshu.app.data.SharedPreferencesManager
import com.wenshu.app.databinding.ActivitySplashBinding
import com.wenshu.app.ui.auth.LoginActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private var hasNavigated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imgLogo.alpha = 0f
        binding.imgLogo.scaleX = 0.8f
        binding.imgLogo.scaleY = 0.8f
        binding.tvAppName.alpha = 0f
        binding.tvAppName.translationY = 40f
        binding.tvSlogan.alpha = 0f
        binding.tvSlogan.translationY = 30f
        binding.viewLine.alpha = 0f
        binding.viewLine.scaleX = 0f
        binding.tvVersion.alpha = 0f

        binding.root.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                binding.root.viewTreeObserver.removeOnPreDrawListener(this)
                binding.root.post { startAnimations() }
                return true
            }
        })

        scheduleNavigation()
    }

    private fun startAnimations() {
        val logoFade = ObjectAnimator.ofFloat(binding.imgLogo, View.ALPHA, 0f, 1f).setDuration(700)
        val logoScaleX = ObjectAnimator.ofFloat(binding.imgLogo, View.SCALE_X, 0.8f, 1f).setDuration(700)
        val logoScaleY = ObjectAnimator.ofFloat(binding.imgLogo, View.SCALE_Y, 0.8f, 1f).setDuration(700)

        val nameFade = ObjectAnimator.ofFloat(binding.tvAppName, View.ALPHA, 0f, 1f).setDuration(500)
        val nameTranslate = ObjectAnimator.ofFloat(binding.tvAppName, View.TRANSLATION_Y, 40f, 0f).setDuration(500)
        val sloganFade = ObjectAnimator.ofFloat(binding.tvSlogan, View.ALPHA, 0f, 1f).setDuration(500)
        val sloganTranslate = ObjectAnimator.ofFloat(binding.tvSlogan, View.TRANSLATION_Y, 30f, 0f).setDuration(500)
        val lineScale = ObjectAnimator.ofFloat(binding.viewLine, View.SCALE_X, 0f, 1f).setDuration(500)
        val lineFade = ObjectAnimator.ofFloat(binding.viewLine, View.ALPHA, 0f, 1f).setDuration(500)
        val versionFade = ObjectAnimator.ofFloat(binding.tvVersion, View.ALPHA, 0f, 0.5f).setDuration(500)

        nameFade.startDelay = 500
        nameTranslate.startDelay = 500
        sloganFade.startDelay = 700
        sloganTranslate.startDelay = 700
        lineScale.startDelay = 900
        lineFade.startDelay = 900
        versionFade.startDelay = 1200

        logoScaleX.interpolator = DecelerateInterpolator(1.5f)
        logoScaleY.interpolator = DecelerateInterpolator(1.5f)
        nameTranslate.interpolator = DecelerateInterpolator()
        sloganTranslate.interpolator = DecelerateInterpolator()
        lineScale.interpolator = AccelerateDecelerateInterpolator()

        AnimatorSet().apply {
            playTogether(logoFade, logoScaleX, logoScaleY, nameFade, nameTranslate, sloganFade, sloganTranslate, lineScale, lineFade, versionFade)
            start()
        }
    }

    private fun scheduleNavigation() {
        binding.root.postDelayed({ navigate() }, 2200)
    }

    private fun navigate() {
        if (hasNavigated) return
        hasNavigated = true

        if (SharedPreferencesManager.isLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
