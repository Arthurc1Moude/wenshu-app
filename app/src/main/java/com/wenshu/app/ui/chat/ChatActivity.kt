package com.wenshu.app.ui.chat

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.wenshu.app.data.SharedPreferencesManager
import com.wenshu.app.data.model.ChatMessage
import com.wenshu.app.databinding.ActivityChatBinding
import com.wenshu.app.ui.adapters.ChatMessageAdapter
import java.util.UUID

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var messageAdapter: ChatMessageAdapter
    private var userId: String? = null
    private var username: String? = null
    private val messages = mutableListOf<ChatMessage>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userId = intent.getStringExtra("user_id")
        username = intent.getStringExtra("username")

        if (userId == null) {
            finish()
            return
        }

        setupToolbar()
        setupMessages()
        setupInput()
        loadInitialMessages()
    }

    private fun setupToolbar() {
        binding.tvTitle.text = username ?: "聊天"
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupMessages() {
        messageAdapter = ChatMessageAdapter()
        binding.recyclerMessages.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity).apply {
                stackFromEnd = true
            }
            adapter = messageAdapter
        }
    }

    private fun setupInput() {
        binding.btnSend.setOnClickListener { sendMessage() }
        binding.etMessage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }
    }

    private fun loadInitialMessages() {
        val me = SharedPreferencesManager.getUser()
        val welcomeMsg = ChatMessage(
            id = UUID.randomUUID().toString(),
            chatId = userId ?: "",
            senderId = userId ?: "",
            content = "你好，我是${username ?: "用户"}",
            isMine = false,
            senderAvatar = null,
            senderName = username
        )
        messages.add(welcomeMsg)
        updateMessages()
    }

    private fun sendMessage() {
        val content = binding.etMessage.text.toString().trim()
        if (content.isBlank()) return

        val me = SharedPreferencesManager.getUser()
        val message = ChatMessage(
            id = UUID.randomUUID().toString(),
            chatId = userId ?: "",
            senderId = me?.id ?: "",
            content = content,
            isMine = true,
            senderAvatar = me?.avatar,
            senderName = me?.username
        )
        messages.add(message)
        updateMessages()
        binding.etMessage.text?.clear()
        simulateReply(content)
    }

    private fun simulateReply(myContent: String) {
        binding.recyclerMessages.postDelayed({
            val replies = listOf(
                "好的",
                "嗯嗯",
                "明白了",
                "有意思",
                "说得对！",
                "这个想法不错"
            )
            val replyContent = replies.random()
            val reply = ChatMessage(
                id = UUID.randomUUID().toString(),
                chatId = userId ?: "",
                senderId = userId ?: "",
                content = replyContent,
                isMine = false,
                senderAvatar = null,
                senderName = username
            )
            messages.add(reply)
            updateMessages()
        }, 1000)
    }

    private fun updateMessages() {
        messageAdapter.submitList(messages.toList())
        if (messages.isNotEmpty()) {
            binding.recyclerMessages.postDelayed({
                binding.recyclerMessages.smoothScrollToPosition(messages.size - 1)
            }, 100)
        }
    }
}
