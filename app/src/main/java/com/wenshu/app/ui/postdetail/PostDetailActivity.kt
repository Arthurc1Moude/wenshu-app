package com.wenshu.app.ui.postdetail

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.google.android.flexbox.FlexboxLayout
import com.wenshu.app.R
import com.wenshu.app.data.model.Post
import com.wenshu.app.databinding.ActivityPostDetailBinding
import com.wenshu.app.ui.adapters.CommentAdapter
import com.wenshu.app.ui.adapters.ImagePagerAdapter
import com.wenshu.app.util.TimeUtils

class PostDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetailBinding
    private val viewModel: PostDetailViewModel by viewModels()
    private lateinit var commentAdapter: CommentAdapter
    private var postId: String? = null
    private var currentPost: Post? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        postId = intent.getStringExtra("post_id")
        if (postId == null) {
            finish()
            return
        }

        setupToolbar()
        setupImagePager()
        setupComments()
        setupActions()
        setupCommentInput()
        setupDoubleTapLike()
        observeData()

        viewModel.loadPost(postId!!)
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupImagePager() {
        val imageAdapter = ImagePagerAdapter(emptyList())
        binding.viewPagerImages.adapter = imageAdapter

        binding.viewPagerImages.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.viewPagerImages.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val width = binding.viewPagerImages.width
                binding.viewPagerImages.layoutParams.height = width
                binding.viewPagerImages.requestLayout()
            }
        })

        binding.viewPagerImages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateImageIndicator(position)
            }
        })
    }

    private fun updateImageIndicator(position: Int) {
        val images = currentPost?.imageUrls ?: return
        if (images.size <= 1) {
            binding.tvImageIndicator.visibility = View.GONE
            return
        }
        binding.tvImageIndicator.visibility = View.VISIBLE
        binding.tvImageIndicator.text = "${position + 1}/${images.size}"
    }

    private fun setupDoubleTapLike() {
        val gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                if (currentPost?.isLiked != true) {
                    toggleLike()
                }
                showBigHeartAnimation()
                return true
            }
        })
        binding.viewPagerImages.getChildAt(0)?.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            false
        }
    }

    private fun showBigHeartAnimation() {
        val heart = binding.imgBigHeart
        heart.alpha = 0f
        heart.scaleX = 0.3f
        heart.scaleY = 0.3f

        val fadeIn = ObjectAnimator.ofFloat(heart, View.ALPHA, 0f, 1f).setDuration(150)
        val scaleX = ObjectAnimator.ofFloat(heart, View.SCALE_X, 0.3f, 1f).setDuration(250)
        val scaleY = ObjectAnimator.ofFloat(heart, View.SCALE_Y, 0.3f, 1f).setDuration(250)
        val fadeOut = ObjectAnimator.ofFloat(heart, View.ALPHA, 1f, 0f).setDuration(300)
        fadeOut.startDelay = 400
        val scaleOutX = ObjectAnimator.ofFloat(heart, View.SCALE_X, 1f, 1.5f).setDuration(300)
        val scaleOutY = ObjectAnimator.ofFloat(heart, View.SCALE_Y, 1f, 1.5f).setDuration(300)
        scaleOutX.startDelay = 400
        scaleOutY.startDelay = 400

        scaleX.interpolator = DecelerateInterpolator()
        scaleY.interpolator = DecelerateInterpolator()
        fadeOut.interpolator = AccelerateDecelerateInterpolator()

        AnimatorSet().apply {
            playTogether(fadeIn, scaleX, scaleY, fadeOut, scaleOutX, scaleOutY)
            start()
        }
    }

    private fun setupComments() {
        commentAdapter = CommentAdapter(
            comments = emptyList(),
            onLikeClick = { },
            onReplyClick = { comment ->
                val replyHint = getString(R.string.reply_hint, comment.author.nickname)
                binding.etComment.hint = replyHint
                binding.etComment.requestFocus()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.etComment, InputMethodManager.SHOW_IMPLICIT)
            },
            onUserClick = { }
        )
        binding.recyclerComments.apply {
            layoutManager = LinearLayoutManager(this@PostDetailActivity)
            adapter = commentAdapter
            itemAnimator = null
        }
    }

    private fun setupActions() {
        binding.layoutLike.setOnClickListener { toggleLike() }
        binding.layoutCollect.setOnClickListener { toggleCollect() }
        binding.layoutComment.setOnClickListener {
            binding.etComment.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etComment, InputMethodManager.SHOW_IMPLICIT)
        }
        binding.layoutShare.setOnClickListener { sharePost() }
        binding.btnSendComment.setOnClickListener { sendComment() }

        binding.btnSendComment.isEnabled = false
        binding.btnSendComment.alpha = 0.3f
    }

    private fun setupCommentInput() {
        binding.etComment.doAfterTextChanged { text ->
            val hasContent = !text.isNullOrBlank()
            binding.btnSendComment.isEnabled = hasContent
            binding.btnSendComment.alpha = if (hasContent) 1f else 0.3f
        }
    }

    private fun observeData() {
        viewModel.post.observe(this) { post ->
            if (post != null) {
                currentPost = post
                bindPost(post)
            }
        }
        viewModel.comments.observe(this) { comments ->
            commentAdapter.updateComments(comments)
            binding.tvCommentTitle.text = getString(R.string.comment_count, comments.size)
        }
    }

    private fun bindPost(post: Post) {
        with(binding) {
            tvTitle.text = post.title
            tvTitle.visibility = if (post.title.isBlank()) View.GONE else View.VISIBLE
            tvContent.text = post.content
            tvNickname.text = post.author.nickname
            tvTime.text = TimeUtils.getRelativeTime(post.createdAt)
            tvLocation.text = post.location
            layoutLocation.visibility = if (post.location != null) View.VISIBLE else View.GONE
            imgVerified.visibility = if (post.author.isVerified) View.VISIBLE else View.GONE

            Glide.with(this@PostDetailActivity)
                .load(post.author.avatarUrl)
                .placeholder(R.drawable.bg_circle_placeholder)
                .into(imgAvatar)

            val images = if (post.imageUrls.isNotEmpty()) post.imageUrls else listOf(post.coverImageUrl)
            (viewPagerImages.adapter as? ImagePagerAdapter)?.updateImages(images)
            updateImageIndicator(0)

            updateLikeState(post.isLiked, post.likeCount)
            updateCollectState(post.isCollected, post.collectCount)
            tvLikeCountDetail.text = getString(R.string.like_count_post, TimeUtils.formatCount(post.likeCount.toLong()))

            layoutTags.removeAllViews()
            post.tags.forEach { tag ->
                val tv = TextView(this@PostDetailActivity).apply {
                    text = "#$tag#"
                    textSize = 14f
                    setTextColor(ContextCompat.getColor(context, R.color.link))
                    setPadding(0, 0, resources.getDimensionPixelSize(R.dimen.spacing_md), 0)
                }
                layoutTags.addView(tv)
            }
            layoutTags.visibility = if (post.tags.isEmpty()) View.GONE else View.VISIBLE

            btnFollow.text = if (post.isFollowed) getString(R.string.following) else getString(R.string.follow)
            btnFollow.background = ContextCompat.getDrawable(
                this@PostDetailActivity,
                if (post.isFollowed) R.drawable.bg_follow_button_outline else R.drawable.bg_follow_button
            )
            btnFollow.setTextColor(ContextCompat.getColor(
                this@PostDetailActivity,
                if (post.isFollowed) R.color.text_secondary else R.color.on_primary
            ))
        }
    }

    private fun toggleLike() {
        val post = currentPost ?: return
        viewModel.toggleLike()
        animateButton(binding.imgLike)
    }

    private fun toggleCollect() {
        val post = currentPost ?: return
        viewModel.toggleCollect()
        animateButton(binding.imgCollect)
    }

    private fun animateButton(view: View) {
        val scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, 1.4f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, 1.4f, 1f)
        AnimatorSet().apply {
            playTogether(scaleX, scaleY)
            duration = 350
            interpolator = DecelerateInterpolator()
            start()
        }
    }

    private fun updateLikeState(isLiked: Boolean, count: Int) {
        binding.imgLike.setImageResource(if (isLiked) R.drawable.ic_heart_filled else R.drawable.ic_heart)
        binding.imgLike.setColorFilter(ContextCompat.getColor(this, if (isLiked) R.color.liked else R.color.text_primary))
    }

    private fun updateCollectState(isCollected: Boolean, count: Int) {
        binding.imgCollect.setImageResource(if (isCollected) R.drawable.ic_star_filled else R.drawable.ic_star)
        binding.imgCollect.setColorFilter(ContextCompat.getColor(this, if (isCollected) R.color.collected else R.color.text_primary))
    }

    private fun sendComment() {
        val content = binding.etComment.text.toString().trim()
        if (content.isBlank()) return
        viewModel.addComment(content)
        binding.etComment.text?.clear()
        binding.etComment.hint = getString(R.string.say_something)
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.etComment.windowToken, 0)
        binding.recyclerComments.postDelayed({
            binding.recyclerComments.smoothScrollToPosition(0)
        }, 100)
    }

    private fun sharePost() {
        val post = currentPost ?: return
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "${post.title}\n${post.content}\n\n—— 来自文书APP")
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
    }
}
