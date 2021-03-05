package dev.daryl.mlkitbarcodescanner

import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.daryl.mlkitbarcodescanner.utils.mutableEventFlow
import kotlinx.coroutines.flow.asSharedFlow

class MainViewModel : ViewModel() {
    private val _isBtnFlashClicked = mutableEventFlow<Boolean>()
    val isBtnFlashClicked = _isBtnFlashClicked.asSharedFlow()

    fun setBtnFlashClicked() {
        _isBtnFlashClicked.tryEmit(true)
    }

    private val _cameraProvider = MutableLiveData<ProcessCameraProvider>()
    val cameraProvider: LiveData<ProcessCameraProvider> = _cameraProvider

    fun setCameraProvider(processCameraProvider: ProcessCameraProvider) {
        _cameraProvider.value = processCameraProvider
    }
}
