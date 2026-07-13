package com.wenshu.app.ui.settings

import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.wenshu.app.R
import com.wenshu.app.data.SharedPreferencesManager
import com.wenshu.app.databinding.ActivityPostSettingsBinding

class PostSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostSettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener { finish() }

        loadSettings()
        setupListeners()
    }

    private fun loadSettings() {
        val defaultAmount = SharedPreferencesManager.getDefaultTipAmount()
        binding.tvTipAmount.text = "${defaultAmount}币"

        binding.switchDoubleTapLike.isChecked = SharedPreferencesManager.isDoubleTapToLikeEnabled()
        binding.switchImagePreview.isChecked = SharedPreferencesManager.isShowImagePreviewEnabled()
        binding.switchAutoPlayVideo.isChecked = SharedPreferencesManager.isAutoPlayVideoEnabled()
        binding.switchSoundEffects.isChecked = SharedPreferencesManager.isFeedSoundEffectsEnabled()
    }

    private fun setupListeners() {
        binding.itemDefaultTip.setOnClickListener {
            showTipAmountPicker()
        }

        binding.switchDoubleTapLike.setOnCheckedChangeListener { _, isChecked ->
            SharedPreferencesManager.setDoubleTapToLikeEnabled(isChecked)
        }

        binding.switchImagePreview.setOnCheckedChangeListener { _, isChecked ->
            SharedPreferencesManager.setShowImagePreviewEnabled(isChecked)
        }

        binding.switchAutoPlayVideo.setOnCheckedChangeListener { _, isChecked ->
            SharedPreferencesManager.setAutoPlayVideoEnabled(isChecked)
        }

        binding.switchSoundEffects.setOnCheckedChangeListener { _, isChecked ->
            SharedPreferencesManager.setFeedSoundEffectsEnabled(isChecked)
        }
    }

    private fun showTipAmountPicker() {
        val options = intArrayOf(1, 5, 10, 20, 50, 100, 200, 500)
        val labels = options.map { "${it}币" }.toTypedArray()
        val current = SharedPreferencesManager.getDefaultTipAmount()
        val checkedIdx = options.indexOf(current).coerceAtLeast(0)

        AlertDialog.Builder(this)
            .setTitle("选择默认投币数量")
            .setSingleChoiceItems(labels, checkedIdx) { dialog, which ->
                val amount = options[which]
                SharedPreferencesManager.setDefaultTipAmount(amount)
                binding.tvTipAmount.text = "${amount}币"
                dialog.dismiss()
                Toast.makeText(this, "已设置默认投币${amount}币", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("取消", null)
            .show()
    }
}
