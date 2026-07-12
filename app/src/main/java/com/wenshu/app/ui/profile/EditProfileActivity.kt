package com.wenshu.app.ui.profile

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.wenshu.app.data.repository.PostRepository
import com.wenshu.app.databinding.ActivityEditProfileBinding
import com.wenshu.app.util.ImageUtils
import com.wenshu.app.data.SharedPreferencesManager
import kotlinx.coroutines.launch
import java.io.File

class EditProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditProfileBinding
    private val repository = PostRepository.getInstance()
    private var selectedAvatarPath: String? = null
    private var selectedCoverPath: String? = null
    private var pickingCover = false

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleImagePick(uri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadCurrentProfile()
        setupClickListeners()
    }

    private fun loadCurrentProfile() {
        val user = SharedPreferencesManager.getUser()
        binding.etUsername.setText(user?.username ?: "")
        binding.etBio.setText(user?.bio ?: "")
        binding.etLocation.setText(user?.location ?: "")
        val avatarUrl = ImageUtils.normalizeUrl(user?.avatar)
        if (avatarUrl != null) {
            Glide.with(this)
                .load(avatarUrl)
                .circleCrop()
                .into(binding.imgEditAvatar)
        }
        val coverUrl = ImageUtils.normalizeUrl(user?.cover)
        if (coverUrl != null) {
            Glide.with(this)
                .load(coverUrl)
                .centerCrop()
                .into(binding.imgEditCover)
            binding.tvChangeCover.visibility = View.GONE
        }
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener { finish() }

        binding.imgEditAvatar.setOnClickListener {
            showAvatarOptions()
        }

        binding.layoutEditCover.setOnClickListener {
            showCoverOptions()
        }

        binding.btnSave.setOnClickListener {
            saveProfile()
        }
    }

    private fun showAvatarOptions() {
        val options = arrayOf("从相册选择", "拍照")
        AlertDialog.Builder(this)
            .setTitle("更换头像")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> { pickingCover = false; pickFromGallery() }
                    1 -> showNotImplementedToast()
                }
            }
            .show()
    }

    private fun showCoverOptions() {
        val options = arrayOf("从相册选择", "拍照")
        AlertDialog.Builder(this)
            .setTitle("更换封面")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> { pickingCover = true; pickFromGallery() }
                    1 -> showNotImplementedToast()
                }
            }
            .show()
    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        pickImageLauncher.launch(intent)
    }

    private fun handleImagePick(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val prefix = if (pickingCover) "cover" else "avatar"
            val tempFile = File(cacheDir, "${prefix}_${System.currentTimeMillis()}.jpg")
            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            if (pickingCover) {
                selectedCoverPath = tempFile.absolutePath
                Glide.with(this)
                    .load(tempFile)
                    .centerCrop()
                    .into(binding.imgEditCover)
                binding.tvChangeCover.visibility = View.GONE
            } else {
                selectedAvatarPath = tempFile.absolutePath
                Glide.with(this)
                    .load(tempFile)
                    .circleCrop()
                    .into(binding.imgEditAvatar)
            }
        } catch (e: Exception) {
            Log.e("EditProfile", "Error handling image", e)
            Toast.makeText(this, "图片选择失败", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showNotImplementedToast() {
        Toast.makeText(this, "功能开发中", Toast.LENGTH_SHORT).show()
    }

    private fun saveProfile() {
        val username = binding.etUsername.text.toString().trim()
        val bio = binding.etBio.text.toString().trim()
        val location = binding.etLocation.text.toString().trim()

        if (username.isEmpty()) {
            Toast.makeText(this, "用户名不能为空", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSave.isEnabled = false

        lifecycleScope.launch {
            try {
                var avatarUrl: String? = null
                var coverUrl: String? = null

                if (selectedAvatarPath != null) {
                    val uploadResult = repository.uploadImage(selectedAvatarPath!!)
                    uploadResult.onSuccess { url ->
                        avatarUrl = url
                    }.onFailure { e ->
                        Log.e("EditProfile", "Avatar upload failed", e)
                    }
                }

                if (selectedCoverPath != null) {
                    val uploadResult = repository.uploadImage(selectedCoverPath!!)
                    uploadResult.onSuccess { url ->
                        coverUrl = url
                    }.onFailure { e ->
                        Log.e("EditProfile", "Cover upload failed", e)
                    }
                }

                val result = repository.updateProfile(username, bio, location, avatarUrl, coverUrl)
                result.onSuccess {
                    Toast.makeText(this@EditProfileActivity, "保存成功", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                }.onFailure { e ->
                    Toast.makeText(this@EditProfileActivity, e.message ?: "保存失败", Toast.LENGTH_SHORT).show()
                    binding.btnSave.isEnabled = true
                }
            } catch (e: Exception) {
                Log.e("EditProfile", "Save error", e)
                Toast.makeText(this@EditProfileActivity, "保存失败", Toast.LENGTH_SHORT).show()
                binding.btnSave.isEnabled = true
            }
        }
    }
}
