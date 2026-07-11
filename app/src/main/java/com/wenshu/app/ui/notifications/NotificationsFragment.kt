package com.wenshu.app.ui.notifications

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.wenshu.app.MainActivity
import com.wenshu.app.R
import com.wenshu.app.databinding.FragmentNotificationsBinding
import com.wenshu.app.ui.adapters.NotificationAdapter
import com.wenshu.app.ui.chat.CreateGroupActivity
import com.wenshu.app.ui.chat.FriendsActivity
import com.wenshu.app.ui.chat.JoinGroupActivity

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NotificationsViewModel by viewModels()
    private lateinit var notificationAdapter: NotificationAdapter
    private lateinit var swipeRefresh: SwipeRefreshLayout
    private var isVisibleToUser = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        swipeRefresh = SwipeRefreshLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setColorSchemeColors(ContextCompat.getColor(requireContext(), R.color.seal))
        }

        val parent = binding.recyclerNotifications.parent as? ViewGroup
        val index = parent?.indexOfChild(binding.recyclerNotifications) ?: 0
        parent?.removeView(binding.recyclerNotifications)
        swipeRefresh.addView(binding.recyclerNotifications)
        parent?.addView(swipeRefresh, index)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSwipeRefresh()
        setupAddButton()
        observeData()
    }

    private fun setupAddButton() {
        binding.btnAddChat.setOnClickListener { v ->
            val popup = PopupMenu(requireContext(), v, Gravity.END or Gravity.TOP)
            popup.menu.add(0, 1, 0, "创建群聊")
            popup.menu.add(0, 2, 1, "加入群聊")
            popup.menu.add(0, 3, 2, "我的好友")
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    1 -> startActivity(Intent(requireContext(), CreateGroupActivity::class.java))
                    2 -> startActivity(Intent(requireContext(), JoinGroupActivity::class.java))
                    3 -> startActivity(Intent(requireContext(), FriendsActivity::class.java))
                }
                true
            }
            popup.show()
        }
    }

    override fun onResume() {
        super.onResume()
        isVisibleToUser = true
        (activity as? MainActivity)?.clearUnreadBadge()
        viewModel.refresh()
        viewModel.markAllRead()
    }

    override fun onPause() {
        super.onPause()
        isVisibleToUser = false
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(
            onItemClick = { }
        )
        binding.recyclerNotifications.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = notificationAdapter
            itemAnimator = null
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    private fun observeData() {
        viewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            notificationAdapter.submitList(notifications)
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            swipeRefresh.isRefreshing = isLoading
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
