package com.wenshu.app.ui.notifications

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.wenshu.app.R
import com.wenshu.app.databinding.FragmentNotificationsBinding
import com.wenshu.app.ui.adapters.NotificationAdapter

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: NotificationsViewModel by viewModels()
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTabs()
        setupRecyclerView()
        observeData()
    }

    private fun setupTabs() {
        val tabs = listOf(
            "all" to getString(R.string.tab_all),
            "likes" to getString(R.string.tab_likes),
            "comments" to getString(R.string.tab_comments),
            "follows" to getString(R.string.tab_follows),
            "mentions" to getString(R.string.tab_mentions)
        )
        tabs.forEach { (key, label) ->
            val tv = TextView(requireContext()).apply {
                text = label
                textSize = 16f
                setTextColor(ContextCompat.getColor(requireContext(), if (key == "all") R.color.text_primary else R.color.text_secondary))
                val params = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                params.marginEnd = resources.getDimensionPixelSize(R.dimen.spacing_lg)
                layoutParams = params
                setOnClickListener { selectTab(key) }
                tag = "tab_$key"
                typeface = if (key == "all") android.graphics.Typeface.DEFAULT_BOLD else android.graphics.Typeface.DEFAULT
            }
            binding.tabLayout.addView(tv)
        }
    }

    private fun selectTab(key: String) {
        for (i in 0 until binding.tabLayout.childCount) {
            val child = binding.tabLayout.getChildAt(i) as? TextView ?: continue
            val isSelected = child.tag == "tab_$key"
            child.setTextColor(ContextCompat.getColor(requireContext(), if (isSelected) R.color.text_primary else R.color.text_secondary))
            child.typeface = if (isSelected) android.graphics.Typeface.DEFAULT_BOLD else android.graphics.Typeface.DEFAULT
        }
        viewModel.selectTab(key)
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(
            notifications = emptyList(),
            onItemClick = { }
        )
        binding.recyclerNotifications.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = notificationAdapter
            itemAnimator = null
        }
    }

    private fun observeData() {
        viewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            notificationAdapter.updateNotifications(notifications)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
