package com.wenshu.app.ui.settings

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.wenshu.app.MainActivity
import com.wenshu.app.data.SharedPreferencesManager
import com.wenshu.app.data.api.RetrofitClient
import com.wenshu.app.data.api.safeApiCall
import com.wenshu.app.data.model.BindPhoneRequest
import com.wenshu.app.data.model.SendCodeRequest
import com.wenshu.app.databinding.ActivityBindPhoneBinding
import com.wenshu.app.util.PasswordValidator
import kotlinx.coroutines.launch

class BindPhoneActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBindPhoneBinding
    private var countDownTimer: CountDownTimer? = null
    private var isFromRegister = false

    companion object {
        const val EXTRA_FROM_REGISTER = "from_register"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBindPhoneBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isFromRegister = intent.getBooleanExtra(EXTRA_FROM_REGISTER, false)
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.tvSkip.setOnClickListener {
            handleSkip()
        }

        binding.btnSendCode.setOnClickListener {
            sendVerificationCode()
        }

        binding.btnBind.setOnClickListener {
            performBind()
        }
    }

    private fun handleSkip() {
        if (isFromRegister) {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
        finish()
    }

    private fun sendVerificationCode() {
        val phone = binding.etPhone.text.toString().trim()
        if (!PasswordValidator.isValidPhone(phone)) {
            showError("请输入正确的手机号")
            return
        }

        binding.btnSendCode.isEnabled = false
        lifecycleScope.launch {
            val result = safeApiCall {
                RetrofitClient.apiService.sendVerificationCode(
                    SendCodeRequest(phone, "bind_phone")
                )
            }
            result.onSuccess { response ->
                Toast.makeText(this@BindPhoneActivity, response.message, Toast.LENGTH_SHORT).show()
                response.devCode?.let { code ->
                    binding.etCode.setText(code)
                }
                startCountDown()
            }.onFailure { e ->
                showError(e.message ?: "发送失败")
                binding.btnSendCode.isEnabled = true
            }
        }
    }

    private fun startCountDown() {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.btnSendCode.text = "${millisUntilFinished / 1000}s"
                binding.btnSendCode.isEnabled = false
            }

            override fun onFinish() {
                binding.btnSendCode.text = "获取验证码"
                binding.btnSendCode.isEnabled = true
            }
        }.start()
    }

    private fun performBind() {
        val phone = binding.etPhone.text.toString().trim()
        val code = binding.etCode.text.toString().trim()

        if (!PasswordValidator.isValidPhone(phone)) {
            showError("请输入正确的手机号")
            return
        }
        if (code.length != 6) {
            showError("请输入6位验证码")
            return
        }

        binding.btnBind.isEnabled = false
        binding.btnBind.text = "绑定中..."
        binding.tvError.visibility = View.GONE

        lifecycleScope.launch {
            val result = safeApiCall {
                RetrofitClient.apiService.bindPhone(BindPhoneRequest(phone, code))
            }
            result.onSuccess {
                Toast.makeText(this@BindPhoneActivity, "绑定成功", Toast.LENGTH_SHORT).show()
                refreshUser()
            }.onFailure { e ->
                showError(e.message ?: "绑定失败")
                binding.btnBind.isEnabled = true
                binding.btnBind.text = "绑 定"
            }
        }
    }

    private suspend fun refreshUser() {
        val result = safeApiCall { RetrofitClient.apiService.getCurrentUser() }
        result.onSuccess { user ->
            SharedPreferencesManager.updateUser(user)
            if (isFromRegister) {
                val intent = Intent(this@BindPhoneActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            finish()
        }.onFailure {
            handleSkip()
        }
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
