package com.wenshu.app.ui.settings

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.wenshu.app.data.SharedPreferencesManager
import com.wenshu.app.data.repository.PostRepository
import com.wenshu.app.databinding.ActivityVipBinding
import com.wenshu.app.util.TimeUtils
import kotlinx.coroutines.launch

class VipActivity : AppCompatActivity() {

    private lateinit var binding: ActivityVipBinding
    private val repository = PostRepository.getInstance()
    private var selectedPlan: String = "monthly"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVipBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        updateVipStatus()
    }

    private fun updateVipStatus() {
        val user = SharedPreferencesManager.getUser()
        if (user != null && user.isVip) {
            val expiresText = user.vipExpiresAt?.let {
                "会员有效期至 ${TimeUtils.formatDate(it)}"
            } ?: "会员已开通"
            binding.btnSubscribe.text = expiresText
            binding.btnSubscribe.isEnabled = false
            binding.planMonthly.isClickable = false
            binding.planYearly.isClickable = false
        } else {
            binding.btnSubscribe.text = "立即开通"
            binding.btnSubscribe.isEnabled = true
            selectPlan("monthly")
        }
    }

    private fun selectPlan(plan: String) {
        selectedPlan = plan
        binding.planMonthly.alpha = if (plan == "monthly") 1.0f else 0.6f
        binding.planYearly.alpha = if (plan == "yearly") 1.0f else 0.6f
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.planMonthly.setOnClickListener {
            selectPlan("monthly")
        }

        binding.planYearly.setOnClickListener {
            selectPlan("yearly")
        }

        binding.btnSubscribe.setOnClickListener {
            purchaseVip()
        }
    }

    private fun purchaseVip() {
        val user = SharedPreferencesManager.getUser()
        if (user != null && user.isVip) return

        lifecycleScope.launch {
            binding.btnSubscribe.isEnabled = false
            binding.btnSubscribe.text = "处理中..."

            val result = repository.purchaseVip()
            result.onSuccess { response ->
                SharedPreferencesManager.updateUser(response.user)
                Toast.makeText(this@VipActivity, "开通成功！欢迎加入文书会", Toast.LENGTH_LONG).show()
                updateVipStatus()
            }.onFailure { error ->
                Toast.makeText(this@VipActivity, error.message ?: "开通失败，请重试", Toast.LENGTH_SHORT).show()
                binding.btnSubscribe.isEnabled = true
                binding.btnSubscribe.text = "立即开通"
            }
        }
    }
}
