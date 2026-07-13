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
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.wenshu.app.R
import com.wenshu.app.data.SharedPreferencesManager
import com.wenshu.app.data.model.Comment
import com.wenshu.app.data.model.Post
import com.wenshu.app.databinding.ActivityPostDetailBinding
import com.wenshu.app.ui.adapters.CommentAdapter
import com.wenshu.app.ui.adapters.ImagePagerAdapter
import com.wenshu.app.ui.profile.UserProfileActivity
import com.wenshu.app.util.ImageUtils
import com.wenshu.app.util.TimeUtils

class PostDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetailBinding
    private val viewModel: PostDetailViewModel by viewModels()
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var imageAdapter: ImagePagerAdapter
    private var postId: String? = null
    private var currentPost: Post? = null
    private var replyToCommentId: String? = null

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
        imageAdapter = ImagePagerAdapter()
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
        val images = currentPost?.images ?: return
        if (images.size <= 1) {
            binding.tvImageIndicator.visibility = View.GONE
            return
        }
        binding.tvImageIndicator.visibility = View.VISIBLE
        binding.tvImageIndicator.text = "${position + 1}/${images.size}"
    }

    private fun setupDoubleTapLike() {
        if (!SharedPreferencesManager.isDoubleTapToLikeEnabled()) return
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
        heart.setColorFilter(ContextCompat.getColor(this, R.color.seal))

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
            onLikeClick = { comment ->
                viewModel.toggleCommentLike(comment.id)
            },
            onReplyClick = { comment ->
                val name = comment.author?.displayName ?: ""
                replyToCommentId = if (comment.isReply) comment.replyToId ?: comment.id else comment.id
                binding.etComment.hint = "回复 @$name"
                binding.etComment.requestFocus()
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.etComment, InputMethodManager.SHOW_IMPLICIT)
            },
            onUserClick = { comment ->
                val userId = comment.authorId ?: comment.author?.id ?: return@CommentAdapter
                val intent = Intent(this, UserProfileActivity::class.java)
                intent.putExtra("user_id", userId)
                startActivity(intent)
            }
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
        binding.layoutCoin.setOnClickListener { tipPost() }
        binding.layoutComment.setOnClickListener {
            binding.etComment.requestFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.etComment, InputMethodManager.SHOW_IMPLICIT)
        }
        binding.layoutShare.setOnClickListener { sharePost() }
        binding.btnSendComment.setOnClickListener { sendComment() }

        binding.imgAvatar.setOnClickListener { navigateToUserProfile() }
        binding.tvNickname.setOnClickListener { navigateToUserProfile() }

        binding.btnSendComment.isEnabled = false
        binding.btnSendComment.alpha = 0.3f

        binding.btnFollow.visibility = View.GONE
    }

    private fun navigateToUserProfile() {
        val post = currentPost ?: return
        val authorId = post.authorId ?: post.author?.id ?: return
        val intent = Intent(this, UserProfileActivity::class.java)
        intent.putExtra("user_id", authorId)
        startActivity(intent)
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
                viewModel.loadComments(post.id)
            }
        }
        viewModel.comments.observe(this) { comments ->
            val parentComments = comments.filter { !it.isReply }
            val repliesByParent = comments.filter { it.isReply }.groupBy { it.replyToId }
            parentComments.forEach { parent ->
                commentAdapter.setReplies(parent.id, repliesByParent[parent.id] ?: emptyList())
            }
            commentAdapter.submitList(parentComments)
            val totalCount = comments.size
            binding.tvCommentTitle.text = "评论 $totalCount"
        }
        viewModel.commentAdded.observe(this) { added ->
            if (added) {
                binding.etComment.text?.clear()
                binding.etComment.hint = getString(R.string.say_something)
                replyToCommentId = null
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(binding.etComment.windowToken, 0)
                binding.recyclerComments.postDelayed({
                    binding.recyclerComments.smoothScrollToPosition(commentAdapter.itemCount - 1)
                }, 100)
                viewModel.resetCommentAdded()
            }
        }
        viewModel.actionResult.observe(this) { result ->
            result?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearActionResult()
            }
        }
        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
                viewModel.clearError()
            }
        }
    }

    private fun bindPost(post: Post) {
        with(binding) {
            tvTitle.visibility = View.GONE
            tvContent.text = post.content
            tvNickname.text = post.author?.displayName ?: ""
            tvTime.text = TimeUtils.formatRelativeTime(post.createdAt)
            layoutLocation.visibility = View.GONE
            imgVerified.visibility = if (post.author?.isVip == true) View.VISIBLE else View.GONE

            Glide.with(this@PostDetailActivity)
                .load(ImageUtils.normalizeUrl(post.author?.avatar))
                .placeholder(R.drawable.bg_avatar_placeholder)
                .error(R.drawable.bg_avatar_placeholder)
                .centerCrop()
                .into(imgAvatar)

            imageAdapter.updateImages(post.images)
            updateImageIndicator(0)

            updateLikeState(post.isLiked, post.likeCount)
            updateCollectState(post.isCollected, post.collectCount)
            updateCoinState(post.isTipped)
            tvLikeCountDetail.text = post.likeCount.toString()

            layoutTags.removeAllViews()
            post.tags.forEach { tag ->
                val tv = TextView(this@PostDetailActivity).apply {
                    text = tag
                    textSize = 14f
                    typeface = android.graphics.Typeface.SERIF
                    setTextColor(ContextCompat.getColor(context, R.color.primary))
                    setPadding(0, 0, resources.getDimensionPixelSize(R.dimen.spacing_md), 0)
                }
                layoutTags.addView(tv)
            }
            layoutTags.visibility = if (post.tags.isEmpty()) View.GONE else View.VISIBLE
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

    private fun tipPost() {
        val amount = SharedPreferencesManager.getDefaultTipAmount()
        viewModel.tipPost(amount)
        animateButton(binding.coinIconContainer)
    }

    private fun updateCoinState(isTipped: Boolean) {
        if (isTipped) {
            binding.bgCoinIconDetail.setBackgroundResource(R.drawable.bg_coin_filled)
            binding.tvCoinSymbolDetail.setTextColor(ContextCompat.getColor(this, R.color.background))
        } else {
            binding.bgCoinIconDetail.setBackgroundResource(R.drawable.bg_coin_outline)
            binding.tvCoinSymbolDetail.setTextColor(ContextCompat.getColor(this, R.color.ink))
        }
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
        binding.imgLike.setColorFilter(ContextCompat.getColor(this, if (isLiked) R.color.seal else R.color.text_primary))
        binding.tvLikeCountDetail.text = count.toString()
    }

    private fun updateCollectState(isCollected: Boolean, count: Int) {
        binding.imgCollect.setImageResource(if (isCollected) R.drawable.ic_star_filled else R.drawable.ic_star)
        binding.imgCollect.setColorFilter(ContextCompat.getColor(this, if (isCollected) R.color.ink else R.color.text_primary))
    }

    private fun sendComment() {
        val content = binding.etComment.text.toString().trim()
        if (content.isBlank()) return
        viewModel.addComment(content, replyToCommentId)
    }

    private fun sharePost() {
        val post = currentPost ?: return
        val username = post.author?.username ?: "user"
        val shareUrl = "https://wenshucom.vercel.app/$username/${post.id}"
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "${post.content}\n\n$shareUrl\n—— 来自文书")
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
    }
}
