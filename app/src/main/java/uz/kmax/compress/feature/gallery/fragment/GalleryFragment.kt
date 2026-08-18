package uz.kmax.compress.feature.gallery.fragment

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import uz.kmax.base.fragment.BaseFragmentNV
import uz.kmax.compress.R
import uz.kmax.compress.core.permission.PermissionManager
import uz.kmax.compress.core.storage.StorageManager
import uz.kmax.compress.databinding.FragmentGalleryBinding
import uz.kmax.compress.feature.gallery.adapter.RecentImagesAdapter
import uz.kmax.compress.feature.gallery.event.GalleryEvent
import uz.kmax.compress.feature.gallery.state.GalleryUiState
import uz.kmax.compress.feature.gallery.viewmodel.GalleryViewModel
import javax.inject.Inject

@AndroidEntryPoint
class GalleryFragment : BaseFragmentNV<FragmentGalleryBinding>(FragmentGalleryBinding::inflate) {

    @Inject
    lateinit var permissionManager: PermissionManager

    @Inject
    lateinit var storageManager: StorageManager

    private val viewModel: GalleryViewModel by viewModels()
    private val adapter by lazy {
        RecentImagesAdapter { image ->
            viewModel.onEvent(GalleryEvent.NavigateCompress(image))
        }
    }

    private var tempImageUri: Uri? = null

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(50)) { uris ->
        if (uris.isNotEmpty()) {
            try {
                viewModel.onImagesPicked(uris)
            } catch (e: Exception) {
                com.google.android.material.snackbar.Snackbar.make(binding.root, "Limit error: ${e.message}", com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private val captureImage = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            tempImageUri?.let { viewModel.onImagePicked(it) }
        }
    }

    private val requestCameraPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            openCamera()
        }
    }

    override fun onViewCreated() {
        setupToolbar()
        setupRecyclerView()
        setupListeners()
        observeViewModel()
        
        // Only load if permission already granted, don't ask automatically
        if (permissionManager.hasStoragePermission()) {
            viewModel.loadRecentImages()
        }
    }

    private fun openCamera() {
        val tempFile = storageManager.createTempFile("jpg", "Camera_Capture")
        tempImageUri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.fileprovider",
            tempFile
        )
        captureImage.launch(tempImageUri)
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            navController.popBackStack()
        }
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_settings -> {
                    navController.navigate(R.id.action_galleryFragment_to_settingsFragment)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        binding.rvRecentImages.adapter = adapter
    }

    private fun setupListeners() {
        binding.apply {
            cardSelectImage.setOnClickListener {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
            btnGallery.setOnClickListener {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
            btnCamera.setOnClickListener {
                if (permissionManager.hasPermission(Manifest.permission.CAMERA)) {
                    openCamera()
                } else {
                    requestCameraPermission.launch(Manifest.permission.CAMERA)
                }
            }
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        renderState(state)
                    }
                }
                launch {
                    viewModel.event.collect { event ->
                        handleEvent(event)
                    }
                }
            }
        }
    }

    private fun renderState(state: GalleryUiState) {
        binding.apply {
            progressBar.visibility = if (state is GalleryUiState.Loading) View.VISIBLE else View.GONE
            tvEmpty.visibility = if (state is GalleryUiState.Empty) View.VISIBLE else View.GONE
            rvRecentImages.visibility = if (state is GalleryUiState.Content) View.VISIBLE else View.GONE
            tvRecentTitle.visibility = if (state is GalleryUiState.Content) View.VISIBLE else View.GONE

            if (state is GalleryUiState.Content) {
                adapter.submitList(state.images)
            } else if (state is GalleryUiState.Error) {
                com.google.android.material.snackbar.Snackbar.make(root, state.message, com.google.android.material.snackbar.Snackbar.LENGTH_LONG).show()
            }
        }
    }

    private fun handleEvent(event: GalleryEvent) {
        when (event) {
            is GalleryEvent.NavigateCompress -> {
                val action = GalleryFragmentDirections.actionGalleryFragmentToPredictionFragment(event.image)
                navController.navigate(action)
            }
            is GalleryEvent.NavigateBatch -> {
                val action = GalleryFragmentDirections.actionGalleryFragmentToBatchFragment(
                    batchId = event.batchId,
                    categoryType = null
                )
                navController.navigate(action)
            }
            is GalleryEvent.ShowLimitDialog -> {
                val dialogView = layoutInflater.inflate(R.layout.dialog_limit_premium, null)
                val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext(), R.style.Widget_Lumora_BottomSheet) // Use a style with transparent background if needed
                    .setView(dialogView)
                    .create()

                dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnPremium).setOnClickListener {
                    navController.navigate(R.id.action_global_premiumFragment)
                    dialog.dismiss()
                }
                dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnCancel).setOnClickListener {
                    dialog.dismiss()
                }
                
                dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
                dialog.show()
            }
            else -> {}
        }
    }
}
