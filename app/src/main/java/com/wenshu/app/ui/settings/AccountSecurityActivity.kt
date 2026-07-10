package com.wenshu.app.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.wenshu.app.data.SharedPreferencesManager
import com.wenshu.app.databinding.ActivityAccountSecurityBinding

class AccountSecurityActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccountSecurityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAccountSecurityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        updatePhoneStatus()
    }

    private fun updatePhoneStatus() {
        val user = SharedPreferencesManager.getUser()
        if (user?.phone != null && user.phone.isNotEmpty()) {
            val maskedPhone = user.phone.substring(0, 3) + "****" + user.phone.substring(7)
            binding.tvPhoneStatus.text = maskedPhone
        } else {
            binding.tvPhoneStatus.text = "未绑定"
        }
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { finish() }

        binding.itemBindPhone.setOnClickListener {
            startActivity(Intent(this, BindPhoneActivity::class.java))
        }

        binding.itemChangePassword.setOnClickListener {
            startActivity(Intent(this, ChangePasswordActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        updatePhoneStatus()
    }
}
