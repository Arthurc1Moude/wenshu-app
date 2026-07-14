package com.wenshu.app.ui.postdetail

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.URLSpan
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.MediaController
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
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
import com.wenshu.app.data.model.FileAttachment
import com.wenshu.app.data.model.MediaItem
import com.wenshu.app.data.model.Post
import com.wenshu.app.data.model.UrlPreview
import com.wenshu.app.databinding.ActivityPostDetailBinding
import com.wenshu.app.ui.adapters.CommentAdapter
import com.wenshu.app.ui.adapters.ImagePagerAdapter
import com.wenshu.app.ui.fileviewer.FileViewerActivity
import com.wenshu.app.ui.profile.UserProfileActivity
import com.wenshu.app.ui.webview.WebViewActivity
import com.wenshu.app.util.ImageUtils
import com.wenshu.app.util.LinkifyUtils
import com.wenshu.app.util.TimeUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.regex.Pattern

class PostDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPostDetailBinding
    private val viewModel: PostDetailViewModel by viewModels()
    private lateinit var commentAdapter: CommentAdapter
    private lateinit var imageAdapter: ImagePagerAdapter
    private var postId: String? = null
    private var currentPost: Post? = null
    private var replyToCommentId: String? = null
    private var isVideoPlaying = false
    private var currentVideoUrl: String? = null

    private val urlPattern = Pattern.compile("https?://[^\\s]+")

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
        setupVideoView()
        setupComments()
        setupActions()
        setupCommentInput()
        setupDoubleTapLike()
        observeData()

        viewModel.loadPost(postId!!)
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnMore.setOnClickListener { showMoreMenu(it) }
    }

    private fun showMoreMenu(anchor: View) {
        val popupView = LayoutInflater.from(this).inflate(R.layout.dialog_post_more, null)
        val popupWindow = PopupWindow(
            popupView,
            resources.getDimensionPixelSize(R.dimen.popup_menu_width),
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.elevation = 8f
        popupWindow.setBackgroundDrawable(getDrawable(R.drawable.bg_bottom_sheet))

        popupView.findViewById<View>(R.id.btn_share).setOnClickListener {
            sharePost()
            popupWindow.dismiss()
        }
        popupView.findViewById<View>(R.id.btn_copy_link).setOnClickListener {
            val post = currentPost ?: return@setOnClickListener
            val username = post.author?.username ?: "user"
            val shareUrl = "https://wenshucom.vercel.app/$username/${post.id}"
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("post_link", shareUrl))
            Toast.makeText(this, "链接已复制", Toast.LENGTH_SHORT).show()
            popupWindow.dismiss()
        }
        popupView.findViewById<View>(R.id.btn_collect).setOnClickListener {
            toggleCollect()
            popupWindow.dismiss()
        }
        popupView.findViewById<View>(R.id.btn_report).setOnClickListener {
            Toast.makeText(this, "已举报", Toast.LENGTH_SHORT).show()
            popupWindow.dismiss()
        }
        popupView.findViewById<View>(R.id.btn_cancel).setOnClickListener {
            popupWindow.dismiss()
        }

        val tvCollect = popupView.findViewById<TextView>(R.id.tv_collect)
        if (currentPost?.isCollected == true) {
            tvCollect.text = "取消收藏"
        } else {
            tvCollect.text = "收藏"
        }

        popupWindow.showAsDropDown(anchor, 0, 0)
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
                binding.videoView.layoutParams.height = width
                binding.videoView.requestLayout()
            }
        })

        binding.viewPagerImages.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateImageIndicator(position)
            }
        })
    }

    private fun setupVideoView() {
        binding.btnVideoPlay.setOnClickListener {
            if (currentVideoUrl != null) {
                playVideo(currentVideoUrl!!)
            }
        }
        binding.videoView.setOnPreparedListener { mp ->
            mp.isLooping = true
            binding.btnVideoPlay.visibility = View.GONE
            binding.viewPagerImages.visibility = View.GONE
            binding.videoView.visibility = View.VISIBLE
            isVideoPlaying = true
        }
        binding.videoView.setOnCompletionListener {
            binding.btnVideoPlay.visibility = View.VISIBLE
        }
    }

    private fun playVideo(url: String) {
        val fullUrl = ImageUtils.normalizeUrl(url)
        val videoUri = Uri.parse(fullUrl)
        binding.videoView.setVideoURI(videoUri)
        val mediaController = MediaController(this)
        mediaController.setAnchorView(binding.videoView)
        binding.videoView.setMediaController(mediaController)
        binding.videoView.start()
        binding.btnVideoPlay.visibility = View.GONE
    }

    private fun updateImageIndicator(position: Int) {
        val images = currentPost?.images ?: emptyList()
        val media = currentPost?.media ?: emptyList()
        val imageItems = media.filter { it.type == "image" || it.type == "gif" }
        val videoItems = media.filter { it.type == "video" }
        val totalImages = images.size + imageItems.size

        if (videoItems.isNotEmpty()) {
            val video = videoItems.first()
            currentVideoUrl = video.url
            binding.btnVideoPlay.visibility = View.VISIBLE
        } else {
            currentVideoUrl = null
            binding.btnVideoPlay.visibility = View.GONE
        }

        if (totalImages <= 1 && videoItems.isEmpty()) {
            binding.tvImageIndicator.visibility = View.GONE
            return
        }
        val totalCount = totalImages + if (videoItems.isNotEmpty()) 1 else 0
        if (totalCount <= 1) {
            binding.tvImageIndicator.visibility = View.GONE
            return
        }
        binding.tvImageIndicator.visibility = View.VISIBLE
        binding.tvImageIndicator.text = "${position + 1}/$totalCount"
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
        binding.layoutMediaContainer.setOnTouchListener { _, event ->
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

    private fun setupLinkifyContent(text: String) {
        LinkifyUtils.setupClickableLinks(binding.tvContent, this, text)
    }

    private fun openUrl(url: String) {
        LinkifyUtils.openUrl(this, url)
    }

    private fun bindPost(post: Post) {
        with(binding) {
            val displayTitle = post.displayTitle
            val displayContent = post.displayContent

            if (displayTitle.isNotBlank() && (post.isLongText || post.title.isNotBlank())) {
                tvTitle.visibility = View.VISIBLE
                tvTitle.text = displayTitle
                setupLinkifyContent(displayContent)
            } else {
                tvTitle.visibility = View.GONE
                setupLinkifyContent(post.content)
            }

            tvNickname.text = post.author?.displayName ?: ""
            tvTime.text = TimeUtils.formatRelativeTime(post.createdAt)
            layoutLocation.visibility = if (!post.location.isNullOrBlank()) View.VISIBLE else View.GONE
            if (!post.location.isNullOrBlank()) {
                tvLocation.text = post.location
            }
            imgVerified.visibility = if (post.author?.isVip == true) View.VISIBLE else View.GONE

            Glide.with(this@PostDetailActivity)
                .load(ImageUtils.normalizeUrl(post.author?.avatar))
                .placeholder(R.drawable.bg_avatar_placeholder)
                .error(R.drawable.bg_avatar_placeholder)
                .centerCrop()
                .into(imgAvatar)

            val allImages = mutableListOf<String>()
            allImages.addAll(post.images)
            val videoItems = post.media.filter { it.type == "video" }
            val imageMediaItems = post.media.filter { it.type == "image" || it.type == "gif" }
            imageMediaItems.forEach { allImages.add(it.url) }

            if (videoItems.isNotEmpty()) {
                currentVideoUrl = videoItems.first().url
                btnVideoPlay.visibility = View.VISIBLE
                if (allImages.isEmpty()) {
                    viewPagerImages.visibility = View.GONE
                } else {
                    viewPagerImages.visibility = View.VISIBLE
                }
            } else {
                currentVideoUrl = null
                btnVideoPlay.visibility = View.GONE
                viewPagerImages.visibility = View.VISIBLE
                videoView.visibility = View.GONE
            }

            imageAdapter.updateImages(allImages)
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

            renderFiles(post.files)
            renderUrlPreviews(post.urlPreviews)
        }
    }

    private fun renderFiles(files: List<FileAttachment>) {
        binding.layoutFiles.removeAllViews()
        if (files.isEmpty()) {
            binding.layoutFiles.visibility = View.GONE
            return
        }
        binding.layoutFiles.visibility = View.VISIBLE
        val inflater = LayoutInflater.from(this)
        for (file in files) {
            val view = inflater.inflate(R.layout.item_file_attachment, binding.layoutFiles, false)
            val imgIcon = view.findViewById<ImageView>(R.id.img_file_icon)
            val tvName = view.findViewById<TextView>(R.id.tv_file_name)
            val tvInfo = view.findViewById<TextView>(R.id.tv_file_info)
            val btnRemove = view.findViewById<ImageView>(R.id.btn_remove_file)

            btnRemove.visibility = View.GONE
            tvName.text = file.originalName

            val expiresText = if (file.isPermanent) {
                "永久保存"
            } else if (file.expiresAt != null) {
                val sdf = SimpleDateFormat("MM月dd日过期", Locale.CHINESE)
                sdf.format(Date(file.expiresAt))
            } else "14天后过期"
            tvInfo.text = "${file.sizeFormatted} · $expiresText"

            val iconRes = getFileIconResource(file.iconType, file.ext)
            imgIcon.setImageResource(iconRes)

            view.setOnClickListener {
                val intent = Intent(this@PostDetailActivity, FileViewerActivity::class.java)
                intent.putExtra(FileViewerActivity.EXTRA_FILE, file)
                startActivity(intent)
            }

            binding.layoutFiles.addView(view)
        }
    }

    private fun getFileIconResource(iconType: String, ext: String): Int {
        return when (iconType) {
            "image" -> R.drawable.ic_file_image
            "video" -> R.drawable.ic_file_video
            "audio" -> R.drawable.ic_file_audio
            "document" -> R.drawable.ic_file_document
            "spreadsheet" -> R.drawable.ic_file_spreadsheet
            "presentation" -> R.drawable.ic_file_presentation
            "archive" -> R.drawable.ic_file_archive
            "code" -> R.drawable.ic_file_code
            else -> when {
                ext.equals("pdf", true) -> R.drawable.ic_file_document
                ext.isEmpty() -> R.drawable.ic_file_unknown
                else -> R.drawable.ic_file_generic
            }
        }
    }

    private fun renderUrlPreviews(previews: List<UrlPreview>) {
        binding.layoutUrlPreviews.removeAllViews()
        if (previews.isEmpty()) {
            binding.layoutUrlPreviews.visibility = View.GONE
            return
        }
        binding.layoutUrlPreviews.visibility = View.VISIBLE
        val inflater = LayoutInflater.from(this)
        for (preview in previews) {
            val view = inflater.inflate(R.layout.item_url_preview, binding.layoutUrlPreviews, false)
            val tvTitle = view.findViewById<TextView>(R.id.tv_url_title)
            val tvUrl = view.findViewById<TextView>(R.id.tv_url_domain)
            val imgFavicon = view.findViewById<ImageView>(R.id.img_favicon)

            tvTitle.text = preview.title.ifBlank { preview.url }
            tvUrl.text = Uri.parse(preview.url).host ?: preview.url

            if (preview.favicon != null) {
                Glide.with(this)
                    .load(preview.favicon)
                    .placeholder(R.drawable.ic_globe)
                    .error(R.drawable.ic_globe)
                    .into(imgFavicon)
            } else {
                imgFavicon.setImageResource(R.drawable.ic_globe)
            }

            view.setOnClickListener {
                openUrl(preview.url)
            }

            binding.layoutUrlPreviews.addView(view)
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
            putExtra(Intent.EXTRA_TEXT, "${post.content.take(100)}\n\n$shareUrl\n—— 来自文书")
        }
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::binding.isInitialized) {
            binding.videoView.stopPlayback()
        }
    }
}
