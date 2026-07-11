package com.wenshu.app.ui.chat

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.wenshu.app.data.SharedPreferencesManager
import com.wenshu.app.data.api.RetrofitClient
import com.wenshu.app.data.api.safeApiCall
import com.wenshu.app.data.model.ChatMessage
import com.wenshu.app.data.model.Message
import com.wenshu.app.data.model.SendMessageRequest
import com.wenshu.app.databinding.ActivityChatBinding
import com.wenshu.app.ui.adapters.ChatMessageAdapter
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var messageAdapter: ChatMessageAdapter
    private var conversationId: String? = null
    private var conversationTitle: String? = null
    private var conversationType: String = "private"
    private var otherUserId: String? = null
    private val messages = mutableListOf<ChatMessage>()
    private val me by lazy { SharedPreferencesManager.getUser() }
    private val pollHandler = Handler(Looper.getMainLooper())
    private var pollRunnable: Runnable? = null
    private var lastMessageTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        conversationId = intent.getStringExtra("conversationId")
        conversationTitle = intent.getStringExtra("conversationTitle")
        conversationType = intent.getStringExtra("conversationType") ?: "private"
        otherUserId = intent.getStringExtra("otherUserId")

        if (conversationId == null && otherUserId != null) {
            createOrGetPrivateConversation()
        } else if (conversationId == null) {
            Toast.makeText(this, "会话信息错误", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupMessages()
        setupInput()
        loadMessages()
    }

    override fun onResume() {
        super.onResume()
        startPolling()
    }

    override fun onPause() {
        super.onPause()
        stopPolling()
    }

    private fun setupToolbar() {
        binding.tvTitle.text = conversationTitle ?: "聊天"
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

    private fun createOrGetPrivateConversation() {
        lifecycleScope.launch {
            try {
                val result = safeApiCall {
                    RetrofitClient.apiService.createPrivateConversation(otherUserId!!)
                }
                result.onSuccess { conv ->
                    conversationId = conv.id
                    conversationTitle = conv.name ?: conversationTitle
                    if (conversationTitle == null && conv.otherUser != null) {
                        conversationTitle = conv.otherUser.username
                    }
                    runOnUiThread {
                        setupToolbar()
                        loadMessages()
                    }
                }.onFailure { e ->
                    runOnUiThread {
                        Toast.makeText(this@ChatActivity, "无法创建会话: ${e.message}", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@ChatActivity, "网络错误", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun loadMessages() {
        val convId = conversationId ?: return
        lifecycleScope.launch {
            try {
                val result = safeApiCall {
                    RetrofitClient.apiService.getMessages(convId)
                }
                result.onSuccess { msgList ->
                    val chatMsgs = msgList.map { it.toChatMessage(me?.id ?: "") }
                    messages.clear()
                    messages.addAll(chatMsgs)
                    if (msgList.isNotEmpty()) {
                        lastMessageTime = msgList.last().createdAt
                    }
                    runOnUiThread { updateMessages() }
                    safeApiCall { RetrofitClient.apiService.markConversationRead(convId) }
                }.onFailure { e ->
                    runOnUiThread {
                        Toast.makeText(this@ChatActivity, "加载消息失败: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@ChatActivity, "网络错误", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun sendMessage() {
        val content = binding.etMessage.text.toString().trim()
        if (content.isBlank()) return
        val convId = conversationId ?: return
        binding.etMessage.text?.clear()

        val tempMsg = ChatMessage(
            id = "temp_${System.currentTimeMillis()}",
            chatId = convId,
            senderId = me?.id ?: "",
            content = content,
            isMine = true,
            senderAvatar = me?.avatar,
            senderName = me?.username
        )
        messages.add(tempMsg)
        updateMessages()

        lifecycleScope.launch {
            try {
                val result = safeApiCall {
                    RetrofitClient.apiService.sendMessage(convId, SendMessageRequest(content))
                }
                result.onSuccess { msg ->
                    val idx = messages.indexOfFirst { it.id == tempMsg.id }
                    if (idx >= 0) {
                        messages[idx] = msg.toChatMessage(me?.id ?: "")
                    }
                    lastMessageTime = msg.createdAt
                    runOnUiThread { updateMessages() }
                }.onFailure { e ->
                    runOnUiThread {
                        Toast.makeText(this@ChatActivity, "发送失败: ${e.message}", Toast.LENGTH_SHORT).show()
                        val failIdx = messages.indexOfFirst { it.id == tempMsg.id }
                        if (failIdx >= 0) messages.removeAt(failIdx)
                        updateMessages()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@ChatActivity, "发送失败", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun startPolling() {
        stopPolling()
        val runnable = object : Runnable {
            override fun run() {
                pollNewMessages()
                pollHandler.postDelayed(this, 3000)
            }
        }
        pollRunnable = runnable
        pollHandler.postDelayed(runnable, 1500)
    }

    private fun stopPolling() {
        pollRunnable?.let { pollHandler.removeCallbacks(it) }
        pollRunnable = null
    }

    private fun pollNewMessages() {
        val convId = conversationId ?: return
        lifecycleScope.launch {
            try {
                val result = safeApiCall {
                    RetrofitClient.apiService.getMessages(convId)
                }
                result.onSuccess { msgList ->
                    val newMsgs = msgList.filter { it.createdAt > lastMessageTime }
                    if (newMsgs.isNotEmpty()) {
                        val chatMsgs = newMsgs.map { it.toChatMessage(me?.id ?: "") }
                        messages.addAll(chatMsgs)
                        lastMessageTime = newMsgs.last().createdAt
                        runOnUiThread { updateMessages() }
                        safeApiCall { RetrofitClient.apiService.markConversationRead(convId) }
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun updateMessages() {
        messageAdapter.submitList(messages.toList())
        if (messages.isNotEmpty()) {
            binding.recyclerMessages.postDelayed({
                binding.recyclerMessages.smoothScrollToPosition(messages.size - 1)
            }, 100)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopPolling()
    }
}

fun Message.toChatMessage(myId: String): ChatMessage {
    return ChatMessage(
        id = this.id,
        chatId = this.conversationId,
        senderId = this.senderId,
        content = this.content,
        createdAt = this.createdAt,
        isMine = this.senderId == myId,
        senderAvatar = this.senderAvatar ?: this.sender?.avatar,
        senderName = this.senderName ?: this.sender?.username
    )
}
