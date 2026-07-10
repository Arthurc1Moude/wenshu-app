package com.wenshu.app.ui.settings

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.wenshu.app.data.SharedPreferencesManager
import com.wenshu.app.data.api.RetrofitClient
import com.wenshu.app.data.api.safeApiCall
import com.wenshu.app.data.model.ChangePasswordRequest
import com.wenshu.app.data.model.SendCodeRequest
import com.wenshu.app.databinding.ActivityChangePasswordBinding
import com.wenshu.app.util.PasswordValidator
import kotlinx.coroutines.launch

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private var countDownTimer: CountDownTimer? = null
    private var hasPhone = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        val user = SharedPreferencesManager.getUser()
        hasPhone = user?.phone != null && user.phone!!.isNotEmpty()

        if (hasPhone) {
            binding.tvPhoneSectionHint.visibility = View.VISIBLE
            binding.tilPhone.visibility = View.VISIBLE
            binding.layoutCodeSection.visibility = View.VISIBLE
            binding.etPhone.setText(user?.phone)
            binding.tilOldPassword.visibility = View.GONE
            binding.tvOldPasswordHint.visibility = View.GONE
        } else {
            binding.tvPhoneSectionHint.visibility = View.GONE
            binding.tilPhone.visibility = View.GONE
            binding.layoutCodeSection.visibility = View.GONE
            binding.tilOldPassword.visibility = View.VISIBLE
            binding.tvOldPasswordHint.visibility = View.VISIBLE
        }
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { finish() }

        binding.btnSendCode.setOnClickListener {
            if (hasPhone) {
                sendVerificationCode(binding.etPhone.text.toString().trim())
            }
        }

        binding.btnChangePassword.setOnClickListener {
            performChangePassword()
        }
    }

    private fun sendVerificationCode(phone: String) {
        if (!PasswordValidator.isValidPhone(phone)) {
            showError("请输入正确的手机号")
            return
        }

        binding.btnSendCode.isEnabled = false
        lifecycleScope.launch {
            val result = safeApiCall {
                RetrofitClient.apiService.sendVerificationCode(
                    SendCodeRequest(phone, "change_password")
                )
            }
            result.onSuccess { response ->
                Toast.makeText(this@ChangePasswordActivity, response.message, Toast.LENGTH_SHORT).show()
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

    private fun performChangePassword() {
        val newPassword = binding.etNewPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (newPassword.isEmpty()) {
            showError("请输入新密码")
            return
        }
        if (confirmPassword.isEmpty()) {
            showError("请确认新密码")
            return
        }
        if (newPassword != confirmPassword) {
            showError("两次密码输入不一致")
            return
        }

        val pwdValidation = PasswordValidator.validate(newPassword)
        if (!pwdValidation.valid) {
            showError(pwdValidation.message ?: "密码不符合要求")
            return
        }

        var oldPassword: String? = null
        var phone: String? = null
        var code: String? = null

        if (hasPhone) {
            phone = binding.etPhone.text.toString().trim()
            code = binding.etCode.text.toString().trim()
            if (!PasswordValidator.isValidPhone(phone)) {
                showError("请输入正确的手机号")
                return
            }
            if (code.length != 6) {
                showError("请输入6位验证码")
                return
            }
        } else {
            oldPassword = binding.etOldPassword.text.toString().trim()
            if (oldPassword.isEmpty()) {
                showError("请输入原密码")
                return
            }
        }

        binding.btnChangePassword.isEnabled = false
        binding.btnChangePassword.text = "修改中..."
        binding.tvError.visibility = View.GONE

        lifecycleScope.launch {
            val result = safeApiCall {
                RetrofitClient.apiService.changePassword(
                    ChangePasswordRequest(
                        oldPassword = oldPassword,
                        newPassword = newPassword,
                        confirmPassword = confirmPassword,
                        phone = phone,
                        code = code
                    )
                )
            }
            result.onSuccess {
                Toast.makeText(this@ChangePasswordActivity, "密码修改成功，请重新登录", Toast.LENGTH_SHORT).show()
                performLogout()
            }.onFailure { e ->
                showError(e.message ?: "修改失败")
                binding.btnChangePassword.isEnabled = true
                binding.btnChangePassword.text = "确认修改"
            }
        }
    }

    private fun performLogout() {
        SharedPreferencesManager.logout()
        val intent = android.content.Intent(this, com.wenshu.app.ui.auth.LoginActivity::class.java)
        intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
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
