package com.wenshu.app.ui.settings

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.wenshu.app.data.repository.PostRepository
import com.wenshu.app.databinding.ActivityRedeemBinding
import kotlinx.coroutines.launch

class RedeemActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRedeemBinding
    private val repository = PostRepository.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRedeemBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
            finish()
        }

        binding.btnRedeem.setOnClickListener {
            redeemCode()
        }
    }

    private fun redeemCode() {
        val code = binding.etCode.text.toString().trim()
        if (code.isEmpty()) {
            binding.tvError.text = "请输入兑换码"
            binding.tvError.visibility = View.VISIBLE
            return
        }

        binding.tvError.visibility = View.GONE
        binding.btnRedeem.isEnabled = false
        binding.btnRedeem.text = "兑换中..."

        lifecycleScope.launch {
            val result = repository.redeemCode(code)
            result.onSuccess { data ->
                val message = buildString {
                    append("兑换成功！")
                    if (data.vipGranted) {
                        append(" VIP已激活")
                        data.vipExpiresAt?.let { append("，有效期至${com.wenshu.app.util.TimeUtils.formatDate(it)}") }
                    } else if (data.coins > 0) {
                        append(" 获得${data.coins}文书币")
                    }
                }
                Toast.makeText(this@RedeemActivity, message, Toast.LENGTH_LONG).show()
                binding.etCode.text?.clear()
                binding.btnRedeem.text = "兑 换"
                binding.btnRedeem.isEnabled = true
            }.onFailure { error ->
                binding.tvError.text = error.message ?: "兑换失败，请检查兑换码"
                binding.tvError.visibility = View.VISIBLE
                binding.btnRedeem.text = "兑 换"
                binding.btnRedeem.isEnabled = true
            }
        }
    }
}
