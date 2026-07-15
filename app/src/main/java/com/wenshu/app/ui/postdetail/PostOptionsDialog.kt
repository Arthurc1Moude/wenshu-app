package com.wenshu.app.ui.postdetail

import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wenshu.app.R
import com.wenshu.app.data.model.Post

class PostOptionsDialog : BottomSheetDialogFragment() {

    private var post: Post? = null
    private var isCollected: Boolean = false
    private var isBlocked: Boolean = false
    private var isOwnPost: Boolean = false
    private var listener: PostOptionsListener? = null

    interface PostOptionsListener {
        fun onShare()
        fun onCopyLink()
        fun onCollect()
        fun onReport()
        fun onBlock()
        fun onDelete()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        val view = layoutInflater.inflate(R.layout.dialog_post_options, null)
        dialog.setContentView(view)

        val optionShare = view.findViewById<LinearLayout>(R.id.option_share)
        val optionCopyLink = view.findViewById<LinearLayout>(R.id.option_copy_link)
        val optionCollect = view.findViewById<LinearLayout>(R.id.option_collect)
        val optionReport = view.findViewById<LinearLayout>(R.id.option_report)
        val optionBlock = view.findViewById<LinearLayout>(R.id.option_block)
        val optionDelete = view.findViewById<LinearLayout>(R.id.option_delete)
        val optionCancel = view.findViewById<LinearLayout>(R.id.option_cancel)
        val imgCollect = view.findViewById<ImageView>(R.id.img_collect)
        val tvCollect = view.findViewById<TextView>(R.id.tv_collect)
        val tvBlock = view.findViewById<TextView>(R.id.tv_block)

        updateCollectUI(imgCollect, tvCollect)
        if (isOwnPost) {
            optionBlock.visibility = View.GONE
            optionReport.visibility = View.GONE
            optionDelete.visibility = View.VISIBLE
        } else {
            optionBlock.visibility = View.VISIBLE
            optionDelete.visibility = View.GONE
            tvBlock.text = if (isBlocked) "取消拉黑" else "拉黑"
        }

        optionShare.setOnClickListener {
            listener?.onShare()
            dismiss()
        }

        optionCopyLink.setOnClickListener {
            listener?.onCopyLink()
            dismiss()
        }

        optionCollect.setOnClickListener {
            listener?.onCollect()
            isCollected = !isCollected
            updateCollectUI(imgCollect, tvCollect)
            dismiss()
        }

        optionReport.setOnClickListener {
            listener?.onReport()
            dismiss()
        }

        optionBlock.setOnClickListener {
            listener?.onBlock()
            isBlocked = !isBlocked
            tvBlock.text = if (isBlocked) "取消拉黑" else "拉黑"
            dismiss()
        }

        optionDelete.setOnClickListener {
            listener?.onDelete()
            dismiss()
        }

        optionCancel.setOnClickListener {
            dismiss()
        }

        return dialog
    }

    private fun updateCollectUI(img: ImageView, tv: TextView) {
        if (isCollected) {
            img.setImageResource(R.drawable.ic_star_filled)
            tv.text = "已收藏"
        } else {
            img.setImageResource(R.drawable.ic_star)
            tv.text = "收藏"
        }
    }

    fun setPost(post: Post) {
        this.post = post
    }

    fun setCollected(collected: Boolean) {
        this.isCollected = collected
    }

    fun setBlocked(blocked: Boolean) {
        this.isBlocked = blocked
    }

    fun setOwnPost(own: Boolean) {
        this.isOwnPost = own
    }

    fun setListener(listener: PostOptionsListener) {
        this.listener = listener
    }

    companion object {
        const val TAG = "PostOptionsDialog"

        fun newInstance(
            post: Post,
            isCollected: Boolean,
            isOwnPost: Boolean,
            listener: PostOptionsListener
        ): PostOptionsDialog {
            return PostOptionsDialog().apply {
                setPost(post)
                setCollected(isCollected)
                setOwnPost(isOwnPost)
                setListener(listener)
            }
        }
    }
}
