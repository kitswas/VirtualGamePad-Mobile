package io.github.kitswas.virtualgamepadmobile.utils

import android.content.Context
import android.util.Log
import com.google.android.gms.common.moduleinstall.InstallStatusListener
import com.google.android.gms.common.moduleinstall.ModuleInstall
import com.google.android.gms.common.moduleinstall.ModuleInstallRequest
import com.google.android.gms.common.moduleinstall.ModuleInstallStatusUpdate
import com.google.android.gms.common.moduleinstall.ModuleInstallStatusUpdate.InstallState
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScanner
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class QRScannerManager(private val context: Context) {

    private var scanner: GmsBarcodeScanner? = null
    private val _moduleAvailabilityState =
        MutableStateFlow<ModuleAvailability>(ModuleAvailability.CHECKING)
    val moduleAvailabilityState: StateFlow<ModuleAvailability> =
        _moduleAvailabilityState.asStateFlow()

    private var currentInstallListener: InstallStatusListener? = null
    private var onProgressCallback: ((Int) -> Unit)? = null
    private var onCompleteCallback: (() -> Unit)? = null
    private var onErrorCallback: ((String) -> Unit)? = null

    sealed class ModuleAvailability {
        object CHECKING : ModuleAvailability()
        object AVAILABLE : ModuleAvailability()
        object NOT_AVAILABLE : ModuleAvailability()
        object INSTALLING : ModuleAvailability()
        object INSTALL_FAILED : ModuleAvailability()
        object INSTALL_CANCELLED : ModuleAvailability()
    }

    init {
        checkModuleAvailability()
    }

    private fun checkModuleAvailability() {
        val options = GmsBarcodeScannerOptions.Builder().setBarcodeFormats(
            Barcode.FORMAT_QR_CODE,
        ).build()
        scanner = GmsBarcodeScanning.getClient(context, options)

        val moduleInstallClient = ModuleInstall.getClient(context)
        moduleInstallClient.areModulesAvailable(scanner!!).addOnSuccessListener { result ->
            if (result.areModulesAvailable()) {
                _moduleAvailabilityState.value = ModuleAvailability.AVAILABLE
                Log.d("QRScannerManager", "Modules are available")
            } else {
                _moduleAvailabilityState.value = ModuleAvailability.NOT_AVAILABLE
                Log.i("QRScannerManager", "Modules not found on device")
            }
        }.addOnFailureListener { exception ->
            Log.w("QRScannerManager", "Module detection failed: ${exception.message}")
            _moduleAvailabilityState.value = ModuleAvailability.NOT_AVAILABLE
        }
    }

    /**
     * Returns the scanner if available, null if modules need to be installed
     */
    fun getQRScanner(): GmsBarcodeScanner? {
        return when (_moduleAvailabilityState.value) {
            ModuleAvailability.AVAILABLE -> {
                Log.d("QRScannerManager", "Returning available scanner")
                scanner
            }

            else -> {
                Log.d("QRScannerManager", "Scanner not available, modules need installation")
                null
            }
        }
    }

    /**
     * Checks if modules are available without installing
     */
    fun areModulesAvailable(): Boolean {
        return _moduleAvailabilityState.value == ModuleAvailability.AVAILABLE
    }

    /**
     * Starts the module installation process
     */
    fun startInstallation(
        onProgress: ((Int) -> Unit)? = null,
        onComplete: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        if (_moduleAvailabilityState.value == ModuleAvailability.INSTALLING) {
            Log.d("QRScannerManager", "Installation already in progress")
            return
        }

        onProgressCallback = onProgress
        onCompleteCallback = onComplete
        onErrorCallback = onError

        _moduleAvailabilityState.value = ModuleAvailability.INSTALLING

        val moduleInstallClient = ModuleInstall.getClient(context)

        currentInstallListener = object : InstallStatusListener {
            override fun onInstallStatusUpdated(update: ModuleInstallStatusUpdate) {
                update.progressInfo?.let { progressInfo ->
                    val progress =
                        (progressInfo.bytesDownloaded * 100 / progressInfo.totalBytesToDownload).toInt()
                    Log.d("QRScannerManager", "Module download progress: $progress%")
                    onProgress?.invoke(progress)
                }

                if (isTerminateState(update.installState)) {
                    when (update.installState) {
                        InstallState.STATE_COMPLETED -> {
                            Log.i("QRScannerManager", "Module installation completed")
                            _moduleAvailabilityState.value = ModuleAvailability.AVAILABLE
                            onComplete?.invoke()
                        }

                        InstallState.STATE_CANCELED -> {
                            Log.i("QRScannerManager", "Module installation canceled")
                            _moduleAvailabilityState.value = ModuleAvailability.INSTALL_CANCELLED
                            onError?.invoke("Installation was cancelled")
                        }

                        InstallState.STATE_FAILED -> {
                            Log.i("QRScannerManager", "Module installation failed")
                            _moduleAvailabilityState.value = ModuleAvailability.INSTALL_FAILED
                            onError?.invoke("Installation failed")
                        }

                        else -> {
                            Log.i(
                                "QRScannerManager",
                                "Module installation terminated with invalid state"
                            )
                            _moduleAvailabilityState.value = ModuleAvailability.INSTALL_FAILED
                            onError?.invoke("Installation failed with unknown error")
                        }
                    }
                }
            }

            private fun isTerminateState(@InstallState state: Int): Boolean {
                return state == InstallState.STATE_CANCELED ||
                        state == InstallState.STATE_COMPLETED ||
                        state == InstallState.STATE_FAILED
            }
        }

        val moduleInstallRequest = ModuleInstallRequest.newBuilder()
            .addApi(scanner!!)
            .setListener(currentInstallListener!!)
            .build()

        moduleInstallClient.installModules(moduleInstallRequest)
            .addOnSuccessListener {
                Log.i("QRScannerManager", "Module installation requested")
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
                Log.w("QRScannerManager", "Module installation failed: ${exception.message}")
                _moduleAvailabilityState.value = ModuleAvailability.INSTALL_FAILED
                onError?.invoke("Installation failed: ${exception.message}")
            }
    }

    /**
     * Cancels ongoing installation
     */
    fun cancelInstallation() {
        if (_moduleAvailabilityState.value == ModuleAvailability.INSTALLING) {
            Log.d("QRScannerManager", "Cancelling installation")
            _moduleAvailabilityState.value = ModuleAvailability.INSTALL_CANCELLED
            currentInstallListener = null
            onProgressCallback = null
            onCompleteCallback = null
            onErrorCallback = null
        }
    }

    fun getModuleAvailability() = _moduleAvailabilityState.value
}
