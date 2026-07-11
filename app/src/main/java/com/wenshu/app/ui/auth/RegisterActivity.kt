package com.wenshu.app.ui.auth

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.wenshu.app.MainActivity
import com.wenshu.app.R
import com.wenshu.app.data.SharedPreferencesManager
import com.wenshu.app.data.api.ApiException
import com.wenshu.app.data.api.RetrofitClient
import com.wenshu.app.data.api.safeApiCall
import com.wenshu.app.data.model.RegisterRequest
import com.wenshu.app.databinding.ActivityRegisterBinding
import com.wenshu.app.ui.settings.BindPhoneActivity
import com.wenshu.app.util.PasswordValidator
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        setupTextWatchers()
    }

    private fun setupTextWatchers() {
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validatePasswordRealtime(s.toString())
            }
        })

        binding.etUsername.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.layoutSuggestions.visibility = View.GONE
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun validatePasswordRealtime(password: String) {
        if (password.isEmpty()) {
            binding.tvPasswordHint.setTextColor(ContextCompat.getColor(this, R.color.text_tertiary))
            return
        }
        val result = PasswordValidator.validate(password)
        if (result.valid) {
            binding.tvPasswordHint.setTextColor(ContextCompat.getColor(this, R.color.text_tertiary))
        } else {
            binding.tvPasswordHint.setTextColor(ContextCompat.getColor(this, R.color.seal))
        }
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { finish() }

        binding.btnRegister.setOnClickListener {
            performRegister()
        }

        binding.tvLogin.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        finish()
    }

    private fun performRegister() {
        val username = binding.etUsername.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        binding.layoutSuggestions.visibility = View.GONE

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

        val pwdValidation = PasswordValidator.validate(password)
        if (!pwdValidation.valid) {
            showError(pwdValidation.message ?: "密码不符合要求")
            return
        }

        if (confirmPassword.isEmpty()) {
            showError("请确认密码")
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
                
                val intent = Intent(this@RegisterActivity, BindPhoneActivity::class.java)
                intent.putExtra(BindPhoneActivity.EXTRA_FROM_REGISTER, true)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }.onFailure { e ->
                binding.btnRegister.isEnabled = true
                binding.btnRegister.text = "注 册"
                
                if (e is ApiException) {
                    when (e.code) {
                        "USERNAME_TAKEN" -> {
                            showError(e.message ?: "该用户名已被注册")
                            e.suggestions?.let { showUsernameSuggestions(it) }
                        }
                        "PHONE_TAKEN" -> {
                            showErrorWithLoginOption(e.message ?: "该手机号已注册账号")
                        }
                        else -> {
                            showError(e.message ?: "注册失败，请稍后重试")
                        }
                    }
                } else {
                    showError(e.message ?: "注册失败，请稍后重试")
                }
            }
        }
    }

    private fun showUsernameSuggestions(suggestions: List<String>) {
        binding.layoutSuggestionTags.removeAllViews()
        
        val outerLayout = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            gravity = Gravity.START
        }

        var currentRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        suggestions.forEachIndexed { index, suggestion ->
            val chip = TextView(this).apply {
                text = suggestion
                textSize = 13f
                setTextColor(ContextCompat.getColor(this@RegisterActivity, R.color.ink))
                setTypeface(typeface, Typeface.NORMAL)
                setPadding(dpToPx(16), dpToPx(8), dpToPx(16), dpToPx(8))
                setBackgroundResource(R.drawable.bg_suggestion_chip)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginEnd = dpToPx(8)
                    bottomMargin = dpToPx(8)
                }
                setOnClickListener {
                    binding.etUsername.setText(suggestion)
                    binding.etUsername.setSelection(suggestion.length)
                    binding.layoutSuggestions.visibility = View.GONE
                    binding.tvError.visibility = View.GONE
                }
            }
            currentRow.addView(chip)
        }
        
        outerLayout.addView(currentRow)
        binding.layoutSuggestionTags.addView(outerLayout)

        val goLoginText = TextView(this).apply {
            text = "已有账号？直接登录"
            textSize = 13f
            setTextColor(ContextCompat.getColor(this@RegisterActivity, R.color.seal))
            setPadding(0, dpToPx(8), 0, 0)
            setOnClickListener { navigateToLogin() }
        }
        binding.layoutSuggestionTags.addView(goLoginText)
        
        binding.layoutSuggestions.visibility = View.VISIBLE
    }

    private fun showErrorWithLoginOption(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        
        binding.layoutSuggestionTags.removeAllViews()
        val goLoginText = TextView(this).apply {
            text = "立即前往登录 →"
            textSize = 14f
            setTextColor(ContextCompat.getColor(this@RegisterActivity, R.color.seal))
            setTypeface(typeface, Typeface.BOLD)
            setPadding(0, dpToPx(4), 0, 0)
            setOnClickListener { navigateToLogin() }
        }
        binding.layoutSuggestionTags.addView(goLoginText)
        binding.layoutSuggestions.visibility = View.VISIBLE
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
