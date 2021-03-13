package dev.daryl.mlkitbarcodescanner

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import dev.daryl.mlkitbarcodescanner.databinding.ActivityMainBinding
import dev.daryl.mlkitbarcodescanner.databinding.BottomSheetBarcodeBinding
import dev.daryl.mlkitbarcodescanner.utils.*

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private lateinit var binding: ActivityMainBinding
    private val viewModel by viewModels<MainViewModel> { MainViewModelFactory() }

    private var camera: Camera? = null
    private var cameraProvider: ProcessCameraProvider? = null

    private val cameraSelector by lazy {
        CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
    }

    private val screenAspectRatio by lazy {
        val metrics = DisplayMetrics().also { binding.previewView.display.getRealMetrics(it) }
        metrics.getAspectRatio()
    }

    private val executor by lazy {
        ContextCompat.getMainExecutor(this)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                setupCameraProvider()
            } else {
                showToast("Camera permission is needed to scan barcodes")
                finishAffinity()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        setContentView(binding.root)
    }

    init {
        lifecycleScope.launchWhenResumed {
            viewModel.cameraProvider.observe(this@MainActivity) {
                it?.let {
                    cameraProvider = it
                    bindUseCase()
                }
            }
        }
        lifecycleScope.launchWhenStarted {
            checkPermission()
            setupClickListeners()
        }
    }

    private fun openBottomSheet(result: String) {
        val barcodeBottomSheetBinding = BottomSheetBarcodeBinding.inflate(layoutInflater)
        val barcodeBottomSheetDialog = BottomSheetDialog(this).apply {
            setContentView(barcodeBottomSheetBinding.root)
            setCancelable(false)
            setOnShowListener {
                (barcodeBottomSheetBinding.root.parent as ViewGroup).background = ColorDrawable(
                    Color.TRANSPARENT
                )
            }
            show()
        }
        barcodeBottomSheetBinding.textResult.text = result
        barcodeBottomSheetBinding.btnGotIt.setOnClickListener {
            barcodeBottomSheetDialog.dismiss()
        }
        barcodeBottomSheetDialog.setOnDismissListener {
            bindUseCase()
        }
    }

    private fun setupClickListeners() {
        viewModel.isBtnFlashClicked.asLiveData().observe(this) {
            if (it) {
                if (camera?.cameraInfo?.torchState?.value == TorchState.ON) {
                    camera?.cameraControl?.enableTorch(false)
                } else {
                    camera?.cameraControl?.enableTorch(true)
                }
            }
        }
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun bindUseCase() {
        val barcodeScanner = BarcodeScanning.getClient()

        val previewUseCase = Preview.Builder()
            .setTargetRotation(binding.previewView.display.rotation)
            .setTargetAspectRatio(screenAspectRatio)
            .build().also {
                it.setSurfaceProvider(binding.previewView.surfaceProvider)
            }

        val analysisUseCase = ImageAnalysis.Builder()
            .setTargetRotation(binding.previewView.display.rotation)
            .setTargetAspectRatio(screenAspectRatio)
            .build().also {
                it.setAnalyzer(
                    executor,
                    { imageProxy ->
                        processImageProxy(barcodeScanner, imageProxy)
                    }
                )
            }

        val useCaseGroup = UseCaseGroup.Builder()
            .addUseCase(previewUseCase)
            .addUseCase(analysisUseCase)
            .build()

        try {
            camera = cameraProvider?.bindToLifecycle(
                this,
                cameraSelector,
                useCaseGroup
            )
        } catch (e: Exception) {
            Log.e(TAG, "bindUseCase: ", e)
        }
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun processImageProxy(barcodeScanner: BarcodeScanner, imageProxy: ImageProxy) {

        // This scans the entire screen for barcodes
        imageProxy.image?.let { image ->
            val inputImage = InputImage.fromMediaImage(image, imageProxy.imageInfo.rotationDegrees)
            barcodeScanner.process(inputImage)
                .addOnSuccessListener { barcodeList ->
                    if (!barcodeList.isNullOrEmpty()) {
                        Log.i(TAG, "processImageProxy: " + barcodeList[0].rawValue)
                        cameraProvider?.unbindAll()
                        openBottomSheet(barcodeList[0].rawValue!!) // Change this as required
                    }
                }.addOnFailureListener {
                    Log.e(TAG, "processImageProxy: ", it)
                }.addOnCompleteListener {
                    imageProxy.close()
                }
        }
/*
        // Takes a screenshot of the camera feed (previewView)
        val previewViewBitmap = binding.previewView.bitmap

        if (previewViewBitmap != null) {

            // Takes a screenshot of the view AppCompatImageView
            getScreenShotFromView(binding.imageOverlay, this) {

                // Darkens the view screenshot to keep the transparent portion intact, otherwise the barcode scanner will see through the
                // partially transparent view and will detect the code
                val darkenedBitmap = darkenBitMap(it)

                // Overlays the darkenedImage over the original camera feed which gives a bitmap with only the centre portion visible
                val overLaidBitmap = overlayBitmap(previewViewBitmap, darkenedBitmap)

                // Converts the overlaid bitmap into an inputImage for barcode scanner to scan
                val inputImage =
                    InputImage.fromBitmap(overLaidBitmap, imageProxy.imageInfo.rotationDegrees)
                barcodeScanner.process(inputImage)
                    .addOnSuccessListener { barcodeList ->
                        if (!barcodeList.isNullOrEmpty()) {
                            Log.i(TAG, "processImageProxy: " + barcodeList[0].rawValue)
                            cameraProvider.unbindAll()
                            openBottomSheet(barcodeList[0].rawValue!!) // Change this as required
                        }
                    }.addOnFailureListener {
                        Log.e(TAG, "processImageProxy: ", it)
                    }.addOnCompleteListener {
                        imageProxy.close()
                    }
            }
        }
*/
    }

    private fun setupCameraProvider() {
        val cameraProvideFuture = ProcessCameraProvider.getInstance(this)
        cameraProvideFuture.addListener(
            { viewModel.setCameraProvider(cameraProvideFuture.get()) },
            executor
        )
    }

    private fun checkPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> setupCameraProvider()
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            ) -> openPermissionRationaleDialog()
            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openPermissionRationaleDialog() {
        Snackbar.make(
            binding.root,
            "Camera permission is needed to scan barcodes",
            Snackbar.LENGTH_LONG
        ).setAction("Allow") {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }.show()
    }
}
