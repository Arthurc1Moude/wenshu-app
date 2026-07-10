package com.wenshu.app.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.wenshu.app.MainActivity
import com.wenshu.app.data.SharedPreferencesManager
import com.wenshu.app.data.api.RetrofitClient
import com.wenshu.app.data.api.safeApiCall
import com.wenshu.app.data.model.RegisterRequest
import com.wenshu.app.databinding.ActivityRegisterBinding
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            performRegister()
        }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun performRegister() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (username.isEmpty()) {
            showError("请输入用户名")
            return
        }
        if (username.length < 2) {
            showError("用户名至少2个字符")
            return
        }
        if (password.isEmpty()) {
            showError("请输入密码")
            return
        }
        if (password.length < 6) {
            showError("密码至少6个字符")
            return
        }
        if (password != confirmPassword) {
            showError("两次密码输入不一致")
            return
        }

        binding.tvError.visibility = View.GONE
        binding.btnRegister.isEnabled = false
        binding.btnRegister.text = "注册中..."

        lifecycleScope.launch {
            val result = safeApiCall {
                RetrofitClient.apiService.register(RegisterRequest(username, password))
            }
            result.onSuccess { response ->
                SharedPreferencesManager.saveAuth(response.token, response.user)
                Toast.makeText(this@RegisterActivity, "注册成功！", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }.onFailure { e ->
                showError(e.message ?: "注册失败，请稍后重试")
                binding.btnRegister.isEnabled = true
                binding.btnRegister.text = "注 册"
            }
        }
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
