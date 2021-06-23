package dev.daryl.mlkitbarcodescanner

import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _cameraProvider = MutableLiveData<ProcessCameraProvider>()
    val cameraProvider: LiveData<ProcessCameraProvider> = _cameraProvider
    fun setCameraProvider(processCameraProvider: ProcessCameraProvider) {
        _cameraProvider.postValue(processCameraProvider)
    }
}
