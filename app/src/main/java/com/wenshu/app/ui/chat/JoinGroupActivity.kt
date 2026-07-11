package com.wenshu.app.ui.chat

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.wenshu.app.R
import com.wenshu.app.data.api.RetrofitClient
import com.wenshu.app.data.model.JoinGroupRequest
import com.wenshu.app.databinding.ActivityJoinGroupBinding
import kotlinx.coroutines.launch

class JoinGroupActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJoinGroupBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoinGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnJoinCode.setOnClickListener { joinByCode() }
        binding.btnJoinNumber.setOnClickListener { joinByNumber() }
    }

    private fun joinByCode() {
        val code = binding.etJoinCode.text.toString().trim()
        if (code.length < 4) {
            Toast.makeText(this, "请输入有效的邀请码", Toast.LENGTH_SHORT).show()
            return
        }
        performJoin(JoinGroupRequest(code = code))
    }

    private fun joinByNumber() {
        val num = binding.etGroupNumber.text.toString().trim()
        if (num.length < 6) {
            Toast.makeText(this, "请输入有效的群号", Toast.LENGTH_SHORT).show()
            return
        }
        performJoin(JoinGroupRequest(groupNumber = num))
    }

    private fun performJoin(req: JoinGroupRequest) {
        binding.btnJoinCode.isEnabled = false
        binding.btnJoinNumber.isEnabled = false
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.apiService.joinGroup(req)
                if (resp.alreadyJoined == true) {
                    Toast.makeText(this@JoinGroupActivity, "你已经在群里了", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this@JoinGroupActivity, "成功加入群聊「${resp.name}」", Toast.LENGTH_LONG).show()
                }
                val intent = Intent(this@JoinGroupActivity, ChatActivity::class.java).apply {
                    putExtra("conversationId", resp.id)
                    putExtra("conversationTitle", resp.name)
                    putExtra("conversationType", "group")
                }
                startActivity(intent)
                setResult(RESULT_OK)
                finish()
            } catch (e: Exception) {
                binding.btnJoinCode.isEnabled = true
                binding.btnJoinNumber.isEnabled = true
                Toast.makeText(this@JoinGroupActivity, "加入失败：${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
