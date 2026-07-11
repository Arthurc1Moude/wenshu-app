package com.wenshu.app.ui.settings

import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.wenshu.app.R
import com.wenshu.app.data.repository.PostRepository
import com.wenshu.app.databinding.ActivitySignInBinding
import kotlinx.coroutines.launch

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private val repository = PostRepository.getInstance()
    private var currentCoins = 0
    private var currentConsecutive = 0
    private var isSignedToday = false

    private val rewards = listOf(
        "10文書币",
        "20文書币",
        "随机奖励",
        "随机奖励",
        "随机奖励",
        "随机奖励",
        "50币+7天VIP"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnSign.setOnClickListener { performSignIn() }

        loadUserData()
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            val result = repository.refreshCurrentUser()
            result.onSuccess { user ->
                currentCoins = user.wenshuCoin
                currentConsecutive = user.consecutiveSignDays
                isSignedToday = user.isSignedInToday
                updateUI()
            }.onFailure {
                Toast.makeText(this@SignInActivity, "加载失败", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUI() {
        binding.tvCurrentCoin.text = currentCoins.toString()
        binding.tvConsecutiveDays.text = "已连续签到 $currentConsecutive 天"
        if (isSignedToday) {
            binding.btnSign.text = "今日已签到"
            binding.btnSign.isEnabled = false
            binding.btnSign.alpha = 0.6f
            binding.tvRewardDesc.text = "明天继续签到领取更多奖励哦~"
        } else {
            val nextDay = (currentConsecutive % 7) + 1
            binding.btnSign.text = "立即签到（第${nextDay}天）"
            binding.btnSign.isEnabled = true
            binding.btnSign.alpha = 1f
            binding.tvRewardDesc.text = "今日奖励：${rewards[nextDay - 1]}"
            buildCalendar(nextDay - 1)
        }
    }

    private fun buildCalendar(highlightIndex: Int) {
        binding.gridSignCalendar.removeAllViews()
        val dayLabels = listOf("第1天", "第2天", "第3天", "第4天", "第5天", "第6天", "第7天")
        val dp = resources.displayMetrics.density
        for (i in rewards.indices) {
            val cell = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                val size = (72 * dp).toInt()
                layoutParams = GridLayout.LayoutParams().apply {
                    width = size
                    height = size
                    setMargins(6, 6, 6, 6)
                }
                setBackgroundResource(if (i == highlightIndex) R.drawable.bg_seal else R.drawable.bg_tag)
                val pad = (8 * dp).toInt()
                setPadding(pad, pad, pad, pad)
            }

            val dayTv = TextView(this).apply {
                text = dayLabels[i]
                textSize = 11f
                setTextColor(
                    if (i == highlightIndex) ContextCompat.getColor(this@SignInActivity, R.color.on_primary)
                    else ContextCompat.getColor(this@SignInActivity, R.color.text_secondary)
                )
                gravity = Gravity.CENTER
                typeface = Typeface.SERIF
            }

            val rewardTv = TextView(this).apply {
                text = rewards[i]
                textSize = 9f
                setTextColor(
                    if (i == highlightIndex) ContextCompat.getColor(this@SignInActivity, R.color.on_primary)
                    else ContextCompat.getColor(this@SignInActivity, R.color.text_primary)
                )
                gravity = Gravity.CENTER
                typeface = Typeface.SERIF
                setPadding(0, 4, 0, 0)
            }

            cell.addView(dayTv)
            cell.addView(rewardTv)
            binding.gridSignCalendar.addView(cell)
        }
    }

    private fun performSignIn() {
        binding.btnSign.isEnabled = false
        lifecycleScope.launch {
            val result = repository.dailySignIn()
            result.onSuccess { resp ->
                Toast.makeText(this@SignInActivity, "签到成功！获得 ${resp.rewardDesc}", Toast.LENGTH_LONG).show()
                currentCoins = resp.totalCoins
                currentConsecutive = resp.consecutiveDays
                isSignedToday = true
                updateUI()
                setResult(RESULT_OK)
            }.onFailure {
                binding.btnSign.isEnabled = true
                val msg = it.message ?: "签到失败"
                Toast.makeText(this@SignInActivity, if (msg.contains("今日已签到")) "今日已签到" else msg, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
